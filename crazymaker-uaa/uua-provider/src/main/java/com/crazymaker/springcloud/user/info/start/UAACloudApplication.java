package com.crazymaker.springcloud.user.info.start;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 启用Eureka Client客户端组件
 */
@EnableEurekaClient
@SpringBootApplication
@Slf4j
public class UAACloudApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(UAACloudApplication.class, args);
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        String ip = env.getProperty("eureka.instance.ip-address");
        log.info("\n----------------------------------------------------------\n\t" +
                "UAA 用户账号与认证服务 is running! Access URLs:\n\t" +
                "Local: \t\thttp://"+ ip+":"+ port +  path + "/\n\t" +
                "swagger-ui: \thttp://"+ ip+":"+ port +  path + "/swagger-ui.html\n\t" +
                "actuator: \thttp://"+ ip+":"+ port +  path + "/actuator/info\n\t" +
                "----------------------------------------------------------");
    }
}
