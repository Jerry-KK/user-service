package cn.lethekk.userservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author Lethekk
 * @Date 2026/4/24 14:05
 */
@Builder
@Data
@TableName("check_in_log")
public class CheckInLogEntity {
    @TableId("id")
    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalDateTime time;

}
