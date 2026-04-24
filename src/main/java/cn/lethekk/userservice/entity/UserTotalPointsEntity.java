package cn.lethekk.userservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Lethekk
 * @Date 2026/4/24 20:21
 */
@Data
@Builder
@TableName("user_total_points")
public class UserTotalPointsEntity {
    @TableId("user_id")
    private Long userId;
    private Integer totalPoints;
    private LocalDateTime updateTime;
}
