package cn.lethekk.userservice.mq;

import cn.lethekk.userservice.config.RabbitMqConfig;
import cn.lethekk.userservice.dto.CheckInMessage;
import cn.lethekk.userservice.entity.MsgDeduplicationEntity;
import cn.lethekk.userservice.repository.msg.MsgDeduplicationMapper;
import cn.lethekk.userservice.service.CheckInService;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * 积分任务消费者
 */
@Component
@AllArgsConstructor
@Slf4j
public class AddPointsConsumer {

    private final CheckInService checkInService;
    private final MsgDeduplicationMapper msgDeduplicationMapper;

    //@RabbitListener(queues = RabbitMqConfig.POINTS_QUEUE)
    public void consume(CheckInMessage message) {
        try {
            log.info("开始处理积分任务: userId={}", message.getUserId());
            checkInService.addPoints(message.getUserId(), message.getDateTime());
            log.info("积分任务处理成功: userId={}", message.getUserId());
        } catch (Exception e) {
            log.error("积分任务处理失败: userId={}", message.getUserId(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMqConfig.POINTS_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    @Transactional
    public void consumeWithNoRepeated(CheckInMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        //消息状态 (0 待处理 1 插入成功 2 业务成功 )
        int state = 0;
        try {
            //1 尝试插入去幂等表
            msgDeduplicationMapper.insert(new MsgDeduplicationEntity(message.getUserId()));
            state = 1;
            //2 执行业务
            checkInService.addPoints(message.getUserId(), message.getDateTime());
            state = 2;
        } catch (Exception e) {
            if (state == 1) {
                log.error("积分任务处理失败: msgId={}", message.getId(), e);
            }
        } finally {
            log.info("积分任结果:state = {} , msgId={}", state,  message.getId());
        }
        if (state == 0 || state == 2) {
            // 响应ACK
            try {
                channel.basicAck(tag, false);
            } catch (IOException ex) {
                log.error("响应ACK失败: msgId={}", message.getId(), ex);
            }
        }
    }
}
