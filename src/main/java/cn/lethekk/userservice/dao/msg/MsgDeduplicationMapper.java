package cn.lethekk.userservice.dao.msg;

import cn.lethekk.userservice.model.po.MsgDeduplicationPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;

/**
 * @Author Lethekk
 * @Date 2026/7/11 15:39
 */
public interface MsgDeduplicationMapper extends BaseMapper<MsgDeduplicationPO> {

    @Insert("INSERT IGNORE INTO `msg_deduplicate` (id) VALUES (#{id})")
    int insertIgnore(Long id);

}