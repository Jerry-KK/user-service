package cn.lethekk.userservice.mq;

import cn.lethekk.userservice.dto.CheckInMessage;
import cn.lethekk.userservice.service.CheckInService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * AddPointsConsumer 单元测试
 * 覆盖 MQ 消息消费逻辑
 */
@ExtendWith(MockitoExtension.class)
class AddPointsConsumerTest {

    @Mock
    private CheckInService checkInService;

    private AddPointsConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new AddPointsConsumer(checkInService);
    }

    @Test
    @DisplayName("consume: 正常消费消息，应调用 checkInService.addPoints() 并传入正确参数")
    void consume_normalMessage_shouldCallAddPoints() {
        // given
        Long userId = 9001L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 10, 30, 0);
        CheckInMessage message = CheckInMessage.builder()
                .userId(userId)
                .dateTime(ldt)
                .build();
        doNothing().when(checkInService).addPoints(userId, ldt);

        // when
        consumer.consume(message);

        // then
        verify(checkInService).addPoints(userId, ldt);
    }

    @Test
    @DisplayName("consume: addPoints 抛出异常时，消费者应重新抛出（让 RabbitMQ 重试或死信）")
    void consume_addPointsThrows_shouldRethrow() {
        // given
        Long userId = 9002L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 11, 0, 0);
        CheckInMessage message = CheckInMessage.builder()
                .userId(userId)
                .dateTime(ldt)
                .build();
        RuntimeException simulatedEx = new RuntimeException("数据库连接失败（模拟）");
        doThrow(simulatedEx).when(checkInService).addPoints(userId, ldt);

        // when / then
        assertThatThrownBy(() -> consumer.consume(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("数据库连接失败（模拟）");
        // addPoints 仍被调用了一次
        verify(checkInService).addPoints(userId, ldt);
    }

    @Test
    @DisplayName("consume: 每次消费仅调用一次 addPoints，不重复处理")
    void consume_shouldCallAddPointsExactlyOnce() {
        // given
        Long userId = 9003L;
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 21, 12, 0, 0);
        CheckInMessage message = CheckInMessage.builder()
                .userId(userId)
                .dateTime(ldt)
                .build();

        // when
        consumer.consume(message);

        // then
        verify(checkInService, times(1)).addPoints(userId, ldt);
        verifyNoMoreInteractions(checkInService);
    }
}
