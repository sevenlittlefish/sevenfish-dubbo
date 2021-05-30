package com.sevenfish.service;

import java.util.concurrent.CompletableFuture;

public interface DemoService {

    String sayHello(String name);

    CompletableFuture<String> asyncSayHello(String name);

    Object redisLock(String key);

    Object zkLock();
}
