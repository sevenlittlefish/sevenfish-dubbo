package com.sevenfish.util;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@Component
public class ZkUtils {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${dubbo.registry.address}")
    private String IP_PORT;

    private static final String Z_NODE = "/LOCK";

    private static ZkClient zkClient = null;

    private static ThreadLocal<String> path = new ThreadLocal<>();
    private static ThreadLocal<String> beforePath = new ThreadLocal<>();

    @PostConstruct
    public void init(){
        zkClient = new ZkClient(IP_PORT);
        if (!zkClient.exists(Z_NODE)) {
            zkClient.createPersistent(Z_NODE);
        }
    }

    @PreDestroy
    public void destroy(){
        if (zkClient != null) zkClient.close();
    }

    public void lock(){
        if (tryLock()) {
            logger.info("获得锁");
        } else {
            // 尝试加锁
            // 进入等待 监听
            waitForLock();
            // 再次尝试
            lock();
        }
    }

    private boolean tryLock() {
        // 第一次就进来创建自己的临时有序节点
        if (StringUtils.isBlank(path.get())) {
            path.set(zkClient.createEphemeralSequential(Z_NODE + "/node", Thread.currentThread().getName()));
        }

        // 对节点排序
        List<String> children = zkClient.getChildren(Z_NODE);
        Collections.sort(children);
        logger.info(children.toString());

        // 当前的是最小节点就返回加锁成功
        if (path.get().equals(Z_NODE + "/" + children.get(0))) {
            return true;
        } else {
            // 不是最小节点 就找到自己的前一个 依次类推 释放也是一样
            int i = Collections.binarySearch(children, path.get().substring(Z_NODE.length() + 1));
            beforePath.set(Z_NODE + "/" + children.get(i - 1));
        }
        return false;
    }

    private void waitForLock() {
        //注:监听事件可能是单线程处理的，不可用ThreadLocal做CountDownLatch的线程隔离
        CountDownLatch cdl = new CountDownLatch(1);
        IZkDataListener listener = new IZkDataListener() {
            public void handleDataChange(String s, Object o) throws Exception {
            }

            public void handleDataDeleted(String s) throws Exception {
                logger.info("监听到节点删除事件！");
                cdl.countDown();
            }
        };
        // 监听
        zkClient.subscribeDataChanges(beforePath.get(), listener);
        if (zkClient.exists(beforePath.get())) {
            try {
                logger.info("等待节点删除");
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 释放监听
        zkClient.unsubscribeDataChanges(beforePath.get(), listener);
        beforePath.remove();
    }

    public void unlock(){
        zkClient.delete(path.get());
        path.remove();
        logger.info("释放锁");
    }

}
