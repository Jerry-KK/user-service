package cn.lethekk.userservice.manage;

import cn.lethekk.userservice.entity.CheckInLogEntity;
import cn.lethekk.userservice.repository.checkin.CheckInLogMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Lethekk
 * @Date 2026/4/26 14:14
 */
@AllArgsConstructor
@Component
public class CheckInTableShardingManager {

    private final CheckInLogMapper checkInLogMapper;

    public int insertCheckInLog(CheckInLogEntity e) {
        String monthSuf = getMonthSuf(e.getDate());
        return checkInLogMapper.insertMonthTable(e, monthSuf);
    }

    public CheckInLogEntity selectLog(Long userId, LocalDate date) {
        String monthSuf = getMonthSuf(date);
        return checkInLogMapper.selectLog(userId, date, monthSuf);
    }

    public List<CheckInLogEntity> queryRange(Long userId, LocalDate start, LocalDate end) {
        List<CheckInLogEntity> res = new ArrayList<>();
        List<LocalDate[]> queryRangeList = new ArrayList<>();
        YearMonth startYM = YearMonth.from(start);
        YearMonth endYM = YearMonth.from(end);
        YearMonth cur = startYM;
        while (!cur.isAfter(endYM)) {
            LocalDate s = cur.equals(startYM) ? start : cur.atDay(1);
            LocalDate e = cur.equals(endYM) ? end : cur.atEndOfMonth();
            queryRangeList.add(new LocalDate[]{s, e});
            cur = cur.plusMonths(1);
        }
        queryRangeList.forEach((LocalDate[] arr) -> {
            List<CheckInLogEntity> logList = checkInLogMapper.selectMonthLog(userId, arr[0], arr[1], getMonthSuf(arr[0]));
            res.addAll(logList);
        });
        return res;
    }

    private String getMonthSuf(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

}
