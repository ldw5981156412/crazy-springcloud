package com.crazymaker.springcloud.seckill.main;

import com.crazymaker.redission.demo.RedissonManager;
import com.crazymaker.springcloud.common.dto.UserDTO;
import com.crazymaker.springcloud.common.util.ThreadUtil;
import com.crazymaker.springcloud.seckill.start.RedissionDemoCloudApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RunWith(SpringRunner.class)
// 指定启动类
@SpringBootTest(classes = {RedissionDemoCloudApplication.class})
public class RedissonTest {
    @Resource
    RedissonManager redissonManager;

    @Test
    public void testRBucketExamples() {
        // 默认连接上 127.0.0.1:6379
        RedissonClient client = redissonManager.getRedisson();
        RBucket<String> rstring = client.getBucket("redisson:test:bucket:string");
        rstring.set("hello world");
        RBucket<UserDTO> ruser = client.getBucket("redisson:test:bucket:user");
        UserDTO user = new UserDTO();
        user.setUsername("张三");
        user.setToken("12345678");
        ruser.set(user);
        System.out.println("bucket string:" + rstring.get());
        System.out.println("bucket user:" + ruser.get());
        client.shutdown();
    }

    @Test
    public void testListExamples() {
        // 默认连接上 127.0.0.1:6379
        RedissonClient client = redissonManager.getRedisson();
        // RList 继承了 java.util.List 接口
        RList<String> nameList = client.getList("redisson:test:nameList");
        nameList.clear();
        nameList.add("张三");
        nameList.add("李四");
        nameList.add("王五");
        nameList.remove(-1);
        System.out.println("List size" + nameList.size());
        boolean contains = nameList.contains("李四");
        System.out.println("Is list contains name '李四': " + contains);
        nameList.forEach(System.out::println);
        client.shutdown();
    }

    @Test
    public void testMapExamples() {
        // 默认连接上 127.0.0.1:6379
        RedissonClient client = redissonManager.getRedisson();
        // RMap 继承了 java.util.concurrent.ConcurrentMap 接口
        RMap<String, Object> map = client.getMap("redisson:test:personMap");
        map.put("name", "张三");
        map.put("sex", "男");
        map.put("address", "上海");
        map.put("age", 18);
        System.out.println("Map size" + map.size());
        boolean contains = map.containsKey("age");
        System.out.println("Is Map contains key 'age': " + contains);
        String value = String.valueOf(map.get("name"));
        System.out.println("Map value of key 'name': " + value);
        map.forEach((k, v) -> System.out.println(k + ":" + v));
        client.shutdown();
    }

    @Test
    public void testLuaExamples() {
        // 默认连接上 127.0.0.1:6379
        RedissonClient client = redissonManager.getRedisson();
        client.getBucket("redisson:test:foo").set("bar");
        String r = client.getScript().eval(RScript.Mode.READ_ONLY,
                "return redis.call('get', 'redisson:test:foo')",
                RScript.ReturnType.VALUE);
        System.out.println("foo:" + r);
        // 通过预存的脚本进行同样的操作
        RScript s = client.getScript();
        // 首先将脚本加载到Redis
        String sha1 = s.scriptLoad("return redis.call('get', 'redisson:test:foo')");
        System.out.println("sha1:" + sha1);
        // 再通过SHA值调用脚本
        RFuture<Object> r1 = client.getScript().evalShaAsync(RScript.Mode.READ_ONLY, sha1,
                RScript.ReturnType.VALUE, Collections.emptyList());
        try {
            System.out.println("foo:" + r1.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        client.shutdown();
    }

    @Test
    public void testLockExamples() {
        // 默认连接上 127.0.0.1:6379
        RedissonClient client = redissonManager.getRedisson();
        // RLock 继承了 java.util.concurrent.locks.Lock 接口
        RLock lock = client.getLock("redisson:test:lock:1");
        final int[] count = {0};
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    lock.lock();
                    count[0]++;

                    lock.unlock();
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("10个线程每个累加1000为： =" + count[0]);
        //输出统计结果
        float time = System.currentTimeMillis() - start;
        System.out.println("运行的时长为：" + time);
        System.out.println("每一次执行的时长为：" + time /count[0]);
    }

    @Test
    public void testRAtomicLongExamples() {
        // 默认连接上 127.0.0.1:6379
        RedissonClient client = redissonManager.getRedisson();
        RAtomicLong atomicLong = client.getAtomicLong("redisson:test:atomicLong");
        // 线程数
        final int threads = 10;
        // 每条线程的执行轮数
        final int turns = 1000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() ->
            {
                try {
                    for (int j = 0; j < turns; j++) {
                        atomicLong.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        ThreadUtil.sleepSeconds(20);
        System.out.println("atomicLong：" + atomicLong.get());
        client.shutdown();
    }

    @Test
    public void testRLongAdderExamples() {
        // 默认连接上 127.0.0.1:6379
        RedissonClient client = redissonManager.getRedisson();
        RLongAdder longAdder = client.getLongAdder("redisson:test:longAdder");
        // 线程数
        final int threads = 10;
        // 每条线程的执行轮数
        final int turns = 1000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() ->
            {
                try {
                    for (int j = 0; j < turns; j++) {
                        longAdder.increment();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        ThreadUtil.sleepSeconds(10);
        System.out.println("longAdder：" + longAdder.sum());
        client.shutdown();
    }
}
