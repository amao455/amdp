package com.hmdp.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * ClassName: RedisIdWorker
 * Description:
 *
 * @author MQW
 * @date 2023/3/30 17:51
 */
public class RedisIdWorker {

    // 开始的时间戳
    private static final long Begin_TIMESTAMP = 1640995200;

    public long nextId(String keyPrefix){

        // 1 生成时间戳

        // 2 生成序列号

        // 3 拼接并返回

        return 0;
    }

    public static void main(String[] args){
        LocalDateTime time = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println("second = " + second);

    }
}
