package com.crazymaker.springcloud.ratelimit;

import com.crazymaker.springcloud.demo.start.DemoCloudApplication;
import com.crazymaker.springcloud.standard.ratelimit.RedisRateLimitImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RunWith(SpringRunner.class)
// 指定启动类
@SpringBootTest(classes = DemoCloudApplication.class)
public class RedisRateLimitTest {
    @Resource(name = "redisRateLimitImpl")
    RedisRateLimitImpl limitService;

    //线程池，用于多线程模拟测试
    private ExecutorService pool = Executors.newFixedThreadPool(10);

    @Test
    public void test() {
        //初始化的分布式令牌桶限流器
        limitService.initLimitKey(
                "seckill",//Redis key中的类型
                "10000",//Redis key中的业务key，比如商品id
                2, //桶容量
                2); //每秒令牌数
        AtomicInteger count = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        //线程数
        final int threads = 2;
        //每条线程执行的轮数
        final int turns = 20;
        CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    for (int j = 0; j < turns; j++) {
                        if (limitService.tryAcquire("seckill:10000")) {
                            count.incrementAndGet();
                        }
                    }
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float time = (System.currentTimeMillis() - start) / 1000F;
        //输出统计结果
        log.info("限制的次数为：" + count.get() + " 时长为：" + time + ",通过的次数为：" + (threads * turns - count.get()));
        log.info("限制的比例为：" + (float) count.get() / (float) (threads * turns));
        log.info("运行的时长为：" + time);
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
