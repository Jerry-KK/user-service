package cn.lethekk.userservice.adapter.outbound.repository;

import cn.lethekk.userservice.model.domain.CheckInLog;

/**
 * @Author Lethekk
 * @Date 2026/9/5 12:49
 */
public interface CheckInLogRepository {

    boolean save(CheckInLog checkInLog);

}
