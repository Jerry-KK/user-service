package cn.lethekk.userservice.adapter.outbound;

import cn.lethekk.userservice.config.RabbitMqConfig;
import cn.lethekk.userservice.dto.CheckInMessage;
import cn.lethekk.userservice.entity.MsgOutBoxEntity;
import cn.lethekk.userservice.repository.msg.MsgOutBoxMapper;
import cn.lethekk.userservice.service.UserEventPublisher;
import cn.lethekk.userservice.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author Lethekk
 * @Date 2026/6/30 23:50
 */
@AllArgsConstructor
@Service
@Slf4j
public class RabbitMqOutBoxAdapter implements UserEventPublisher, ApplicationRunner {

    /**
     * todo
     * 1. msgOutBox中已发送数据需要定期归档，然后移出,以提高msgOutBox查询效率
     * 2. 消息发送需要添加失败重试机制，重试后还不行再标注为异常，后续要有针对发送异常消息的处理逻辑。
     */
    private final MsgOutBoxMapper msgOutBoxMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void publishCheckInEvent(CheckInMessage message) {
        String msgStr = JsonUtils.toJson(message);
        MsgOutBoxEntity msgOutBox = MsgOutBoxEntity.builder()
                .id(message.getId())
                .label(RabbitMqConfig.USER_CHECKIN_KEY)
                .payload(msgStr)
                .state(0)
                .build();
        msgOutBoxMapper.insert(msgOutBox);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduledExecutorService.scheduleWithFixedDelay(this::sendMsgFromBox, 1, 10, TimeUnit.SECONDS);
    }

    private void sendMsgFromBox() {
        List<MsgOutBoxEntity> msgList = msgOutBoxMapper.selectListByState(0);
        if (msgList.isEmpty()) {
            return;
        }
        for (MsgOutBoxEntity msgEntity : msgList) {
            try {
                rabbitTemplate.convertAndSend(RabbitMqConfig.EVENT_EXCHANGE, msgEntity.getLabel(), msgEntity.getPayload());
                log.info("[发件箱]发送成功: msgId={}", msgEntity.getId());
                msgEntity.setState(1);
            } catch (Exception e) {
                log.info("[发件箱]发送失败: msgId={}", msgEntity.getId());
                msgEntity.setState(2);
            } finally {
                msgOutBoxMapper.updateById(msgEntity);
            }
        }
    }
}
