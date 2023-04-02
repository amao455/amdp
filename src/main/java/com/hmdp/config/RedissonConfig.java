package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: RedissonConfig
 * Description:
 *
 * @author MQW
 * @date 2023/4/1 11:24
 */

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://101.37.124.201:6379").setPassword("123321");

        // 创建RedissonClient对象
        return Redisson.create(config);

    }
}
