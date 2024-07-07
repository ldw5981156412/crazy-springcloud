package com.crazymaker.springcloud.standard.config;

import com.crazymaker.springcloud.standard.properties.RedisRateLimitProperties;
import com.crazymaker.springcloud.standard.ratelimit.RedisRateLimitImpl;
import com.crazymaker.springcloud.standard.redis.RedisRepository;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置.
 */
@EnableCaching
@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
@EnableConfigurationProperties(RedisRateLimitProperties.class)
@ConditionalOnClass({RedisTemplate.class})
public class CustomedRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({RedisTemplate.class})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        /*
         * Redis 序列化器.
         *
         * RedisTemplate 默认的系列化类是 JdkSerializationRedisSerializer,用JdkSerializationRedisSerializer序列化的话,
         * 被序列化的对象必须实现Serializable接口。在存储内容时，除了属性的内容外还存了其它内容在里面，总长度长，且不容易阅读。
         *
         * Jackson2JsonRedisSerializer 和 GenericJackson2JsonRedisSerializer，两者都能系列化成 json，
         * 但是后者会在 json 中加入 @class 属性，类的全路径包名，方便反系列化。前者如果存放了 List 则在反系列化的时候如果没指定
         * TypeReference 则会报错 java.util.LinkedHashMap cannot be cast to
         */
        RedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        RedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // 定义RedisTemplate，并设置连接工程
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        // key 的序列化采用 StringRedisSerializer
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // value 值的序列化采用 GenericJackson2JsonRedisSerializer
        //TODO
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        // 设置连接工厂
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setEnableTransactionSupport(false);
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean({StringRedisTemplate.class})
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setEnableTransactionSupport(false);
        return redisTemplate;
    }

    @Bean
    public CacheManager initRedisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager.RedisCacheManagerBuilder redisCacheManagerBuilder =
                RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory);
        return redisCacheManagerBuilder.build();
    }

    @Bean
    public RedisRepository redisRepository(RedisTemplate<Object, Object> redisTemplate) {
        return new RedisRepository(redisTemplate);
    }

    @Bean(name = "redisRateLimitImpl")
    RedisRateLimitImpl redisRateLimitImpl(RedisRateLimitProperties redisRateLimitProperties,
                                          StringRedisTemplate stringRedisTemplate) {
        return new RedisRateLimitImpl(redisRateLimitProperties, stringRedisTemplate);
    }
}
