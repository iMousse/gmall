package com.example.gmall.oms.listener;

import com.example.gmall.oms.dao.OrderDao;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private AmqpTemplate amqpTemplate;


    @RabbitListener(queues = {"ORDER-DEAD-BINDING"})
    public void closeOrder(String orderToken) {
        if (this.orderDao.closeOrder(orderToken) == 1) {
            //解锁库存
            this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE", "stock.unlock", orderToken);
        }

    }
}
