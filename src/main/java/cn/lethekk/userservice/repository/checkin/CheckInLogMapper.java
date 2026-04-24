package cn.lethekk.userservice.repository.checkin;

import cn.lethekk.userservice.entity.CheckInLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;

/**
 * @Author Lethekk
 * @Date 2026/4/24 14:08
 */
public interface CheckInLogMapper extends BaseMapper<CheckInLogEntity> {

    @Insert("INSERT IGNORE INTO `check_in_log` (id, user_id, date,time) VALUES (#{id}, #{userId}, #{date}, #{time})")
    int insertIgnore(CheckInLogEntity e);

}
