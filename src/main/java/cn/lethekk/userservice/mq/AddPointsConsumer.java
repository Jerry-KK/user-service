package cn.lethekk.userservice.mq;

import cn.lethekk.userservice.config.RabbitMqConfig;
import cn.lethekk.userservice.dto.AddPointsMessage;
import cn.lethekk.userservice.service.CheckInService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 积分任务消费者
 */
@Component
@AllArgsConstructor
@Slf4j
public class AddPointsConsumer {

    private final CheckInService checkInService;

    @RabbitListener(queues = RabbitMqConfig.POINTS_QUEUE)
    public void consume(AddPointsMessage message) {
        try {
            log.info("开始处理积分任务: userId={}", message.getUserId());
            checkInService.addPoints(message.getUserId(), message.getDateTime());
            log.info("积分任务处理成功: userId={}", message.getUserId());
        } catch (Exception e) {
            log.error("积分任务处理失败: userId={}", message.getUserId(), e);
            throw e;
        }
    }
}
