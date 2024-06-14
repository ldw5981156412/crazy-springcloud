package com.crazymaker.springcloud.user.info.controller;

import com.crazymaker.springcloud.base.service.impl.UserServiceImpl;
import com.crazymaker.springcloud.common.constants.SessionConstants;
import com.crazymaker.springcloud.common.result.RestOut;
import com.crazymaker.springcloud.user.info.api.dto.LoginInfoDTO;
import com.crazymaker.springcloud.user.info.api.dto.LoginOutDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "用户端登录与退出", tags = {"用户信息、基础学习DEMO"})
@RestController
@RequestMapping("/api/session")
public class SessionController {

    //用户端会话服务
    @Resource
    private UserServiceImpl userService;

    //用户端的登录REST接口
    @PostMapping("/login/v1")
    @ApiOperation(value = "用户端登录")
    public RestOut<LoginOutDTO> login(@RequestBody LoginInfoDTO loginInfoDTO,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        //调用服务层登录方法获取令牌
        LoginOutDTO dto = userService.login(loginInfoDTO);
        response.setHeader("Content-Type","application/json;charset=utf-8");
        response.setHeader(SessionConstants.AUTHORIZATION_HEAD, dto.getToken());
        return RestOut.success(dto);
    }
}
