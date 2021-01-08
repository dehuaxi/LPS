package com.defei.lps;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.defei.lps.dao")//spring容器启动时就扫描加载了dao
public class LpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LpsApplication.class, args);
    }

}
