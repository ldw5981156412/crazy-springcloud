package com.crazymaker.springcloud.user.info.remote.client;

import com.crazymaker.springcloud.common.dto.UserDTO;
import com.crazymaker.springcloud.common.result.RestOut;
import com.crazymaker.springcloud.standard.config.FeignConfiguration;
import com.crazymaker.springcloud.user.info.remote.fallback.UserClientFallback;
import com.crazymaker.springcloud.user.info.remote.fallback.UserClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign 客户端接口
 * @author ldw59
 * @description 用户信息 远程调用接口
 */
@FeignClient(value = "uaa-provider", path = "/uaa-provider/api/user",
//        fallback = UserClientFallback.class,
//        configuration = FeignConfiguration.class,
        fallbackFactory = UserClientFallbackFactory.class
)
public interface UserClient {

    /**
     * 远程调用 RPC 方法：获取用户详细信息
     * @param userId 用户 Id
     * @return 用户详细信息
     */
    @GetMapping("/detail/v1")
    RestOut<UserDTO> detail(@RequestParam(value = "userId") Long userId);

    @GetMapping("/hello/v1")
    String hello(@RequestParam(value = "name") String name);
}
