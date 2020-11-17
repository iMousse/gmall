package com.example.gmall.wms.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    /**
     * 返回一个延时队列
     *
     * @return
     */
    @Bean("WMS-TTL-QUEUE")
    public Queue ttlQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", "GMALL-ORDER-EXCHANGE");
        map.put("x-deal-letter-routing-key", "stock.unlock");
        map.put("x-message-ttl", 30 * 60 * 1000);
        return new Queue("WMS-TTL-QUEUE", true, false, false, map);
    }

    /**
     * 延时队列绑定到交换机
     *
     * @return
     */
    @Bean("WMS-TTL-BINDING")
    public Binding ttlBinding() {
        return new Binding("WMS-TTL-QUEUE", Binding.DestinationType.QUEUE, "GMALL-ORDER-EXCHANGE", "stock.ttl", null);
    }



}
