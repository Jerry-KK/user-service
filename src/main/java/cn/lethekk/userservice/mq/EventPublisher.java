package cn.lethekk.userservice.mq;

import cn.lethekk.userservice.model.event.MsgOutBoxEvent;

/**
 * @Author Lethekk
 * @Date 2026/9/5 13:12
 */
public interface EventPublisher {

    void send(MsgOutBoxEvent event);

}
