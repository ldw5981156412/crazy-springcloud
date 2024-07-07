package com.crazymaker.springcloud.stock.controller;

import com.crazymaker.springcloud.common.exception.BusinessException;
import com.crazymaker.springcloud.common.result.RestOut;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crazymaker/rockmq/")
@Api(tags = "Rockmq消息Demo")
@Slf4j
public class RockmqMessageController implements ApplicationContextAware {

    public static final String TOPIC_TEST = "TopicTest";
    DefaultMQProducer producer = new DefaultMQProducer("producer_demo");
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_demo");

    @Value("${rocketmq.address}")
    private String rocketmqAddress;

    @PostMapping("/msg/send/v1")
    @ApiOperation(value = "发送rockmq消息")
    public RestOut<String> sendMessage(@RequestBody String content) {
        try {
            //构建消息
            Message msg = new Message(TOPIC_TEST, "TagA", content.getBytes(RemotingHelper.DEFAULT_CHARSET));
            //发送同步消息
            SendResult send = producer.send(msg);
            log.info("发送完成: {}", send);
        } catch (Exception e) {
            throw BusinessException.builder().errMsg(e.getMessage()).build();
        }
        return RestOut.success("发送成功");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        startProducer();
        startConsumer();
    }

    private void startProducer() {
        //指定NameServer地址
        producer.setNamesrvAddr(rocketmqAddress);
        producer.setInstanceName("Instance1");
        producer.setRetryTimesWhenSendFailed(3);
        /*
         * Producer对象在使用之前必须要调用start初始化，初始化一次即可
         * 注意：切记不可以在每次发送消息时，都调用start方法
         */
        try {
            producer.start();
            log.info("product start ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startConsumer() {
        //指定NameServer地址，多个地址以 ; 隔开
        consumer.setNamesrvAddr(rocketmqAddress);
        /*
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        try {
            consumer.subscribe(TOPIC_TEST, "*");
        } catch (MQClientException e) {
            e.printStackTrace();
            return;
        }

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt messageExt : msgs) {
                log.info("收到消息: {}", messageExt.getTopic() + " " + messageExt.getTags() + " " + new String(messageExt.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        try {
            consumer.start();
            log.info("consumer start ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
