# 压测下 MySQL 间隙锁导致的并发死锁分析与修复

---
>**TL;DR：**
> JMeter 并发压测出现死锁，根因是MySQL 默认隔离级别（RR）下 update-then-insert 模式产生 Gap Lock 循环等待，通过 原子语句改写 与 隔离级别调整 将压测错误率从 65% 降低至 0%。
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
- **现象：** 使用JMeter并发压测错误率高达65%，MySQL出现死锁情况。
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
##### 描述:
- update语句在页面 Supremun记录 (页面最大的伪记录) 前加 **间隙锁**。导致后续insert语句在插入前判断存在 **间隙锁**，创建插入意向锁后进入等待状态。
- 多个事务持有锁并等待其他事务释放锁产生了死锁。
### 3. 解决方案
方案 A：原子语句改写
- 改用 `INSERT ... ON DUPLICATE KEY UPDATE` 语句。
- 优化点：合并为单条原子操作，大幅缩短了事务持锁时间。
```sql
INSERT INTO user_total_points (...) VALUES (...) ON DUPLICATE KEY UPDATE total_points = total_points + ?;
```
- 备注：修复后 JMeter 的错误率从 65% 降到0%。
#### 方案 B：调整隔离级别（工程推荐）
将数据库隔离级别由 `REPEATABLE READ` 修改为 **`READ COMMITTED` (RC)**。
*   **原理：** RC 模式下不存在间隙锁（Gap Lock），仅保留记录锁。
*   **工程价值：** 彻底消除由间隙锁引发的死锁风险，提升全库并发上限。
#### **方案 A 与方案 B 应当配合使用**
### 4. 工程经验归纳

1.  **架构权衡：** 在高并发架构中，**RR 的死锁/停机破坏性** 远大于 **RC 的不可重复读/幻读风险**。互联网业务推荐优先使用 RC 隔离级别。
2.  **原子性原则：** 能用单条 SQL 完成的操作，绝不通过代码逻辑组合。原子语句是降低锁冲突最简单、最高效的手段。

---



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

