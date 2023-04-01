package com.hmdp.utils;

/**
 * ClassName: ILock
 * Description:
 *
 * @author MQW
 * @date 2023/3/31 16:45
 */
public interface ILock {
    /**
     * 尝试获取锁
     * @param timeoutSec 锁持有的超时时间，过期后自动释放
     * @return true代表获取锁成功；false代表获取锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unLock();
}
