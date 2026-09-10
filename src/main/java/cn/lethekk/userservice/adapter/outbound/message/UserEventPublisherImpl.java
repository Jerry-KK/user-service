package cn.lethekk.userservice.adapter.outbound.message;

import cn.lethekk.userservice.config.RabbitMqConfig;
import cn.lethekk.userservice.model.domain.CheckInLog;
import cn.lethekk.userservice.model.event.MsgOutBoxEvent;
import cn.lethekk.userservice.mq.EventPublisher;
import cn.lethekk.userservice.utils.JsonUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author Lethekk
 * @Date 2026/9/5 13:04
 */
@AllArgsConstructor
@Component
public class UserEventPublisherImpl implements UserEventPublisher {

    private EventPublisher eventPublisher;

    @Override
    public void publishCheckInEvent(CheckInLog checkIn) {
        MsgOutBoxEvent event = convert(checkIn);
        eventPublisher.send(event);
    }

    private MsgOutBoxEvent convert(CheckInLog checkInLog) {
        return MsgOutBoxEvent.builder()
                .id(checkInLog.getId())
                .label(RabbitMqConfig.USER_CHECKIN_KEY)
                .payload(JsonUtils.toJson(checkInLog))
                .state(0)
                .build();
    }

}
