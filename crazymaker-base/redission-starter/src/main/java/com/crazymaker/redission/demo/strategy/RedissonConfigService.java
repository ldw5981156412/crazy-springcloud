package com.crazymaker.redission.demo.strategy;

import com.crazymaker.redission.demo.entity.RedissonConfig;
import org.redisson.config.Config;

/**
 * Redisson 配置构建接口
 * @author liudawei
 * @date 2023/9/30
 **/
public interface RedissonConfigService {

    /**
     * 根据不同的Redis配置策略创建对应的Config
     * @param redissonConfig
     * @return Config
     */
    Config createRedissonConfig(RedissonConfig redissonConfig);
}
