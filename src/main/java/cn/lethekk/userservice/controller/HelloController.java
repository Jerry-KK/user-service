package cn.lethekk.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Lethekk
 * @Date 2025/9/25 22:54
 */
@RequestMapping("/test")
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        return "hello " + name + "! by user-server";
    }
}
