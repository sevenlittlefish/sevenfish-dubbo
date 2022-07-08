package com.sevenfish.aspect;

import com.sevenfish.annotation.CurrentLimiting;
import com.sevenfish.common.Result;
import com.sevenfish.util.RedisUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
public class CurrentLimitingAspect {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisUtils redisUtils;

    @Pointcut("@annotation(com.sevenfish.annotation.CurrentLimiting)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Method method = ms.getMethod();
        CurrentLimiting annotation = method.getAnnotation(CurrentLimiting.class);
        String limitKey = annotation.limitKey();
        int limitNum = annotation.limitNum();
        int windowRange = annotation.windowRange();
        int expireTime = annotation.expireTime();
        boolean result = redisUtils.slidingWindowCurrentLimiting(limitKey, limitNum, UUID.randomUUID().toString(), windowRange, expireTime);
        if (result) return joinPoint.proceed();
        //The following code will occur concurrency issues
        /*long curTime = System.currentTimeMillis();
        Long count = redisUtils.zcount(limitKey, curTime - windowRange * 1000, curTime);
        if (count != null && count < limitNum) {
            redisUtils.zadd(limitKey, UUID.randomUUID().toString(), curTime, windowRange);
            log.info(Thread.currentThread().getName() + " get permit, current time:" + System.currentTimeMillis());
            return joinPoint.proceed();
        }*/
        return Result.error("Request too many, please wait a moment to try!");
    }
}
