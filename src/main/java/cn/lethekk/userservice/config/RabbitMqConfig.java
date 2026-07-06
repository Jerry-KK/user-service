package cn.lethekk.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 */
@Configuration
public class RabbitMqConfig {

    // ==========================================
    // 1. 全局基础设施常量 (其他微服务/模块也可以共用此定义)
    // ==========================================
    public static final String EVENT_EXCHANGE = "event.exchange";

    // ==========================================
    // 2. 积分模块 (Points Module) 专属常量
    // ==========================================
    public static final String POINTS_QUEUE = "points.queue";
    /**
     * 积签到事件路由键 / 积分队列绑定键
     * 暂时保持精准匹配。发送端以该 Key 发出，积分队列以该 Key 监听绑定。
     * 未来业务扩展（如增加购买 user.purchase）时，可在此扩展规则或将绑定键调整为 "user.#"
     */
    public static final String USER_CHECKIN_KEY = "user.checkin";

    // ==========================================
    // 3. MQ 拓扑结构配置 (Exchange, Queue, Binding)
    // ==========================================

    /**
     * 全局事件交换机（核心：使用 Topic 类型兼容未来多视角广播场景）
     */
    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange (EVENT_EXCHANGE, true, false);
    }

    /**
     * 积分模块专用队列
     */
    @Bean
    public Queue pointsQueue() {
        return new Queue(POINTS_QUEUE, true);
    }

    /**
     * 绑定规则：将积分队列精准绑定到全局交换机的签到事件上
     */
    @Bean
    public Binding pointsBinding(Queue pointsQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(pointsQueue)
                .to(eventExchange)
                .with(USER_CHECKIN_KEY);
    }

    // ==========================================
    // 4. 全局序列化与模板配置
    // ==========================================

    /**
     * 使用 Spring 容器中统一的 ObjectMapper
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 发送端模板
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    /**
     * 消费端监听器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
