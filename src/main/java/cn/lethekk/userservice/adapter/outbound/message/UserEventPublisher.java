package cn.lethekk.userservice.adapter.outbound.message;

import cn.lethekk.userservice.model.domain.CheckInLog;

/**
 * @Author Lethekk
 * @Date 2026/7/19 10:19
 */
public interface UserEventPublisher {

    /**
     * 发送签到
     * 消息
     */
    void publishCheckInEvent(CheckInLog checkIn);

}
