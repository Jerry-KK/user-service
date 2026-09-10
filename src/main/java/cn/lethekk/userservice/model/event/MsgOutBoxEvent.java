package cn.lethekk.userservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Lethekk
 * @Date 2026/6/10 1:23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsgOutBoxEvent {
    private Long id;
    private String label;
    private String payload;
    private Integer state;
}
