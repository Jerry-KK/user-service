package cn.lethekk.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分任务消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPointsMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private LocalDateTime dateTime;
}
