package com.crazymaker.springcloud.standard.ratelimit;

import com.crazymaker.springcloud.common.distribute.rateLimit.RateLimitService;
import com.crazymaker.springcloud.standard.lua.ScriptHolder;
import com.crazymaker.springcloud.standard.properties.RedisRateLimitProperties;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现：令牌桶限流服务
 */
@Slf4j
public class RedisRateLimitImpl implements RateLimitService, InitializingBean {
    /**
     * 限流器的redis key 前缀
     */
    private static final String RATE_LIMITER_KEY_PREFIX = "rate_limiter:";
    public static final String RATE_LIMITER_LUA_SHA_1 = RATE_LIMITER_KEY_PREFIX + "sha1";

    private RedisRateLimitProperties redisRateLimitProperties;

    private RedisTemplate redisTemplate;
    //lua 脚本的实例
    private static RedisScript<Long> rateLimiterScript = null;

    public RedisRateLimitImpl(RedisRateLimitProperties redisRateLimitProperties, RedisTemplate redisTemplate) {
        this.redisRateLimitProperties = redisRateLimitProperties;
        this.redisTemplate = redisTemplate;
        rateLimiterScript = ScriptHolder.getRateLimitScript();
    }

    private Map<String, LimiterInfo> limiterInfoMap = new HashMap<>();

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

    /**
     * 限流检测：是否超过 redis 计数限制器的限制
     *
     * @param cacheKey 限流的key，如：秒杀的类型和id seckill:10000
     * @return true: 超过限制，不能继续执行；false: 未超过限制，可以继续执行
     */
    @Override
    public Boolean tryAcquire(String cacheKey) {
        if (cacheKey == null) {
            return true;
        }
        if (cacheKey.indexOf(":") <= 0) {
            cacheKey = "default" + cacheKey;
        }
        LimiterInfo limiterInfo = limiterInfoMap.get(cacheKey);
        if (limiterInfo == null) {
            return true;
        }
        Long acquire = (Long) redisTemplate.execute(rateLimiterScript, ImmutableList.of(limiterInfo.fullKey()), "acquire", 1);
        //1:令牌获取成功 , -1:令牌获取失败
        if (acquire == 1) {
            return false;
        }
        return true;
    }

    /**
     * 限流器初始化
     * @param type       类型
     * @param key        id
     * @param maxPermits 上限
     * @param rate       速度
     */
    @Override
    public void initLimitKey(String type, String key, Integer maxPermits, Integer rate) {
        LimiterInfo limiterInfo = LimiterInfo.builder().type(type).key(key).maxPermits(maxPermits).rate(rate).build();
        initLimitKey(limiterInfo);
    }

    /**
     * 重载方法：限流器初始化
     * @param limiterInfo
     */
    public void initLimitKey(LimiterInfo limiterInfo) {
        if (limiterInfo == null) {
            return;
        }
        String maxPermits = limiterInfo.getMaxPermits().toString();
        String rate = limiterInfo.getRate().toString();
        Long init = (Long) redisTemplate.execute(rateLimiterScript, ImmutableList.of(limiterInfo.fullKey()), "init", maxPermits, rate);
        if (init == 1) {
            limiterInfoMap.put(limiterInfo.cashKey(), limiterInfo);
        }
        /**
         * 缓存秒杀 lua 的sha1 编码，方便在其他地方获取
         */
        cacheRateLimiterSha1();
    }

    /**
     * 获取 redis lua 脚本的 sha1 编码,并缓存到 redis
     */
    public String cacheRateLimiterSha1(){
        String sha1 = rateLimiterScript.getSha1();
        redisTemplate.opsForValue().set(RATE_LIMITER_LUA_SHA_1, sha1);
        return sha1;
    }

    /**
     * 加载配置文件中的限速配置
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            List<LimiterInfo> limiterInfos = redisRateLimitProperties.getLimiterInfos();
            if (null == limiterInfos){
                return;
            }
            limiterInfos.forEach(limiterInfo -> {
                initLimitKey(limiterInfo);
            });
            log.info("redis rate limit inited !");
        } catch (Exception e) {
            log.error("redis rate limit  error.....",e);
        }
    }
}
