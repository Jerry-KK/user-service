package cn.lethekk.userservice.mq;

import cn.lethekk.userservice.config.RabbitMqConfig;
import cn.lethekk.userservice.model.event.MsgOutBoxEvent;
import cn.lethekk.userservice.model.po.MsgOutBoxPO;
import cn.lethekk.userservice.dao.msg.MsgOutBoxMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
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
public class RabbitMqOutBoxAdapter implements EventPublisher, ApplicationRunner {

    /**
     * todo
     * 1. msgOutBox中已发送数据需要定期归档，然后移出,以提高msgOutBox查询效率
     * 2. 消息发送需要添加失败重试机制，重试后还不行再标注为异常，后续要有针对发送异常消息的处理逻辑。
     */
    private final MsgOutBoxMapper msgOutBoxMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void send(MsgOutBoxEvent event) {
        MsgOutBoxPO po = convert(event);
        msgOutBoxMapper.insert(po);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduledExecutorService.scheduleWithFixedDelay(this::sendMsgFromBox, 1, 10, TimeUnit.SECONDS);
    }

    private void sendMsgFromBox() {
        List<MsgOutBoxPO> msgList = msgOutBoxMapper.selectListByState(0);
        if (msgList.isEmpty()) {
            return;
        }
        for (MsgOutBoxPO msgPO : msgList) {
            try {
                rabbitTemplate.convertAndSend(RabbitMqConfig.EVENT_EXCHANGE, msgPO.getLabel(), msgPO.getPayload());
                log.info("[发件箱]发送成功: msgId={}", msgPO.getId());
                msgPO.setState(1);
            } catch (Exception e) {
                log.info("[发件箱]发送失败: msgId={}", msgPO.getId());
                msgPO.setState(2);
            } finally {
                msgOutBoxMapper.updateById(msgPO);
            }
        }
    }

    private MsgOutBoxPO convert(MsgOutBoxEvent event) {
        MsgOutBoxPO po = new MsgOutBoxPO();
        BeanUtils.copyProperties(event, po);
        return po;
    }
}
