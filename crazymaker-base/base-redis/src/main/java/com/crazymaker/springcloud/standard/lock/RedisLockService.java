package com.crazymaker.springcloud.standard.lock;

import com.crazymaker.springcloud.common.util.UUIDUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

@Slf4j
@Data
@Service
public class RedisLockService {

    private static final ThreadLocal<String> REQUEST_ID = ThreadLocal.withInitial(UUIDUtil::uuid);

    //分段锁的 默认分段
    public static final int SEGMENT_DEFAULT = 10;

    public RedisLockService() {
    }

    //获取锁
    public Lock getLock(String lockKey, String requestId) {
        return new JedisLock(lockKey, requestId);
    }

    //获取分段锁
    public JedisMultiSegmentLock getSegmentLock(String lockKey, String requestId, int segAmount) {
        return new JedisMultiSegmentLock(lockKey, requestId, segAmount);
    }

    public static String getDefaultRequestId() {
        return REQUEST_ID.get();
    }
}
