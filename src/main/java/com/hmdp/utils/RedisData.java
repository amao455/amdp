package com.hmdp.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 封装基于逻辑过期方式来解决缓存击穿问题的实体
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
