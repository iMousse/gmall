package com.example.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.example.gmall.wms.dao.WareSkuDao;
import com.example.gmall.wms.vo.SkuLockVO;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class WareListener {

    private static final String ORDER_PREFIX_KEY = "stock:lock:";
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private WareSkuDao wareSkuDao;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "WMS-UNLOCK-QUEUE", durable = "true"),
            exchange = @Exchange(value = "GMALL-ORDER-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.unlock"}
    ))
    public void unlockListener(String orderToken) {
        String lockJson = this.redisTemplate.opsForValue().get(ORDER_PREFIX_KEY + orderToken);
        List<SkuLockVO> lockVOS = JSON.parseArray(lockJson, SkuLockVO.class);
        if (!CollectionUtils.isEmpty(lockVOS)) {
            lockVOS.forEach(skuLockVO -> {
                //根据库存id和数量解锁库存
                this.wareSkuDao.unLockStore(skuLockVO.getWareSkuId(), skuLockVO.getCount());
            });
        }
    }
}
