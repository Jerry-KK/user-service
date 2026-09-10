package cn.lethekk.userservice.mq;

import cn.lethekk.userservice.config.RabbitMqConfig;
import cn.lethekk.userservice.model.domain.CheckInLog;
import cn.lethekk.userservice.service.MsgDeduplicationService;
import cn.lethekk.userservice.utils.JsonUtils;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 积分任务消费者
 */
@Component
@AllArgsConstructor
@Slf4j
public class AddPointsConsumer {

    private final MsgDeduplicationService msgDeduplicationService;

    //todo 这里数据转换需要策略或eventbus模式
    @RabbitListener(queues = RabbitMqConfig.POINTS_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void consumeWithNoRepeated(String payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            CheckInLog checkInLog = convert(payload);
            msgDeduplicationService.consumeWithNoRepeated(checkInLog);
            // 响应ACK
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("积分任务处理失败: payload={}", payload, e);
            //显式拒绝消息
            // todo: 暂时不考虑复杂的重试和死信。直接丢弃，避免消息极高频无间隔重试
            channel.basicNack(tag, false, false);
        }
    }

    private CheckInLog convert(String payload) {
        return JsonUtils.fromJson(payload, CheckInLog.class);
    }
}
