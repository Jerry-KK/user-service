package cn.lethekk.userservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * @Author Lethekk
 * @Date 2026/6/10 1:23
 */
@Builder
@Data
@TableName("msg_out_box")
public class MsgOutBoxEntity {
    @TableId("id")
    private Long id;
    private String label;
    private String payload;
    private Integer state;
}
