package com.sevenfish.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import com.sevenfish.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 对于一个topic,同一个group中不能有多于partitions个数的consumer同时消费,否则将意味着某些consumer将无法得到消息
 * 同一个消费组的两个消费者不会同时消费一个 partition
 */
@Component
public class KafkaTopicListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DemoService demoService;

    @KafkaListener(topics = "myTopic")
    public void myTopicListener1(ConsumerRecord record){
        //未设置groupId，则从配置文件spring.kafka.consumer.group-id取
        logger.info("Topic:myTopic,GroupId:default,receive kafka message:{}",record.toString());
    }

    @KafkaListener(topics = "myTopic",groupId = "ggb")
    public void myTopicListener2(ConsumerRecord record){
        logger.info("Topic:myTopic,GroupId:ggb,receive kafka message:{}",record.toString());
    }

    @KafkaListener(topics = "seckill")
    public void myTopicListener3(ConsumerRecord record){
        logger.info("Topic:seckill,GroupId:default,receive kafka message:{}",record.toString());
        demoService.redisLock(record.value().toString());
    }
}
