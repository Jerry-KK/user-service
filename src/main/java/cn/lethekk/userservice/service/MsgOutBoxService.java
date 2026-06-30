package cn.lethekk.userservice.service;

import cn.lethekk.userservice.config.RabbitMqConfig;
import cn.lethekk.userservice.dto.CheckInMessage;
import cn.lethekk.userservice.entity.MsgOutBoxEntity;
import cn.lethekk.userservice.repository.msg.MsgOutBoxMapper;
import cn.lethekk.userservice.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
public class MsgOutBoxService {

    /**
     * todo
     * 1. msgOutBox中已发送数据需要定期归档，然后移出,以提高msgOutBox查询效率
     * 2. 消息发送需要添加失败重试机制，重试后还不行再标注为异常，后续要有针对发送异常消息的处理逻辑。
     */

    private final MsgOutBoxMapper msgOutBoxMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleWithFixedDelay(this::sendMsg, 1, 10, TimeUnit.SECONDS);
    }

    private void sendMsg() {
        List<MsgOutBoxEntity> msgList = msgOutBoxMapper.selectListByState(0);
        if (msgList.isEmpty()) {
            return;
        }
        for (MsgOutBoxEntity msgEntity : msgList) {
            String msgStr = msgEntity.getPayload();
            CheckInMessage msg  = JsonUtils.fromJson(msgStr, CheckInMessage.class);
            try {
                rabbitTemplate.convertAndSend(RabbitMqConfig.EVENT_EXCHANGE, msgEntity.getLabel(), msg);
                log.info("[发件箱]发送成功: msgId={}", msg.getId());
                msgEntity.setState(1);
            } catch (Exception e) {
                log.info("[发件箱]发送失败: msgId={}", msg.getId());
                msgEntity.setState(2);
            } finally {
                msgOutBoxMapper.updateById(msgEntity);
            }
        }
    }
}
