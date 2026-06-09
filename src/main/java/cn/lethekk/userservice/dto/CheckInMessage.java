package cn.lethekk.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户签到消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private LocalDateTime dateTime;
}
