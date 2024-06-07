package com.crazymaker.springcloud.user.info.controller;

import com.crazymaker.springcloud.base.service.impl.UserServiceImpl;
import com.crazymaker.springcloud.common.dto.UserDTO;
import com.crazymaker.springcloud.common.result.RestOut;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/user/")
public class UserController {

    @Resource
    private UserServiceImpl userService;

    @GetMapping("detail/v1")
    public RestOut<UserDTO> getUser(@RequestParam(value = "userId") Long userId){
        log.info("方法 getUser 被调用了");
        UserDTO userDTO = userService.getUser(userId);
        if (userDTO == null) {
            return RestOut.error("没有找到用户");
        }
        return RestOut.success(userDTO).setRespMsg("操作成功");
    }
}
