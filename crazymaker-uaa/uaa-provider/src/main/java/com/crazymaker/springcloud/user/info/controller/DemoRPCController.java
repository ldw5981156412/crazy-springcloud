package com.crazymaker.springcloud.user.info.controller;

import com.alibaba.fastjson.JSONObject;
import com.crazymaker.springcloud.common.result.RestOut;
import com.crazymaker.springcloud.seckill.remote.client.DemoClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/call/demo/")
@Api(tags = "演示 demo-provider 远程调用")
public class DemoRPCController {
    @Resource
    DemoClient demoClient;

    @GetMapping("/hello/v1")
    @ApiOperation(value = "hello远程调用")
    public RestOut<JSONObject> remoteHello(){
        RestOut<JSONObject> hello = demoClient.hello();
        JSONObject data = new JSONObject();
        data.put("demo-data",hello);
        return RestOut.success(data).setRespMsg("操作成功");
    }

    @GetMapping("/echo/{word}/v1")
    @ApiOperation(value = "hello远程调用")
    public RestOut<JSONObject> remoteecho(@PathVariable("word") String word){
        /**
         * 调用demo-provider的REST接口api/demo/echo/{0}/v1
         */
        RestOut<JSONObject> hello = demoClient.echo(word);
        JSONObject data = new JSONObject();
        data.put("demo-data",hello);
        return RestOut.success(data).setRespMsg("操作成功");
    }
}
