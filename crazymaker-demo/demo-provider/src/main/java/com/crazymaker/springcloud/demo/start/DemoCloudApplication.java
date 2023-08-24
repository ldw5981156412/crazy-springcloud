package com.crazymaker.springcloud.demo.start;

import com.crazymaker.springcloud.standard.context.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.List;

@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
@Slf4j
public class DemoCloudApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DemoCloudApplication.class, args);
        /**
         * 打印所有的 spring ioc bean
         */
        List<String> beans = SpringContextUtil.getBeanDefinitionNames();
        log.info(beans.toString());

        Environment env = applicationContext.getEnvironment();
        String port = env.getProperty("server.port");
        String name = env.getProperty("spring.application.name");

        String path = env.getProperty("server.servlet.context-path");
        if (StringUtils.isBlank(path)) {
            path = "";
        }
        String ip = env.getProperty("spring.cloud.client.ip-address");

        System.out.println("\n----------------------------------------------------------\n\t" +
                name.toUpperCase() + " is running! Access URLs:\n\t" +
                "Local: \t\thttp://" + ip + ":" + port + path + "/\n\t" +
                "swagger-ui: \thttp://" + ip + ":" + port + path + "/swagger-ui.html\n\t" +
                "actuator: \thttp://" + ip + ":" + port + path + "/actuator/info\n\t" +
                "----------------------------------------------------------");
    }
}
