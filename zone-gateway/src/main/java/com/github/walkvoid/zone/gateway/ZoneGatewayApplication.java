package com.github.walkvoid.zone.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author walkvoid
 * @version 1.0
 * @date 2025/12/5
 * @desc Zone Gateway 启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.github.walkvoid.zone")
@MapperScan("com.github.walkvoid.zone.**.db.mapper")
public class ZoneGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZoneGatewayApplication.class, args);
    }
}
