package cn.lethekk.userservice.controller;

import cn.lethekk.userservice.entity.CheckInLogEntity;
import cn.lethekk.userservice.service.CheckInService;
import cn.lethekk.userservice.utils.TimestampConverterUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Lethekk
 * @Date 2026/4/24 13:19
 */
@RequestMapping("/checkin")
@AllArgsConstructor
@RestController
public class CheckInController {

    private final CheckInService service;

    //签到
    //签到仅签到一次，且需要累加积分
    @GetMapping("/checkIn")
    public boolean checkIn(Long userId) {
        //签到

        //发送MQ
        //异步提交累加积分任务,即积分有个总计，有个记录表
        return service.checkIn(userId , LocalDateTime.now());
    }



    //查看今日是否签到
    @GetMapping("/isCheckIn")
    public boolean isCheckIn(Long userId) {
        return service.isCheckIn(userId, LocalDate.now());
    }


    //查询一段时间签到记录
    @GetMapping("/queryRange")
    public List<CheckInLogEntity> queryRange(Long userId, Long startTime, Long endTime) {
        //todo 限制查询范围，跨度不能太大
        LocalDate start = TimestampConverterUtil.fromMillis(startTime).toLocalDate();
        LocalDate end = TimestampConverterUtil.fromMillis(endTime).toLocalDate();
        return service.queryRange(userId, start, end);
    }

    //查询当前连续签到数


    //积分查询
    @GetMapping("/queryPoints")
    public int queryPoints(Long userId) {
        return service.queryPoints(userId);
    }
}
