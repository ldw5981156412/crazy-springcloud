package com.crazymaker.springcloud.user.info.start;

import com.crazymaker.springcloud.standard.config.FeignConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = {
        "com.crazymaker.springcloud.user",
        "com.crazymaker.springcloud.base",
        "com.crazymaker.springcloud.seckill.remote.fallback",
        "com.crazymaker.springcloud.standard"
}, exclude = {
        SecurityAutoConfiguration.class //禁用Spring Security
})
//引入base-session包需要
@EnableSwagger2
@EnableJpaRepositories(basePackages = {"com.crazymaker.springcloud.base.dao"})
@EntityScan(basePackages = {
        "com.crazymaker.springcloud.user.*.dao.po",
        "com.crazymaker.springcloud.base.dao.po",
        "com.crazymaker.springcloud.standard.*.dao.po"})
/**
 *  启用 Eureka Client 客户端组件
*/
@EnableEurekaClient

//启动Feign
@EnableFeignClients(basePackages =
        {"com.crazymaker.springcloud.seckill.remote.client"},
        defaultConfiguration = FeignConfiguration.class
)
@Slf4j
public class UAACloudApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(UAACloudApplication.class, args);
        Environment env = applicationContext.getEnvironment();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        String ip = env.getProperty("eureka.instance.ip-address");

        log.info("\n----------------------------------------------------------\n\t" +
                "UAA 用户账号与认证服务 is running! Access URLs:\n\t" +
                "Local: \t\thttp://" + ip + ":" + port + path + "/\n\t" +
                "swagger-ui: \thttp://" + ip + ":" + port + path + "/swagger-ui.html\n\t" +
                "actuator: \thttp://" + ip + ":" + port + path + "/actuator/info\n\t" +
                "----------------------------------------------------------");
    }


}
