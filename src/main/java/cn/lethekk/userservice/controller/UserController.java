package cn.lethekk.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Lethekk
 * @Date 2025/9/26 13:47
 */
@RequestMapping("/user")
@RestController
public class UserController {

    @GetMapping("/getUserName")
    public String getUserNameById(@RequestParam("id") int id) {
        System.out.println("被调用:" + id);
        return id + "名字" + System.currentTimeMillis();
    }
}
