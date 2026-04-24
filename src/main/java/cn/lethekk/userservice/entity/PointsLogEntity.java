package cn.lethekk.userservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Lethekk
 * @Date 2026/4/24 21:19
 */
@Builder
@Data
@TableName("points_log")
public class PointsLogEntity {
    @TableId("id")
    private Long id;
    private Long userId;
    private Integer type;
    private Integer points;
    private LocalDateTime time;
}
