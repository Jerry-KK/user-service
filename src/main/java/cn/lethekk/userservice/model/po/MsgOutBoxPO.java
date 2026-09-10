package cn.lethekk.userservice.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Lethekk
 * @Date 2026/6/10 1:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("msg_out_box")
public class MsgOutBoxPO {
    @TableId("id")
    private Long id;
    private String label;
    private String payload;
    private Integer state;
}
