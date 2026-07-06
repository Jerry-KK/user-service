package cn.lethekk.userservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @Author Lethekk
 * @Date 2026/7/6 22:58
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary // 确保这成为主要的 ObjectMapper，覆盖 Spring Boot 默认的
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. 注册 JSR310 时间模块（处理 LocalDateTime 等）
        objectMapper.registerModule(new JavaTimeModule());
        // 2. 常用安全配置：反序列化时，遇到不认识的字段不报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 3. 选配：序列化时，Date/LocalDateTime 不转成时间戳，而是标准的 ISO 格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}