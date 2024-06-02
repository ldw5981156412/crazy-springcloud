package com.crazymaker.springcloud.standard.ratelimit;

import com.crazymaker.springcloud.common.distribute.rateLimit.RateLimitService;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

/**
 * 实现：令牌桶限流服务
 */
@Slf4j
public class RedisRateLimitImpl implements RateLimitService, InitializingBean {
    /**
     * 限流器的redis key 前缀
     */
    private static final String RATE_LIMITER_KEY_PREFIX = "rate_limiter:";

    /**
     * 限流器的信息
     */
    @Builder
    @Data
    public static class LimiterInfo {
        /**
         * 限流器的key，如：秒杀的id
         */
        private String key;

        /**
         * 限流器的类型，如：seckill
         */
        private String type = "default";

        /**
         * 限流器的最大桶容量
         */
        private Integer maxPermits;
        /**
         * 限流器的速率
         */
        private Integer rate;

        /**
         * 限流器的redis key
         */
        public String fullKey() {
            return RATE_LIMITER_KEY_PREFIX + type + ":" + key;
        }

        /**
         * 限流器的在map中的缓存key
         */
        public String cashKey() {
            return type + ":" + key;
        }
    }

    @Override
    public Boolean tryAcquire(String cacheKey) {
        return null;
    }

    @Override
    public void initLimitKey(String type, String key, Integer maxPermits, Integer rate) {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
