package com.defei.lps;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 部署到外部tomcat容器时，从此入口进入程序
 */
@MapperScan(basePackages = "com.defei.lps.dao")//spring容器启动时就扫描加载了dao
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(LpsApplication.class);
    }

}
