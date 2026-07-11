package cn.lethekk.userservice.repository.msg;

import cn.lethekk.userservice.entity.MsgDeduplicationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;

/**
 * @Author Lethekk
 * @Date 2026/7/11 15:39
 */
public interface MsgDeduplicationMapper extends BaseMapper<MsgDeduplicationEntity> {

    @Insert("INSERT IGNORE INTO `msg_deduplicate` (id) VALUES (#{id})")
    int insertIgnore(Long id);

}