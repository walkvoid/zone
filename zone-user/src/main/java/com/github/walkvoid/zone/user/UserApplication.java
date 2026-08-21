package com.github.walkvoid.zone.user;

import com.github.walkvoid.zone.user.client.UserCredentialFeignClient;
import com.github.walkvoid.zone.user.client.UserIdentityFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {UserCredentialFeignClient.class, UserIdentityFeignClient.class})
@MapperScan("com.github.walkvoid.zone.user.db.mapper")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
