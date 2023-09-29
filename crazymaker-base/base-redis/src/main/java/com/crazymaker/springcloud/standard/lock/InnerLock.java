package com.crazymaker.springcloud.standard.lock;

import com.crazymaker.springcloud.common.exception.BusinessException;
import com.crazymaker.springcloud.standard.context.SpringContextUtil;
import com.crazymaker.springcloud.standard.lua.ScriptHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InnerLock {

    private RedisTemplate redisTemplate;

    public static final Long LOCKED = Long.valueOf(1);
    String key;
    String requestId;  // lockValue 锁的value ,代表线程的uuid

    /**
     * 默认为2000ms
     */
    long expire = 2000L;
    private volatile boolean isLocked = false;
    private RedisScript lockScript;
    private RedisScript unLockScript;

    public InnerLock(String lockKey, String requestId) {
        this.key = lockKey;
        this.requestId = requestId;
        this.lockScript = ScriptHolder.getLockScript();
        this.unLockScript = ScriptHolder.getUnLockScript();
    }

    /**
     * 抢夺锁
     */
    public void lock() {
        if (null == key) {
            return;
        }
        try {
            List<String> redisKeys = new ArrayList<>();
            redisKeys.add(key);
            redisKeys.add(requestId);
            redisKeys.add(String.valueOf(expire));
            getRedisTemplate().execute(lockScript, redisKeys);
            isLocked = false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException().builder().errMsg("抢锁失败").build();
        }
    }

    /**
     * 有返回值的抢夺锁
     */
    public boolean lock(Long millisToWait) {
        if (null == key) {
            return false;
        }
        try {
            List<String> redisKeys = new ArrayList<>();
            redisKeys.add(key);
            redisKeys.add(requestId);
            redisKeys.add(String.valueOf(millisToWait));
            Long res = (Long) getRedisTemplate().execute(lockScript, redisKeys);
            return res != null && res.equals(LOCKED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException().builder().errMsg("抢锁失败").build();
        }
    }

    //释放锁
    public void unlock() {
        if (key == null || requestId == null) {
            return;
        }
        try {
            List<String> redisKeys = new ArrayList<>();
            redisKeys.add(key);
            redisKeys.add(requestId);
            getRedisTemplate().execute(unLockScript, redisKeys);
        } catch (Exception e) {
            e.printStackTrace();
            throw BusinessException.builder().errMsg("释放锁失败").build();
        }
    }

    private RedisTemplate getRedisTemplate() {
        if (null == redisTemplate) {
            redisTemplate = (RedisTemplate) SpringContextUtil.getBean("stringRedisTemplate");
        }
        return redisTemplate;
    }

}
