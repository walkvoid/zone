package com.github.walkvoid.zone.auth.business;

import com.github.walkvoid.zone.user.api.client.RoleFeignClient;
import com.github.walkvoid.zone.user.api.client.UserInfoFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {UserInfoFeignClient.class, RoleFeignClient.class})
@MapperScan("com.github.walkvoid.zone.auth.business.db.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
