package cn.lethekk.userservice.service;

import cn.lethekk.userservice.config.RabbitMqConfig;
import cn.lethekk.userservice.dto.AddPointsMessage;
import cn.lethekk.userservice.entity.CheckInDaysEntity;
import cn.lethekk.userservice.entity.CheckInLogEntity;
import cn.lethekk.userservice.entity.PointsLogEntity;
import cn.lethekk.userservice.entity.UserTotalPointsEntity;
import cn.lethekk.userservice.repository.checkin.CheckInDaysMapper;
import cn.lethekk.userservice.repository.checkin.CheckInLogMapper;
import cn.lethekk.userservice.repository.checkin.PointsLogMapper;
import cn.lethekk.userservice.repository.checkin.UserTotalPointsMapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author Lethekk
 * @Date 2026/4/24 14:10
 */
@AllArgsConstructor
@Service
@Slf4j
public class CheckInService {

    private final UserTotalPointsMapper userTotalPointsMapper;
    private final PointsLogMapper pointsLogMapper;
    private final CheckInDaysMapper checkInDaysMapper;
    private final CheckInLogMapper checkInLogMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(rollbackFor = Exception.class)
    public boolean checkIn(Long userId, LocalDateTime ldt) {
        CheckInLogEntity e = CheckInLogEntity.builder()
                .id(IdWorker.getId())
                .userId(userId)
                .date(ldt.toLocalDate())
                .time(ldt)
                .build();
        int insert = checkInLogMapper.insertIgnore(e);
        if (insert == 1) {
            AddPointsMessage message = AddPointsMessage.builder()
                    .userId(userId)
                    .dateTime(ldt)
                    .build();
            rabbitTemplate.convertAndSend(RabbitMqConfig.POINTS_EXCHANGE, RabbitMqConfig.POINTS_ROUTING_KEY, message);
            log.info("积分任务消息已发送: userId={}", userId);
        }
        return insert == 1;
    }

    public boolean isCheckIn(Long userId, LocalDate date) {
        CheckInLogEntity e = checkInLogMapper.selectLog(userId, date);
        return e != null && e.getId() != null;
    }

    public List<CheckInLogEntity> queryRange(Long userId, LocalDate start, LocalDate end) {
        return checkInLogMapper.selectMonthLog(userId, start, end);
    }

    public int queryPoints(Long userId) {
        UserTotalPointsEntity e = userTotalPointsMapper.selectById(userId);
        if (e == null || e.getTotalPoints() == null) {
            return 0;
        }
        return e.getTotalPoints();
    }

    public void addPoints(Long userId, LocalDateTime ldt) {
        //处理连续天数
        CheckInDaysEntity checkInDays = checkInDaysMapper.selectById(userId);
        boolean condition_7_days = false;
        if (checkInDays == null || checkInDays.getUserId() == null) {
            checkInDays = CheckInDaysEntity.builder()
                    .userId(userId)
                    .days(1)
                    .lastDate(ldt.toLocalDate())
                    .updateTime(ldt)
                    .build();
            checkInDaysMapper.insert(checkInDays);
        } else {
            if (checkInDays.getLastDate().equals(ldt.toLocalDate().minusDays(1))) {
                checkInDays.setDays(checkInDays.getDays() + 1);
                if (checkInDays.getDays() % 7 == 0) {
                    condition_7_days = true;
                }
            } else {
                checkInDays.setDays(1);
            }
            checkInDays.setLastDate(ldt.toLocalDate());
            checkInDays.setUpdateTime(ldt);
            checkInDaysMapper.updateById(checkInDays);
        }
        //添加积分
        int addPoints = condition_7_days ? 101 : 1;
        UserTotalPointsEntity userTotalPoints = UserTotalPointsEntity.builder().userId(userId).totalPoints(addPoints).updateTime(ldt).build();
        userTotalPointsMapper.insertOrUpdatePoint(userTotalPoints);
        //记录积分记录
        List<PointsLogEntity> list = new ArrayList<>();
        list.add(PointsLogEntity.builder().id(IdWorker.getId()).userId(userId).type(0).points(1).time(ldt).build());
        if (condition_7_days) {
            list.add(PointsLogEntity.builder().id(IdWorker.getId()).userId(userId).type(1).points(100).time(ldt).build());
        }
        pointsLogMapper.insert(list);
        log.info("积分已累加: userId={}, 增加{}分", userId, addPoints);
    }


}

