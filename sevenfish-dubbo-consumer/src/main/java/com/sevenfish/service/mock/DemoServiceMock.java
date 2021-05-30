package com.sevenfish.service.mock;

import com.sevenfish.common.Result;
import com.sevenfish.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 本地伪装通常用于服务降级，比如某验权服务，当服务提供方全部挂掉后，客户端不抛出异常，而是通过 Mock 数据返回授权失败
 */
public class DemoServiceMock implements DemoService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String sayHello(String name) {
        logger.error("DemoService provider is useless,sayHello method use mock");
        return "fake";
    }

    @Override
    public CompletableFuture<String> asyncSayHello(String name) {
        logger.error("DemoService provider is useless,asyncSayHello method use mock");
        return CompletableFuture.supplyAsync(() -> "fail");
    }

    @Override
    public Object redisLock(String key) {
        logger.error("DemoService provider is useless,redisLock method use mock");
        return Result.error();
    }

    @Override
    public Object zkLock() {
        logger.error("DemoService provider is useless,zkLock method use mock");
        return Result.error();
    }
}
