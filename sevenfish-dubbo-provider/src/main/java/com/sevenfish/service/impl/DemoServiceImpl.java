package com.sevenfish.service.impl;

import com.sevenfish.common.InventoryData;
import com.sevenfish.common.Result;
import com.sevenfish.service.DemoService;
import com.sevenfish.util.RedisUtils;
import com.sevenfish.util.ThreadPoolUtil;
import com.sevenfish.util.ZkUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.CompletableFuture;

@DubboService(version = "${demo.service.version}",
        timeout = 60000,
        //集群容错策略
        cluster = "failover",
        //失败重试次数，缺省是2
        retries = 0,
        //权重
        weight = 5,
        //负载均衡策略，缺省是随机random。还可以配置轮询roundrobin、最不活跃优先leastactive 和一致性哈希consistenthash等
        loadbalance = "roundrobin",
        //指定方法集群容错重试次数
        methods = {@Method(name = "sayHello",retries = 2),@Method(name = "asyncSayHello",retries = 0)},
        //占用线程池线程数
        executes = 10
)
public class DemoServiceImpl implements DemoService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${dubbo.application.name}")
    private String serviceName;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ZkUtils zkUtils;

    @Override
    public String sayHello(String name) {
        logger.info("消费者调用了hello方法，参数："+name);
        return String.format("[%s] : Hello, %s",serviceName,name);
    }

    @Override
    public CompletableFuture<String> asyncSayHello(String name) {
        RpcContext savedContext = RpcContext.getContext();
        // 建议为supplyAsync提供自定义线程池，避免使用JDK公用线程池
        return CompletableFuture.supplyAsync(() -> {
            System.out.println(savedContext.getAttachment("consumer-key"));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "async response from provider.";
        }, ThreadPoolUtil.getThreadPool());
    }

    @Override
    public Object redisLock(String key) {
        try {
            redisUtils.lock(key);
            if(InventoryData.getInventory() > 0){
                int curIn = InventoryData.getInventory();
                Thread.sleep(100);
                InventoryData.setInventory(--curIn);
            }
            logger.info("Current inventory is "+InventoryData.getInventory());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            redisUtils.unlock(key);
        }
        return Result.success("库存："+InventoryData.getInventory());
    }

    @Override
    public Object zkLock() {
        try {
            zkUtils.lock();
            if(InventoryData.getInventory() > 0){
                int curIn = InventoryData.getInventory();
                Thread.sleep(100);
                InventoryData.setInventory(--curIn);
            }
            logger.info("Current inventory is "+InventoryData.getInventory());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            zkUtils.unlock();
        }
        return Result.success("库存："+InventoryData.getInventory());
    }
}
