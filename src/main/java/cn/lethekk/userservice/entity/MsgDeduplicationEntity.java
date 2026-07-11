package cn.lethekk.userservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author Lethekk
 * @Date 2026/7/11 15:09
 */
@AllArgsConstructor
@Data
@TableName("msg_deduplicate")
public class MsgDeduplicationEntity {
    @TableId("id")
    private Long id;

}
