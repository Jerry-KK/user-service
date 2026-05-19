package cn.lethekk.userservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 */
@Configuration
public class RabbitMqConfig {

    public static final String POINTS_EXCHANGE = "points.exchange";
    public static final String POINTS_QUEUE = "points.queue";
    public static final String POINTS_ROUTING_KEY = "points.add";

    @Bean
    public DirectExchange pointsExchange() {
        return new DirectExchange(POINTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue pointsQueue() {
        return new Queue(POINTS_QUEUE, true);
    }

    @Bean
    public Binding pointsBinding(Queue pointsQueue, DirectExchange pointsExchange) {
        return BindingBuilder.bind(pointsQueue)
                .to(pointsExchange)
                .with(POINTS_ROUTING_KEY);
    }
}
