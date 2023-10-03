package com.crazymaker.redission.demo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 针对源码 Redisson进行一层封装
 *
 * @author liudawei
 * @date 2023/10/3
 **/
@Slf4j
@Component
@Data
public class RedissonLock {
    private RedissonManager redissonManager;
    private Redisson redisson;

    public RedissonLock() {
    }

    public RedissonLock(RedissonManager redissonManager) {
        this.redissonManager = redissonManager;
        this.redisson = redissonManager.getRedisson();
    }

    /**
     * 加锁操作 （设置锁的有效时间）
     * @param lockName 锁名称
     * @param leaseTime 锁有效时间
     */
    public void lock(String lockName,long leaseTime) {
        RLock rLock = redisson.getLock(lockName);
        rLock.lock(leaseTime, TimeUnit.SECONDS);
    }

    /**
     * 加锁操作 （锁有效时间采用默认时间30秒）
     * @param lockName 锁名称
     */
    public void lock(String lockName) {
        RLock rLock = redisson.getLock(lockName);
        rLock.lock();
    }

    /**
     * 加锁操作(tryLock锁，没有等待时间）
     * @param lockName 锁名称
     * @param leaseTime 锁有效时间
     * @return
     */
    public boolean tryLock(String lockName,long leaseTime) {
        RLock rLock = redisson.getLock(lockName);
        try {
            return rLock.tryLock(leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("获取Redisson分布式锁[异常]，lockName=" + lockName,e);
            return false;
        }
    }

    /**
     * 加锁操作(tryLock锁，没有等待时间）
     * @param lockName 锁名称
     * @param leaseTime 锁有效时间
     * @param waitTime 等待时间
     * @return
     */
    public boolean tryLock(String lockName,long leaseTime,long waitTime) {
        RLock rLock = redisson.getLock(lockName);
        try {
            return rLock.tryLock(waitTime,leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("获取Redisson分布式锁[异常]，lockName=" + lockName,e);
            return false;
        }
    }

    /**
     * 解锁
     * @param lockName 锁名称
     */
    public void unlock(String lockName) {
        redisson.getLock(lockName).unlock();
    }

    /**
     * 判断该锁是否已经被线程持有
     * @param lockName 锁名称
     * @return
     */
    public boolean isLock(String lockName) {
        return redisson.getLock(lockName).isLocked();
    }

    /**
     * 判断该线程是否持有当前锁
     * @param lockName 锁名称
     * @return
     */
    public boolean isHeldByCurrentThread(String lockName){
        return redisson.getLock(lockName).isHeldByCurrentThread();
    }

}
