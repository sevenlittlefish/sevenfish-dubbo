package com.sevenfish.controller;

import com.sevenfish.annotation.CurrentLimiting;
import com.sevenfish.common.Result;
import com.sevenfish.service.CallbackService;
import com.sevenfish.service.CallbackListener;
import com.sevenfish.service.DemoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class DemoController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @DubboReference(version = "${demo.service.version}",
            //服务降级
            mock = "com.sevenfish.service.mock.DemoServiceMock"
    )
    private DemoService demoService;

    @DubboReference(version = "${demo.service.version}")
    private CallbackService callBackService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @GetMapping("/hello")
    @CurrentLimiting(limitKey = "hello", limitNum = 2, windowRange = 1, expireTime = 60)
    public Object hello(@RequestParam("name") String name) {
        return demoService.sayHello(name);
    }

    @GetMapping("/asnycHello")
    public Object asnycHello(@RequestParam("name")String name){
        RpcContext.getContext().setAttachment("consumer-key","just do it");
        CompletableFuture<String> future = demoService.asyncSayHello(name);
        logger.info("future task run out before");
        String content = null;
        try {
            content = future.get();
            logger.info("future task run out,content:{}",content);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return content;
    }

    @GetMapping("/callback")
    public void callback(@RequestParam("key")String key){
        callBackService.addListener(key, new CallbackListener() {
            @Override
            public void changed(String msg) {
                logger.info("callback:{}",msg);
            }
        });
        logger.info("callback ok");
    }

    @GetMapping("/redisLock")
    public Object redisLock(@RequestParam("key")String key){
        return demoService.redisLock(key);
    }

    @GetMapping("/zkLock")
    public Object zkLock(){
        return demoService.zkLock();
    }

    /**
     * 对于一条记录，先对其进行序列化，然后按照Topic和Partition，放进对应的发送队列中
     * 如果 Partition 没填，那么情况会是这样的：
     * a、Key 有填。按照 Key 进行哈希，相同 Key 去一个 Partition
     * b、Key 没填。Round-Robin 来选 Partition
     * @param msg
     * @return
     */
    @GetMapping("/msgSend")
    public Object msgSend(@RequestParam("msg")String msg){
        kafkaTemplate.send("myTopic", msg);
        return Result.success();
    }

    @GetMapping("/msgQueueSeckill")
    public Object msgQueueSeckill(@RequestParam("msg")String msg){
        kafkaTemplate.send("seckill", msg);
        return Result.success();
    }
}
