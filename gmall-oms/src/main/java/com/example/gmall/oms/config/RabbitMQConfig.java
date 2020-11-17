package com.example.gmall.oms.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    /**
     * 返回一个延时队列
     *
     * @return
     */
    @Bean("ORDER-TTL-QUEUE")
    public Queue ttlQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", "GMALL-ORDER-EXCHANGE");
        map.put("x-deal-letter-routing-key", "order.dead");
        map.put("x-message-ttl", 30 * 60 * 1000);
        return new Queue("ORDER-TTL-QUEUE", true, false, false, map);
    }

    /**
     * 延时队列绑定到交换机
     *
     * @return
     */
    @Bean("ORDER-TTL-BINDING")
    public Binding ttlBinding() {
        return new Binding("ORDER-TTL-QUEUE", Binding.DestinationType.QUEUE, "GMALL-ORDER-EXCHANGE", "order.ttl", null);
    }

    /**
     * 死信队列
     * @return
     */
    @Bean("ORDER-DEAD-QUEUE")
    public Queue queue() {
        return new Queue("ORDER-DEAD-QUEUE", true, false, false, null);
    }

    /**
     * 死信队列绑定到交换机
     * @return
     */
    @Bean("ORDER-DEAD-BINDING")
    public Binding closeBinding() {
        return new Binding("ORDER-DEAD-QUEUE", Binding.DestinationType.QUEUE, "GMALL-ORDER-EXCHANGE", "order.dead", null);
    }

}
