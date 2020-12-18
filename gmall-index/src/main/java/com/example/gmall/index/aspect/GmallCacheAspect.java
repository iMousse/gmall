package com.example.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.example.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 环绕通知的4个必要条件
     *      1.返回值object
     *      2.参数proceedingJoinPoint
     *      3.抛出异常Throwable
     *      4.proceedingJoinPoint.proceed(args)执行业务方法
     * @param point
     * @return
     */
    @Around("@annotation(com.example.gmall.index.annotation.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) throws Throwable {
        Object result = null;

        //获取连接点签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        //获取连接点的GmallCache注解信息
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
        //获取缓存的前缀
        String prefix = gmallCache.prefix();
        //组装成key
        String key = prefix + Arrays.asList(point.getArgs().toString());

        result = this.cacheHit(signature, key);
        if (result != null) {
            return result;
        }

        //初始化分布式锁并加锁防止缓存穿透
        RLock lock = this.redissonClient.getLock("gmallCache");
        lock.lock();

        //再次检查内存是否存在，因为在高并发下，可能在加锁这段时间内，已有其他线程放入缓存
        result = this.cacheHit(signature, key);
        if (result != null) {
            lock.unlock();
            return result;
        }

        //执行查询的业务路径
        result = point.proceed(point.getArgs());
        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result));

        lock.unlock();
        return result;
    }

    /**
     * 查询缓存的方法
     *
     * @param signature
     * @param key
     * @return
     */
    private Object cacheHit(MethodSignature signature, String key) {
        String cache = this.redisTemplate.opsForValue().get(key);
        //如果cache不为空则反序列化并返回
        if (StringUtils.isNotBlank(cache)) {
            //获取方法返回类型
            Class returnType = signature.getReturnType();
            return JSON.parseObject(cache, returnType);
        }
        return null;
    }
}
