# CheckInService 代码审查问题记录

**审查日期：** 2026/05/24  
**审查对象：** `cn.lethekk.userservice.service.CheckInService`  
**发现问题数：** 1 个

---

## 问题：消息可靠性 - 事务与 MQ 消息发送不一致

**严重程度：** 🔴 **Critical**  
**分类：** 数据一致性 / 事务设计  
**位置：** `checkIn()` 方法，第 42-59 行

### 问题描述

当前代码在 `@Transactional` 事务内直接发送 RabbitMQ 消息，RabbitMQ 是外部系统，不参与数据库事务，导致以下风险：

```java
@Transactional(rollbackFor = Exception.class)
public boolean checkIn(Long userId, LocalDateTime ldt) {
    int insert = checkInLogMapper.insertIgnore(e);
    if (insert == 1) {
        // 消息已发出，但事务尚未提交，仍可能回滚
        rabbitTemplate.convertAndSend(..., message);
    }
    return insert == 1;
}
```

### 风险场景

1. **消息已发送，事务回滚**：消费端已处理积分，但 `check_in_log` 记录不存在 → 积分无对应签到记录
2. **事务成功，消息发送失败**：`check_in_log` 插入成功，但消息未发出 → 签到记录存在但积分永不累加
3. **消息重试，消费端无幂等**：消息被重试多次，积分重复累加

### 修复方向

发送端和消费端均需处理：

**发送端（CheckInService）**
- 方案 A：使用 `TransactionSynchronizationManager` 在事务提交后发送消息，消息发送失败则需额外重试机制
- 方案 B（推荐）：本地消息表 — 在同一事务内将消息写入 `check_in_message` 表，定时任务扫表投递到 MQ，可重试，不丢失

**消费端（addPoints 消费逻辑）**
- 引入幂等去重：基于 `check_in_log` 的唯一 ID 判断是否已处理，防止重复消费
- 改为手动 ACK：业务处理成功后再提交 ACK，失败则回队重试

### 关联讨论

消费端幂等机制同时可以规避 `check_in_days` 读改写的并发问题（同一用户同一天只有一条有效签到，消费端去重后消费者串行处理，天然避免并发写冲突），无需额外引入悲观锁。若后续高并发场景需要更强并发保护，推荐在 v3 引入 Redis 后使用分布式锁，而非 MySQL 悲观锁。

