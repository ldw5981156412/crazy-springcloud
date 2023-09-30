package com.crazymaker.springcloud.distribute.lock;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;

/**
 * @author liudawei
 * @date 2023/9/30
 **/
public interface LockService {
    /**
     * 取得ZK 的分布式锁
     *
     * @param key 锁的key
     * @return ZK 的分布式锁
     */
    InterProcessMutex getZookeeperLock(String key);
}
