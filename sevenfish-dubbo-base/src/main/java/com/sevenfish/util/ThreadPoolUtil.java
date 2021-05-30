package com.sevenfish.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {

    private static volatile ThreadPoolExecutor threadPool = null;

    private ThreadPoolUtil(){}

    public static ThreadPoolExecutor getThreadPool(){
        if (threadPool == null){
            synchronized (ThreadPoolUtil.class){
                if (threadPool == null)
                    threadPool = new ThreadPoolExecutor(10,15,60, TimeUnit.SECONDS,new LinkedBlockingQueue<>(5));
            }
        }
        return threadPool;
    }
}
