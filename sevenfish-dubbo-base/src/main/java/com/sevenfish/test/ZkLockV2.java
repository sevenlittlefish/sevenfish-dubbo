package com.sevenfish.test;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ZkLockV2 implements Lock,Runnable {
    static int inventory = 100;
    private static final int NUM = 100;

    private static final String IP_PORT = "127.0.0.1:2181";
    private static final String Z_NODE = "/LOCK";

    //这两个变量要保证每个线程是独享的，否则就会有问题
    private volatile String beforePath;
    private volatile String path;

    private ZkClient zkClient = new ZkClient(IP_PORT);

    public ZkLockV2() {
        if (!zkClient.exists(Z_NODE)) {
            zkClient.createPersistent(Z_NODE);
        }
    }

    public static void main(String[] args) {
        try {
            for (int i = 1; i <= NUM; i++) {
                //由于这里每个任务都创建了一个实例，所以属性都是独享的
                new Thread(new ZkLockV2()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            lock();
            int curIn = inventory;
            Thread.sleep(100);
            inventory = --curIn;
            System.out.println(inventory);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            unlock();
            System.out.println("释放锁");
        }
    }

    public void lock() {
        if (tryLock()) {
            System.out.println("获得锁");
        } else {
            // 尝试加锁
            // 进入等待 监听
            waitForLock();
            // 再次尝试
            lock();
        }

    }

    public boolean tryLock() {
        // 第一次就进来创建自己的临时有序节点
        if (path != null && !Objects.equals(path,"")) {
            path = zkClient.createEphemeralSequential(Z_NODE + "/", Thread.currentThread().getName());
        }

        // 对节点排序
        List<String> children = zkClient.getChildren(Z_NODE);
        Collections.sort(children);
        System.out.println(children.toString());

        // 当前的是最小节点就返回加锁成功
        if (path.equals(Z_NODE + "/" + children.get(0))) {
            return true;
        } else {
            // 不是最小节点 就找到自己的前一个 依次类推 释放也是一样
            int i = Collections.binarySearch(children, path.substring(Z_NODE.length() + 1));
            beforePath = Z_NODE + "/" + children.get(i - 1);
        }
        return false;
    }

    public void unlock() {
        zkClient.delete(path);
    }

    public void waitForLock() {
        CountDownLatch cdl = new CountDownLatch(1);
        IZkDataListener listener = new IZkDataListener() {
            public void handleDataChange(String s, Object o) throws Exception {
            }

            public void handleDataDeleted(String s) throws Exception {
                System.out.println("监听到节点删除事件！");
                cdl.countDown();
            }
        };
        // 监听
        zkClient.subscribeDataChanges(beforePath, listener);
        if (zkClient.exists(beforePath)) {
            try {
                System.out.println("等待节点删除");
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 释放监听
        zkClient.unsubscribeDataChanges(beforePath, listener);
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public Condition newCondition() {
        return null;
    }
}
