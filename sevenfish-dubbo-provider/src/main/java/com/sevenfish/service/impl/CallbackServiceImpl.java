package com.sevenfish.service.impl;

import org.apache.dubbo.config.annotation.Argument;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;
import com.sevenfish.service.CallbackListener;
import com.sevenfish.service.CallbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@DubboService(version = "${demo.service.version}",
        connections = 1,
        callbacks = 1000,
        //指定方法相应的参数回调方法
        methods = {@Method(name = "addListener",arguments = @Argument(index = 1,callback = true))}
)
public class CallbackServiceImpl implements CallbackService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, CallbackListener> listeners = new ConcurrentHashMap<>();

    public CallbackServiceImpl(){
        Thread t = new Thread(()->{
            while(true){
                try {
                    for (Map.Entry<String, CallbackListener> entry : listeners.entrySet()) {
                        try {
                            entry.getValue().changed(getChange(entry.getKey()));
                        } catch (Throwable e) {
                            logger.error("listener listens fail,error message:",e);
                            listeners.remove(entry.getKey());
                        }
                    }
                    // 定时触发变更通知
                    Thread.sleep(5000);
                } catch (Throwable e) {
                    logger.error("listener listens fail,error message:",e);
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
        logger.info("listener start success");
    }

    @Override
    public void addListener(String key, CallbackListener listener) {
        listeners.put(key,listener);
        listener.changed(getChange(key));
    }

    private String getChange(String key){
        return "Change "+key+":"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
