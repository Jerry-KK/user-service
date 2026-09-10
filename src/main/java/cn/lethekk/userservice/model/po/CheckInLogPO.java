package cn.lethekk.userservice.model.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author Lethekk
 * @Date 2026/4/24 14:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("check_in_log")
public class CheckInLogPO {
    @TableId("id")
    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalDateTime time;

}
