package cn.lethekk.userservice.adapter.outbound.repository;

import cn.lethekk.userservice.dao.checkin.CheckInLogMapper;
import cn.lethekk.userservice.model.domain.CheckInLog;
import cn.lethekk.userservice.model.po.CheckInLogPO;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @Author Lethekk
 * @Date 2026/9/5 12:50
 */
@AllArgsConstructor
@Component
public class CheckInLogRepositoryImpl implements CheckInLogRepository {

    private final CheckInLogMapper checkInLogMapper;

    @Override
    public boolean save(CheckInLog checkInLog) {
        CheckInLogPO po = convert(checkInLog);
        int n = checkInLogMapper.insertIgnore(po);
        return n == 1;
    }

    private CheckInLogPO convert(CheckInLog checkInLog) {
        CheckInLogPO checkInLogPO = new CheckInLogPO();
        BeanUtils.copyProperties(checkInLog, checkInLogPO);
        return checkInLogPO;
    }

}
