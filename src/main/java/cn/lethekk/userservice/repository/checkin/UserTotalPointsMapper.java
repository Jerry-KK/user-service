package cn.lethekk.userservice.repository.checkin;

import cn.lethekk.userservice.entity.UserTotalPointsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Author Lethekk
 * @Date 2026/4/24 14:08
 */
public interface UserTotalPointsMapper extends BaseMapper<UserTotalPointsEntity> {

    int insertOrUpdatePoint(UserTotalPointsEntity entity);

}
