package com.crazymaker.redission.demo.strategy.impl;

import com.crazymaker.redission.demo.constant.GlobalConstant;
import com.crazymaker.redission.demo.entity.RedissonConfig;
import com.crazymaker.redission.demo.strategy.RedissonConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.config.Config;

/**
 * 哨兵集群部署Redis连接配置
 *
 * @author liudawei
 * @date 2023/10/1
 **/
@Slf4j
public class SentineConfigImpl implements RedissonConfigService {
    @Override
    public Config createRedissonConfig(RedissonConfig redissonConfig) {
        Config config = new Config();
        try {
            String address = redissonConfig.getAddress();
            String password = redissonConfig.getPassword();
            int database = redissonConfig.getDatabase();
            String[] addrTokens = address.split(",");
            String sentinelAliasName = addrTokens[0];
            //设置redis配置文件sentinel.conf配置的sentinel别名
            config.useSentinelServers()
                    .setMasterName(sentinelAliasName)
                    .setDatabase(database);
            if (StringUtils.isNotBlank(password)) {
                config.useSentinelServers().setPassword(password);
            }
            //设置sentinel节点的服务IP和端口
            for (int i = 1; i < addrTokens.length; i++) {
                config.useSentinelServers().addSentinelAddress(GlobalConstant.REDIS_CONNECTION_PREFIX.getConstantValue() + addrTokens[i]);
            }
            log.info("初始化[哨兵部署]方式Config,redisAddress:" + address);
        } catch (Exception e) {
            log.error("哨兵部署 Redisson init error", e);
        }
        return config;
    }
}
