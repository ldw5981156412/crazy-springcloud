package com.crazymaker.springcloud.seckill.remote.client;

import com.alibaba.fastjson.JSONObject;
import com.crazymaker.springcloud.common.result.RestOut;
import com.crazymaker.springcloud.seckill.remote.fallback.DemoDefaultFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "demo-provider", path = "/demo-provider/api/demo/",
        fallback = DemoDefaultFallback.class)
public interface DemoClient {


    @GetMapping("/hello/v1")
    RestOut<JSONObject> hello();

    @RequestMapping(name = "/echo/{word}/v1",method = RequestMethod.GET)
    RestOut<JSONObject> echo(@PathVariable(value = "word") String word);
}
