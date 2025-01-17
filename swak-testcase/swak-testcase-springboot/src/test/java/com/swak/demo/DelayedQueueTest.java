package com.swak.demo;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.swak.cache.queue.DelayEvent;
import com.swak.cache.queue.DelayedQueueHandler;
import com.swak.cache.spi.DelayedQueueManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RList;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * De.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SwakCaseApplication.class,webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class DelayedQueueTest {

    @Resource
    private DelayedQueueManager delayedQueueManager;

    @Test
    public void testDelayedQueue() throws InterruptedException {
        String queueName = "WorkFlow_Queue";
        delayedQueueManager.onMultiSubscribe(1000L,queueName);
        DelayedQueueHandler delayedQueueHandler = delayedQueueManager.createQueue(queueName);
        delayedQueueHandler.addListeners((delayEvent,handler)->{
             System.out.println(JSON.toJSONString(delayEvent));
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>");
        });
        DelayEvent<String> delayEvent = new DelayEvent<>("1234");
        delayedQueueHandler.add(delayEvent,15L, TimeUnit.SECONDS);
        delayedQueueHandler.add(new DelayEvent<>("222"),5L, TimeUnit.SECONDS);
        delayedQueueHandler.add(new DelayEvent<>("3333"),5L, TimeUnit.SECONDS);

        delayedQueueHandler.remove(delayEvent);

        delayedQueueHandler.remove(new DelayEvent<>("222"));

        TimeUnit.MINUTES.sleep(1L);

        delayedQueueManager.stop();
    }

    @Test
    public void testRedissonList() {
        RList<String> redList = delayedQueueManager.getClient().getList("colley");
        redList.add("1");
        redList.add("2");

        redList = delayedQueueManager.getClient().getList("colley");
        for (String s:redList){
            System.out.println(s);
        }
    }
}
