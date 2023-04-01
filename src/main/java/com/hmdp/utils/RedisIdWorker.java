package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * ClassName: RedisIdWorker
 * Description:
 * 利用Redis实现全局唯一ID
 * 优点：
 *  每天一个key，方便统计订单量
 *
 * @author MQW
 * @date 2023/3/30 17:51
 */
@Component
public class RedisIdWorker {

    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 开始的时间戳
    private static final long Begin_TIMESTAMP = 1640995200;

    // 序列号位数
    private static final int COUNT_BITS = 32;

    public long nextId(String keyPrefix){

        // 1 生成时间戳
        long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - Begin_TIMESTAMP;

        // 2 生成序列号
        // 2.1 获取当前你日期，精确到天
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 2.2 自增长
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3 拼接并返回
        return timestamp << COUNT_BITS | count;
    }

    public static void main(String[] args){
        LocalDateTime time = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println("second = " + second);

    }
}
