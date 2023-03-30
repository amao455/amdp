package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import io.lettuce.core.dynamic.annotation.CommandNaming;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.*;

/**
 * ClassName: CacheClient
 * Description: 封装redis工具类
 *
 * @author MQW
 * @date 2023/3/30 15:15
 */


@Slf4j
@Component
public class CacheClient {
    private StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 将任意Java对象序列化为json并存储在string类型的key中，并且设置TTL过期时间
    public void set(String key, Object object, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(object), time, unit);
    }

    // 将任意java对象序列化为json并存储在string类型的key中，并且设置逻辑缓存时间，用于处理缓存击穿问题
    public void setWithLogicalExpire(String key, Object object, Long time, TimeUnit unit){
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(object);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));

        // 写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    // 根据指定的key查询缓存，并反序列为指定类型，利用缓存空值的方式解决缓存穿透问题
    public <R, ID> R  queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback,
                                           Long time, TimeUnit unit) {

        String key = keyPrefix + id;
        // 1 从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3 存在，直接返回
            return JSONUtil.toBean(json, type);
        }

        // 判断命中的是否是空值（第二次查询时，可以查到value，所以shopJson != null）
        if (json != null) {
            // 返回一个错误消息
            return null;
        }

        // 4 不存在，根据id查询数据库
        R r = dbFallback.apply(id);

        // 5 不存在，返回错误
        if (r == null) {
            // 解决缓存穿透问题（将空值写入redis）
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }

        // 6 存在，写入redis
        this.set(key, r, time, unit);
        // 7 返回
        return r;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    // 根据指定的key查询缓存，并反序列化为指定类型，需要利用逻辑过期解决缓存击穿问题
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback,
                                            Long time, TimeUnit unit) {

        String key = keyPrefix + id;

        // 1 从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2 判断是否存在
        // 3 未命中，直接返回
        if (StrUtil.isBlank(json)) {
            return null;
        }

        // 4 命中,需要先把redis反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        // TODO 理解为何这样转换
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 5 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 未过期，直接返回店铺信息
            return r;
        }

        // 5.2 已过期，需要缓存重建
        // 6 缓存重建
        // 6.1 获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);

        // 6.2 判断是否获取锁成功
        if (isLock) {
            // TODO 6.3 成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 重建缓存
                    // (1) 查询数据库
                    R r1 = dbFallback.apply(id);
                    // (2) 写入Redis
                    this.setWithLogicalExpire(key, r1, time, unit);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unLock(lockKey);
                }
            });
        }

        // 6.4 返回过期的商铺信息
        return r;
    }


    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.MINUTES);
        // TODO 拆箱的过程中为何有可能是null
        return BooleanUtil.isTrue(flag);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }


}
