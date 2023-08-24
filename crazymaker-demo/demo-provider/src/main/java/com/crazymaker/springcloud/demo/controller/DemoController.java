package com.crazymaker.springcloud.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.crazymaker.springcloud.common.result.RestOut;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;


@RestController
@RequestMapping("/api/demo/")
@Api(tags = "Demo 演示")
public class DemoController
{

    /**
     * 非常简单的一个接口,返回Json 对象
     *
     * @return hello world
     */
    @GetMapping("/hello/v1")
    @ApiOperation(value = "hello world api 接口")
    public RestOut<JSONObject> hello()
    {
        JSONObject data = new JSONObject();
        data.put("hello", "world");
        return RestOut.success(data).setRespMsg("操作成功");
    }


    /**
     * 回显消息 接口
     *
     * @return echo 回显消息
     */
    @GetMapping("/echo/{word}/v1")
    @ApiOperation(value = "回显消息  api 接口")
    public RestOut<JSONObject> echo(@PathVariable(value = "word") String word)
    {
        JSONObject data = new JSONObject();
        data.put("echo", word);
        return RestOut.success(data).setRespMsg("操作成功");
    }

    /**
     * 获取头部信息 接口
     *
     * @return RestOut
     */
    @GetMapping("/header/echo/v1")
    @ApiOperation(value = "回显头部信息")
    public RestOut<JSONObject> echo(HttpServletRequest request)
    {
        /**
         * 获取头部信息，放入  JSONObject 实例
         */
        JSONObject data = new JSONObject();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements())
        {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            data.put(key, value);
        }

        return RestOut.success(data).setRespMsg("操作成功");
    }


}
