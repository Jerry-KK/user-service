package cn.lethekk.userservice.repository.msg;


import cn.lethekk.userservice.entity.MsgOutBoxEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Author Lethekk
 * @Date 2026/6/10 1:27
 */
public interface MsgOutBoxMapper  extends BaseMapper<MsgOutBoxEntity> {

    List<MsgOutBoxEntity> selectListByState(Integer state);

}
