# 压测下 MySQL 间隙锁导致的并发死锁分析与修复

---
>**TL;DR：**
> JMeter 并发压测出现死锁，根因是 REPEATABLE READ 下 update-then-insert 模式产生 Gap Lock 循环等待，用 `INSERT ... ON DUPLICATE KEY UPDATE` 替换后解决。
---
### 1. 场景复现
- **业务逻辑：** 累加用户积分。
- **代码实现：**
```java
// 第一步：先尝试 UPDATE
int update = userTotalPointsMapper.update(null, wrapper);
// 第二步：UPDATE 影响0行，再 INSERT
if (update == 0) {
    userTotalPointsMapper.insert(userTotalPoints);
}
```
- **现象：** 使用JMeter并发压测错误率高达65%，多次测试中甚至出现MySQL死锁情况。
### 2. 死锁原因分析

**update-then-insert（经典死锁模式）：**

```sql
时间线：
线程A: UPDATE → 0行 → 准备INSERT，持有间隙锁
线程B: UPDATE → 0行 → 准备INSERT，持有间隙锁
线程A: INSERT → 等待线程B释放间隙锁
线程B: INSERT → 等待线程A释放间隙锁
                         ↓
                      死锁！
```

背景:
- **隔离级别：** `REPEATABLE READ` (默认)。
- 使用的具有递增特性的分布式ID，导致插入总在索引末尾的同一个间隙，竞争更集中。
  描述:
- update语句在页面 Supremun记录 (页面最大的伪记录) 前加 **间隙锁**。导致后续insert语句在插入前判断存在 **间隙锁**，创建插入意向锁后进入等待状态。
- 多个事务持有锁并等待其他事务释放锁产生了死锁。
### 3. 解决方案
修复方案:
- 改用 `INSERT ... ON DUPLICATE KEY UPDATE` 语句。
- 优化点：合并为单条原子操作，大幅缩短了事务持锁时间。
```sql
INSERT INTO user_total_points (...) VALUES (...) ON DUPLICATE KEY UPDATE total_points = total_points + ?;
```
- 备注：修复后 JMeter 的错误率从 65% 降到0%。
  其他修复方案：
- 由于是 **可重复读** 隔离模式下间隙锁导致的死锁，如果业务条件符合，改用 **已提交读** 隔离模式不会用到间隙锁，也就不会出现死锁。

***
### 相关知识

|       | 事务并发执行遇到的一致性问题                                     |
| ----- | -------------------------------------------------- |
| 脏写    | 事务修改未提交事务修改过的数据                                    |
| 脏读    | 事务读到未提交事务修改过的数据                                    |
| 不可重复读 | 事务修改未提交事务读取的数据                                     |
| 幻读    | 如果一个事务先根据某些搜索条件查询出一些记录，该事务未提交时，另一个事务写入了一些符合搜索条件的记录 |

| 隔离级别             |           | 脏读  | 不可重复读 | 幻读    |
| ---------------- | --------- | --- | ----- | ----- |
| READ UNCOMMITTED | 未提交读      | 可能  | 可能    | 可能    |
| READ COMMITTED   | 已提交读      | 不可能 | 可能    | 可能    |
| REPEATABLE READ  | 可重复读 (默认) | 不可能 | 不可能   | 大部分解决 |
| SERIALIZABLE     | 可串行化      | 不可能 | 不可能   | 不可能   |

| 锁                     |       | 说明                                                   |
| --------------------- | ----- | ---------------------------------------------------- |
| Record Lock           | 记录锁   | 锁住记录行                                                |
| Gap Lock              | 间隙锁   | 为解决幻读问题，限制记录前间隙不能插入记录                                |
| Next-Key Lock         | 临键锁   | 等于Record Lock+Gap Lock                               |
| Insert Intention Lock | 插入意向锁 | INSERT 前加的特殊 Gap Lock，与 Gap Lock 冲突，同一间隙插入不同位置时互相不阻塞 |
|                       | 隐式锁   | 优化手段，延迟插入意向锁生成                                       |

特殊作用范围说明:
- Gap Lock为REPEATABLE READ、SERIALIZABLE
- MVCC为READ COMMITTED、REPEATABLE READ；不同在于生成Read View的时机

