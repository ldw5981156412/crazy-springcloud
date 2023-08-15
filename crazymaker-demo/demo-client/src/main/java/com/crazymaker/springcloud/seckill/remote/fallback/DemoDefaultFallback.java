package com.crazymaker.springcloud.seckill.remote.fallback;

import com.alibaba.fastjson.JSONObject;
import com.crazymaker.springcloud.common.result.RestOut;
import com.crazymaker.springcloud.seckill.remote.client.DemoClient;
import org.springframework.web.bind.annotation.GetMapping;

public class DemoDefaultFallback implements DemoClient {

    /**
     * 测试远程调用
     */
    @GetMapping("/hello/v1")
    @Override
    public RestOut<JSONObject> hello() {
        return RestOut.error("远程调用失败,返回熔断后的调用结果");
    }

    @Override
    public RestOut<JSONObject> echo(String word) {
        return RestOut.error("远程调用失败,返回熔断后的调用结果" );
    }
}
