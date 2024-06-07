package com.crazymaker.springcloud.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.crazymaker.springcloud.common.dto.UserDTO;
import com.crazymaker.springcloud.common.result.RestOut;
import com.crazymaker.springcloud.common.util.JsonUtil;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/call/uaa/")
public class UaaRemoteCallController {

    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    @RequestMapping("/user/detail/v1")
    public RestOut<JSONObject> remoteCallV1() {
        //根据实际的地址调整：UAA 服务的获取用户信息地址
        String url = "http://crazydemo.com:7702/uaa-provider/api/user/detail/v1?userId=1";
        //使用建造者的build()方法，建造 restTemplate 实例
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        TypeReference<RestOut<UserDTO>> pojoType = new TypeReference<RestOut<UserDTO>>() {
        };
        //转成json对象,用到了阿里 FastJson
        RestOut<UserDTO> result = JsonUtil.jsonToPojo(responseEntity.getBody(), pojoType);
        //组装成最终的结果，然后返回到客户端
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uaa-data", result);
        return RestOut.success(jsonObject).setRespMsg("操作成功");
    }
}
