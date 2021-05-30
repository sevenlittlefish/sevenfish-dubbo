package com.sevenfish;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@MapperScan("com.sevenfish.dao")
public class DubboProviderDemo extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DubboProviderDemo.class,args);
    }

    //若打包成war包,则需要继承 org.springframework.boot.context.web.SpringBootServletInitializer类,覆盖其config(SpringApplicationBuilder)方法
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DubboProviderDemo.class);
    }
}
