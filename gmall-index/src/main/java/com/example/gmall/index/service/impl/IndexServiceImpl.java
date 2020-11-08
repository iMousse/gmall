package com.example.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.example.gmall.index.annotation.GmallCache;
import com.example.gmall.index.client.GmallPmsClient;
import com.example.gmall.index.service.IndexService;
import com.example.gmall.pms.entity.CategoryEntity;
import com.example.gmall.pms.vo.CategoryVO;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    private static final String KEY_PREFIX = "index:cates:";
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redisson;

    @Override
    public List<CategoryEntity> queryCate1() {
        Resp<List<CategoryEntity>> listResp = this.pmsClient.queryByCategoriesByPidOrLevel(1, null);
        return listResp.getData();
    }


    /**
     * <p>缓存雪崩:大量缓存同时失效，导致大量请求访问MySQL数据库。解决方法：过期时间 + 随机值</p>
     * <p>缓存穿透:大量的请求同时访问不存在的数据。解决方法：把查询结果为null的数据也放入缓存</p>
     * <p>缓存击穿:有一个key缓存中没有，大量请求同时访问该key对应的数据。解决方法：分布式锁</p>
     */
    @Override
    @GmallCache(prefix = KEY_PREFIX, timeout = 5, random = 100)
    public List<CategoryVO> querySubCategories(Long pid) {

        Resp<List<CategoryVO>> listResp = this.pmsClient.querySubCategories(pid);

        return listResp.getData();
    }


    public List<CategoryVO> querySubCategories2(Long pid) {

        //判断缓存中是否有数据
        String cateJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);

        //有缓存直接返回
        if (StringUtils.isNotEmpty(cateJson)) {
            return JSON.parseArray(cateJson, CategoryVO.class);
        }


        //添加分布式锁，如果来了1000个请求
        //pid是来锁住一个分类有1000个请求
        RLock lock = this.redisson.getLock("lock" + pid);
        lock.lock();

        //第一个请求没有缓存，数据去mysql查询放入了缓存，剩下的9999请求来查缓存
        String cateJson2 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotEmpty(cateJson2)) {
            lock.unlock();
            return JSON.parseArray(cateJson2, CategoryVO.class);
        }

        //没有缓存查询完后放入缓存,解决穿透也解决了
        Resp<List<CategoryVO>> listResp = this.pmsClient.querySubCategories(pid);
        List<CategoryVO> categoryVOS = listResp.getData();
        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryVOS));

        //释放锁
        lock.unlock();
        return categoryVOS;
    }


    /**
     * 服务器在分布式的情况下会出错
     */
    @Override
    public synchronized void testLock() {
        //查询redis中的num值
        String value = this.redisTemplate.opsForValue().get("num");
        //没有该值则返回return
        if (StringUtils.isBlank(value)) {
            return;
        }

        //有值就转化成int并+1
        int num = Integer.parseInt(value);
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
    }

    /**
     * 分布式锁的实现原理，多个服务器只能获取一个唯一的key值，来对key是否获取来进行判断
     * <p>满足分布式锁的四个条件</p>
     * 1.互斥锁，在任何事件只有一个客户端能持有锁
     * 2.不会发生死锁。即在一个客户端在持有锁的期间崩溃而没有主动解锁，也能保证后续其他客户端能加锁
     * 3.加锁和解锁必须是同一个客户端，客户端不能把别人加的锁给解了
     * 4.加锁和解锁必须具有原子性
     * <p>redis集群下的问题</p>
     * 客户端A从master获取到锁，在master将锁同步到slave之前，master宕机了，slave节点成为了master节点，
     * 客户端B取得了同一个资源被客户端A已经获取到另外一个锁
     */
    @Override
    public  void testLock2() {
        String uuid = UUID.randomUUID().toString();
        //设置锁需要原子性
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 5, TimeUnit.SECONDS);
        if (lock) {
            String numStr = this.redisTemplate.opsForValue().get("num");

            //没有该值则返回return
            if (StringUtils.isBlank(numStr)) {
                return;
            }

            //有值就转化成int并+1
            int num = Integer.parseInt(numStr);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

            //释放锁 del 使用lua脚本来实现原子操作
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"), Arrays.asList(uuid));


            //释放锁 del没有原子操作
           /* if (StringUtils.endsWith(redisTemplate.opsForValue().get("lock"), uuid)) {
                this.redisTemplate.delete("lock");
            }*/
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 使用Redisson
     */
    @Override
    public void testRedisson() {
        RLock lock = this.redisson.getLock("lock");//只要锁的名称相同就是同一把锁
        lock.lock();//加锁

        //查询redis中的num值
        String value = this.redisTemplate.opsForValue().get("num");
        //没有该值则返回return
        if (StringUtils.isBlank(value)) {
            return;
        }

        //有值就转化成int并+1
        int num = Integer.parseInt(value);
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

        lock.unlock();//解锁

    }

    @Override
    public String readLock() {
        //初始化读写锁,读写锁要保证锁名字的一致
        RReadWriteLock readwriteLock = redisson.getReadWriteLock("readwriteLock");
        //获取读锁
        RLock rLock = readwriteLock.readLock();
        //10s后自动释放
        rLock.lock(10, TimeUnit.SECONDS);
//        rLock.unlock();

        return this.redisTemplate.opsForValue().get("msg");
    }

    @Override
    public String testWrite() {
        //初始化读写锁,读写锁要保证锁名字的一致
        RReadWriteLock readwriteLock = redisson.getReadWriteLock("readwriteLock");
        //获取写锁
        RLock rLock = readwriteLock.writeLock();
        //10s后自动释放
        rLock.lock(10, TimeUnit.SECONDS);
//        rLock.unlock();
        return "成功写入了内容";
    }

    @Override
    public String testLatch() {
        RCountDownLatch countdownLatch = this.redisson.getCountDownLatch("countdownLatch");
        try {
            String count = this.redisTemplate.opsForValue().get("count");
            countdownLatch.trySetCount(Integer.parseInt(count));
            countdownLatch.await();

            return "关门了";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "开门了";
    }

    @Override
    public String testCount() {
        RCountDownLatch countdownLatch = redisson.getCountDownLatch("countdownLatch");
        String count = this.redisTemplate.opsForValue().get("count");
        int i = Integer.parseInt(count);
        this.redisTemplate.opsForValue().set("count", String.valueOf(--i));
        countdownLatch.countDown();
        return "还剩下" + count + "人";
    }


}
