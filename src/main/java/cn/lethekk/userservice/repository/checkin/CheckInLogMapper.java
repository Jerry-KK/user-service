package cn.lethekk.userservice.repository.checkin;

import cn.lethekk.userservice.entity.CheckInLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author Lethekk
 * @Date 2026/4/24 14:08
 */
public interface CheckInLogMapper extends BaseMapper<CheckInLogEntity> {

    @Insert("INSERT IGNORE INTO `check_in_log` (id, user_id, date,time) VALUES (#{id}, #{userId}, #{date}, #{time})")
    int insertIgnore(CheckInLogEntity e);

    CheckInLogEntity selectLog(@Param("userId") Long userId, @Param("date") LocalDate date);

    List<CheckInLogEntity> selectMonthLog(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

}
