package cn.lethekk.userservice.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author Lethekk
 * @Date 2026/4/24 14:05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInLog {
    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalDateTime time;
}
