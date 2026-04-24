package cn.lethekk.userservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author Lethekk
 * @Date 2026/4/24 21:32
 */
@Data
@Builder
@TableName("check_in_days")
public class CheckInDaysEntity {
    @TableId("user_id")
    private Long userId;
    private Integer days;
    private LocalDate lastDate;
    private LocalDateTime updateTime;
}
