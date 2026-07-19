package cn.lethekk.userservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author Lethekk
 * @Date 2026/6/10 1:34
 */
@Component
public class JsonUtils {
    private static ObjectMapper OBJECT_MAPPER;

    // 通过构造函数或 @Autowired 注入 Spring 容器中的全局 ObjectMapper
    @Autowired
    public JsonUtils(ObjectMapper objectMapper) {
        JsonUtils.OBJECT_MAPPER = objectMapper;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        OBJECT_MAPPER = objectMapper;
    }

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }
}
