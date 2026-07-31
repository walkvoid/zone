package com.github.walkvoid.zone.system.business;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Zone System 服务启动类（独立 Dubbo Provider）
 *
 * @author walkvoid
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("com.github.walkvoid.zone.system.business.db.mapper")
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
