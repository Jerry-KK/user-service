package cn.lethekk.userservice.dao.msg;


import cn.lethekk.userservice.model.po.MsgOutBoxPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Author Lethekk
 * @Date 2026/6/10 1:27
 */
public interface MsgOutBoxMapper  extends BaseMapper<MsgOutBoxPO> {

    List<MsgOutBoxPO> selectListByState(Integer state);

}
