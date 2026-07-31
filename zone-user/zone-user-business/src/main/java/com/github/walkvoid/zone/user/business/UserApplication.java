package com.github.walkvoid.zone.user.business;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Zone User 服务启动类（独立 Dubbo Provider）
 *
 * @author walkvoid
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("com.github.walkvoid.zone.user.business.db.mapper")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
