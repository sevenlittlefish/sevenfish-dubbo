package com.sevenfish.test;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

public class SeataConfigInit {

    private static final String IP_PORT = "127.0.0.1:2181";
    private static final String SEATA_NODE = "/seata";
    private static ZkClient zkClient = new ZkClient(IP_PORT);

    public static void main(String[] args) {
        if(zkClient.exists(SEATA_NODE)) zkClient.deleteRecursive(SEATA_NODE);
        zkClient.createPersistent(SEATA_NODE,true);
        zkClient.setZkSerializer(new ZkSerializer() {
            @Override
            public byte[] serialize(Object data) throws ZkMarshallingError {
                return ((String)data).getBytes(Charset.forName("UTF-8"));
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                return new String(bytes, Charset.forName("UTF-8"));
            }
        });
        Properties properties = new Properties();
        try {
            File file = ResourceUtils.getFile("classpath:config/seata.txt");
            FileInputStream fileInputStream = new FileInputStream(file);
            properties.load(fileInputStream);
            for (Object key : properties.keySet()) {
                putConfig(key.toString(),properties.get(key).toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void putConfig(String key, String value) {
        String path = SEATA_NODE + "/" + key;
        if(!zkClient.exists(path)){
            zkClient.create(path,value, CreateMode.PERSISTENT);
        }else{
            zkClient.writeData(path,value);
        }
    }
}
