package cn.lethekk.userservice.dao.checkin;

import cn.lethekk.userservice.model.po.UserTotalPointsPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Author Lethekk
 * @Date 2026/4/24 14:08
 */
public interface UserTotalPointsMapper extends BaseMapper<UserTotalPointsPO> {

    int insertOrUpdatePoint(UserTotalPointsPO entity);

}
