package com.github.walkvoid.zone.user;

import com.github.walkvoid.zone.user.client.UserCredentialClient;
import com.github.walkvoid.zone.user.client.UserIdentityClient;
import com.github.walkvoid.zone.user.client.ZoneAuthServiceName;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@EnableDiscoveryClient
@ImportHttpServices(group = ZoneAuthServiceName.SERVICE_NAME, types = {
        UserCredentialClient.class,
        UserIdentityClient.class
})
@MapperScan("com.github.walkvoid.zone.user.db.mapper")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
