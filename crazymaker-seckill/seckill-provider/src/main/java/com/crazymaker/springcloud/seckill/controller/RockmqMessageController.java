package com.crazymaker.springcloud.seckill.controller;

import com.crazymaker.springcloud.common.exception.BusinessException;
import com.crazymaker.springcloud.common.result.RestOut;
import com.crazymaker.springcloud.common.util.JsonUtil;
import com.crazymaker.springcloud.seckill.api.dto.SeckillDTO;
import com.crazymaker.springcloud.seckill.service.impl.RedisSeckillServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author ldw59
 */
@RestController
@RequestMapping("/api/seckill/rockmq/")
@Api(tags = "Rockmq消息Demo")
@Slf4j
public class RockmqMessageController implements ApplicationContextAware {

    public static final String TOPIC_SECKILL = "Seckill_Topic";

    @Value("${rocketmq.address}")
    private String rocketmqAddress;

    @Resource
    private RedisSeckillServiceImpl redisSeckillService;

    DefaultMQProducer producer = new DefaultMQProducer("seckill_producer");
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("seckill_consumer");

    {
        consumer.setMessageModel(MessageModel.CLUSTERING);
    }

    SendCallback sendCallback = new SendCallback() {
        @Override
        public void onSuccess(SendResult sendResult) {
            log.info("发送成功:{}", sendResult.toString());
        }

        @Override
        public void onException(Throwable e) {
            log.error("发送失败:{}", e.getMessage(), e);
            // 消息发送失败，可以持久化这条数据，后续进行补偿处理
        }
    };

    /**
     * 执行秒杀的操作
     * <p>
     * <p>
     * {
     * "exposedKey": "4b70903f6e1aa87788d3ea962f8b2f0e",
     * "newStockNum": 10000,
     * "seckillSkuId": 1157197244718385152,
     * "seckillToken": "0f8459cbae1748c7b14e4cea3d991000",
     * "userId": 37
     * }
     *
     * @return
     */
    @ApiOperation(value = "发送秒杀")
    @PostMapping("/sendSeckill/v1")
    public RestOut<String> sendSeckill(@RequestBody SeckillDTO dto) {
        String content = JsonUtil.pojoToJson(dto);
        try {
            Message msg = new Message(TOPIC_SECKILL, "Tag", content.getBytes(RemotingHelper.DEFAULT_CHARSET));
            //异步发送消息
            producer.send(msg, sendCallback);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().errMsg("发送失败").build();
        }
        return RestOut.success("发送成功");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        startProducer();
        startConsumer();
    }


    private void startProducer() {
        producer.setNamesrvAddr(rocketmqAddress);
        producer.setInstanceName("seckill_producer");
        producer.setRetryTimesWhenSendFailed(3);
        try {
            producer.start();
        } catch (Exception e) {
            log.error("启动producer失败", e);
        }

    }

    private void startConsumer() {
        consumer.setNamesrvAddr(rocketmqAddress);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        try {
            consumer.subscribe(TOPIC_SECKILL, "*");
        } catch (MQClientException e) {
            log.error(e.getMessage(), e);
            return;
        }

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                String content = new String(msg.getBody());
                log.info("收到消息:{}", msg.getMsgId() + " " + msg.getTopic() + " " + msg.getTags() + " " + content);
                SeckillDTO seckillDTO = JsonUtil.jsonToPojo(content, SeckillDTO.class);
                try {
                    redisSeckillService.executeSeckill(seckillDTO);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        try {
            consumer.start();
        } catch (MQClientException e) {
            log.error(e.getMessage(), e);
        }

    }
}
