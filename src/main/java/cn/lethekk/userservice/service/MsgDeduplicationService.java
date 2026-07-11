package cn.lethekk.userservice.service;

import cn.lethekk.userservice.dto.CheckInMessage;
import cn.lethekk.userservice.repository.msg.MsgDeduplicationMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Author Lethekk
 * @Date 2026/7/11 18:29
 */
@Service
@AllArgsConstructor
@Slf4j
public class MsgDeduplicationService {

    private final CheckInService checkInService;
    private final MsgDeduplicationMapper msgDeduplicationMapper;

    @Transactional
    public void consumeWithNoRepeated(CheckInMessage message) {
        //1 尝试插入去幂等表
        int rowNum = msgDeduplicationMapper.insertIgnore(message.getId());
        if (rowNum == 0) {
            //重复消息
            return;
        }
        //2 执行业务
        checkInService.addPoints(message.getUserId(), message.getDateTime());
    }

}
