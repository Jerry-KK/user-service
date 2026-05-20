package cn.lethekk.userservice.service;

import cn.lethekk.userservice.config.RabbitMqConfig;
import cn.lethekk.userservice.dto.AddPointsMessage;
import cn.lethekk.userservice.entity.CheckInDaysEntity;
import cn.lethekk.userservice.entity.CheckInLogEntity;
import cn.lethekk.userservice.entity.PointsLogEntity;
import cn.lethekk.userservice.entity.UserTotalPointsEntity;
import cn.lethekk.userservice.repository.checkin.CheckInDaysMapper;
import cn.lethekk.userservice.repository.checkin.CheckInLogMapper;
import cn.lethekk.userservice.repository.checkin.PointsLogMapper;
import cn.lethekk.userservice.repository.checkin.UserTotalPointsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * CheckInService 单元测试
 * 覆盖异步化积分功能：checkIn() 发送 MQ 消息、addPoints() 积分逻辑
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class CheckInServiceTest {

    @Mock
    private UserTotalPointsMapper userTotalPointsMapper;
    @Mock
    private PointsLogMapper pointsLogMapper;
    @Mock
    private CheckInDaysMapper checkInDaysMapper;
    @Mock
    private CheckInLogMapper checkInLogMapper;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private CheckInService checkInService;

    @BeforeEach
    void setUp() {
        checkInService = new CheckInService(
                userTotalPointsMapper,
                pointsLogMapper,
                checkInDaysMapper,
                checkInLogMapper,
                rabbitTemplate
        );
    }

    // ==================== checkIn() 测试 ====================

    @Test
    @DisplayName("checkIn: 首次签到成功，应发送 MQ 消息并返回 true")
    void checkIn_firstTimeSuccess_shouldSendMqMessageAndReturnTrue() {
        // given
        Long userId = 1001L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 10, 0, 0);
        given(checkInLogMapper.insertIgnore(any(CheckInLogEntity.class))).willReturn(1);

        // when
        boolean result = checkInService.checkIn(userId, ldt);

        // then
        assertThat(result).isTrue();
        // 验证发送了 MQ 消息
        ArgumentCaptor<AddPointsMessage> msgCaptor = ArgumentCaptor.forClass(AddPointsMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.POINTS_EXCHANGE),
                eq(RabbitMqConfig.POINTS_ROUTING_KEY),
                msgCaptor.capture()
        );
        AddPointsMessage sentMsg = msgCaptor.getValue();
        assertThat(sentMsg.getUserId()).isEqualTo(userId);
        assertThat(sentMsg.getDateTime()).isEqualTo(ldt);
    }

    @Test
    @DisplayName("checkIn: 当天重复签到（INSERT IGNORE 返回 0），不发送 MQ 消息，返回 false")
    void checkIn_duplicateToday_shouldNotSendMqAndReturnFalse() {
        // given
        Long userId = 1001L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 10, 0, 0);
        // INSERT IGNORE 因唯一键冲突返回 0
        given(checkInLogMapper.insertIgnore(any(CheckInLogEntity.class))).willReturn(0);

        // when
        boolean result = checkInService.checkIn(userId, ldt);

        // then
        assertThat(result).isFalse();
        // 不应发送 MQ 消息
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("checkIn: insertIgnore 的日志实体 date 字段正确由 ldt 转换")
    void checkIn_insertEntityDateShouldMatchLdt() {
        // given
        Long userId = 2002L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 15, 8, 30, 0);
        given(checkInLogMapper.insertIgnore(any(CheckInLogEntity.class))).willReturn(1);

        // when
        checkInService.checkIn(userId, ldt);

        // then
        ArgumentCaptor<CheckInLogEntity> entityCaptor = ArgumentCaptor.forClass(CheckInLogEntity.class);
        verify(checkInLogMapper).insertIgnore(entityCaptor.capture());
        CheckInLogEntity entity = entityCaptor.getValue();
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getDate()).isEqualTo(ldt.toLocalDate());
        assertThat(entity.getTime()).isEqualTo(ldt);
        assertThat(entity.getId()).isNotNull();
    }

    // ==================== addPoints() 测试 ====================

    @Test
    @DisplayName("addPoints: 用户首次累积（无 check_in_days 记录），创建连续天数记录，累加 1 积分")
    void addPoints_firstTime_shouldCreateDaysRecordAndAdd1Point() {
        // given
        Long userId = 3003L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 10, 0, 0);
        // 无历史签到记录
        given(checkInDaysMapper.selectById(userId)).willReturn(null);
        // pointsLogMapper.insert(Collection) 在 MyBatis-Plus 3.5.x 中返回 List<BatchResult>，无需 stub

        // when
        checkInService.addPoints(userId, ldt);

        // then
        // 验证创建了新的 check_in_days 记录，days=1
        ArgumentCaptor<CheckInDaysEntity> daysCaptor = ArgumentCaptor.forClass(CheckInDaysEntity.class);
        verify(checkInDaysMapper).insert(daysCaptor.capture());
        assertThat(daysCaptor.getValue().getDays()).isEqualTo(1);
        assertThat(daysCaptor.getValue().getLastDate()).isEqualTo(ldt.toLocalDate());

        // 验证 upsert 积分，值为 1
        ArgumentCaptor<UserTotalPointsEntity> pointsCaptor = ArgumentCaptor.forClass(UserTotalPointsEntity.class);
        verify(userTotalPointsMapper).insertOrUpdatePoint(pointsCaptor.capture());
        assertThat(pointsCaptor.getValue().getTotalPoints()).isEqualTo(1);

        // 验证积分日志只有 1 条（type=0, points=1）
        ArgumentCaptor<List> logCaptor = ArgumentCaptor.forClass(List.class);
        verify(pointsLogMapper).insert(logCaptor.capture());
        List<PointsLogEntity> logs = logCaptor.getValue();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getType()).isEqualTo(0);
        assertThat(logs.get(0).getPoints()).isEqualTo(1);
    }

    @Test
    @DisplayName("addPoints: 连续签到（昨天已签），连续天数 +1，累加 1 积分")
    void addPoints_consecutiveDay_shouldIncrementDaysAndAdd1Point() {
        // given
        Long userId = 4004L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 10, 0, 0);
        LocalDate yesterday = ldt.toLocalDate().minusDays(1);

        CheckInDaysEntity existingDays = CheckInDaysEntity.builder()
                .userId(userId)
                .days(3)
                .lastDate(yesterday)
                .updateTime(ldt.minusDays(1))
                .build();
        given(checkInDaysMapper.selectById(userId)).willReturn(existingDays);

        // when
        checkInService.addPoints(userId, ldt);

        // then
        // 连续天数应变为 4
        ArgumentCaptor<CheckInDaysEntity> daysCaptor = ArgumentCaptor.forClass(CheckInDaysEntity.class);
        // 使用明确的 CheckInDaysEntity 类型消除 updateById 重载歧义
        verify(checkInDaysMapper).updateById(daysCaptor.capture());
        assertThat(daysCaptor.getValue().getDays()).isEqualTo(4);
        assertThat(daysCaptor.getValue().getLastDate()).isEqualTo(ldt.toLocalDate());

        // 积分 +1，无奖励
        ArgumentCaptor<UserTotalPointsEntity> pointsCaptor = ArgumentCaptor.forClass(UserTotalPointsEntity.class);
        verify(userTotalPointsMapper).insertOrUpdatePoint(pointsCaptor.capture());
        assertThat(pointsCaptor.getValue().getTotalPoints()).isEqualTo(1);

        // 日志只有 1 条
        ArgumentCaptor<List> logCaptor = ArgumentCaptor.forClass(List.class);
        verify(pointsLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("addPoints: 非连续签到（lastDate 不是昨天），连续天数重置为 1，累加 1 积分")
    void addPoints_nonConsecutiveDay_shouldResetDaysAndAdd1Point() {
        // given
        Long userId = 5005L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 10, 0, 0);
        LocalDate twoDaysAgo = ldt.toLocalDate().minusDays(2);

        CheckInDaysEntity existingDays = CheckInDaysEntity.builder()
                .userId(userId)
                .days(5)
                .lastDate(twoDaysAgo)
                .updateTime(ldt.minusDays(2))
                .build();
        given(checkInDaysMapper.selectById(userId)).willReturn(existingDays);

        // when
        checkInService.addPoints(userId, ldt);

        // then
        // 连续天数重置为 1
        ArgumentCaptor<CheckInDaysEntity> daysCaptor = ArgumentCaptor.forClass(CheckInDaysEntity.class);
        verify(checkInDaysMapper).updateById(daysCaptor.capture());
        assertThat(daysCaptor.getValue().getDays()).isEqualTo(1);

        // 不触发奖励，积分 +1
        ArgumentCaptor<UserTotalPointsEntity> pointsCaptor = ArgumentCaptor.forClass(UserTotalPointsEntity.class);
        verify(userTotalPointsMapper).insertOrUpdatePoint(pointsCaptor.capture());
        assertThat(pointsCaptor.getValue().getTotalPoints()).isEqualTo(1);

        // 日志 1 条
        ArgumentCaptor<List> logCaptor = ArgumentCaptor.forClass(List.class);
        verify(pointsLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("addPoints: 连续 7 天签到达成，触发 100 积分奖励，共累加 101 积分，积分日志 2 条")
    void addPoints_seventh_consecutive_day_shouldTrigger100BonusAndAdd101Points() {
        // given
        Long userId = 6006L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 10, 0, 0);
        LocalDate yesterday = ldt.toLocalDate().minusDays(1);

        // 当前连续签到 6 天，再签一次即满 7 天
        CheckInDaysEntity existingDays = CheckInDaysEntity.builder()
                .userId(userId)
                .days(6)
                .lastDate(yesterday)
                .updateTime(ldt.minusDays(1))
                .build();
        given(checkInDaysMapper.selectById(userId)).willReturn(existingDays);

        // when
        checkInService.addPoints(userId, ldt);

        // then
        // 连续天数应为 7
        ArgumentCaptor<CheckInDaysEntity> daysCaptor = ArgumentCaptor.forClass(CheckInDaysEntity.class);
        verify(checkInDaysMapper).updateById(daysCaptor.capture());
        assertThat(daysCaptor.getValue().getDays()).isEqualTo(7);

        // 触发奖励，积分 +101（1 普通 + 100 奖励）
        ArgumentCaptor<UserTotalPointsEntity> pointsCaptor = ArgumentCaptor.forClass(UserTotalPointsEntity.class);
        verify(userTotalPointsMapper).insertOrUpdatePoint(pointsCaptor.capture());
        assertThat(pointsCaptor.getValue().getTotalPoints()).isEqualTo(101);

        // 积分日志 2 条：type=0 普通积分 + type=1 奖励积分
        ArgumentCaptor<List> logCaptor = ArgumentCaptor.forClass(List.class);
        verify(pointsLogMapper).insert(logCaptor.capture());
        List<PointsLogEntity> logs = logCaptor.getValue();
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getType()).isEqualTo(0);
        assertThat(logs.get(0).getPoints()).isEqualTo(1);
        assertThat(logs.get(1).getType()).isEqualTo(1);
        assertThat(logs.get(1).getPoints()).isEqualTo(100);
    }

    @Test
    @DisplayName("addPoints: 连续 14 天（两个周期），再次触发 100 积分奖励")
    void addPoints_fourteenth_consecutive_day_shouldTriggerBonusAgain() {
        // given
        Long userId = 7007L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 10, 0, 0);
        LocalDate yesterday = ldt.toLocalDate().minusDays(1);

        // 连续签到 13 天，再签一次即第 14 天（7 的倍数）
        CheckInDaysEntity existingDays = CheckInDaysEntity.builder()
                .userId(userId)
                .days(13)
                .lastDate(yesterday)
                .updateTime(ldt.minusDays(1))
                .build();
        given(checkInDaysMapper.selectById(userId)).willReturn(existingDays);

        // when
        checkInService.addPoints(userId, ldt);

        // then
        ArgumentCaptor<CheckInDaysEntity> daysCaptor = ArgumentCaptor.forClass(CheckInDaysEntity.class);
        verify(checkInDaysMapper).updateById(daysCaptor.capture());
        assertThat(daysCaptor.getValue().getDays()).isEqualTo(14);

        ArgumentCaptor<UserTotalPointsEntity> pointsCaptor = ArgumentCaptor.forClass(UserTotalPointsEntity.class);
        verify(userTotalPointsMapper).insertOrUpdatePoint(pointsCaptor.capture());
        assertThat(pointsCaptor.getValue().getTotalPoints()).isEqualTo(101);

        ArgumentCaptor<List> logCaptor = ArgumentCaptor.forClass(List.class);
        verify(pointsLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("addPoints: userId 对应 check_in_days 记录存在但 userId 字段为 null，视为首次，创建新记录")
    void addPoints_existingRecordWithNullUserId_shouldCreateNew() {
        // given
        Long userId = 8008L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 10, 0, 0);
        // selectById 返回一个空壳对象（userId 为 null）
        CheckInDaysEntity emptyEntity = CheckInDaysEntity.builder().build();
        given(checkInDaysMapper.selectById(userId)).willReturn(emptyEntity);

        // when
        checkInService.addPoints(userId, ldt);

        // then
        // 应走 insert 分支
        verify(checkInDaysMapper).insert(any(CheckInDaysEntity.class));
        // 明确指定 CheckInDaysEntity 类型避免 updateById 重载歧义
        verify(checkInDaysMapper, never()).updateById(any(CheckInDaysEntity.class));
    }
}
