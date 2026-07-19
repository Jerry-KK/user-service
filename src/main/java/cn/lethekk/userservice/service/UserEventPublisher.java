package cn.lethekk.userservice.service;

import cn.lethekk.userservice.dto.CheckInMessage;

/**
 * @Author Lethekk
 * @Date 2026/7/19 10:19
 */
public interface UserEventPublisher {

    /**
     * 发送签到
     * 消息
     */
    void publishCheckInEvent(CheckInMessage message);

}
