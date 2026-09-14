package com.github.walkvoid.zone.auth;

import com.github.walkvoid.zone.auth.client.SmsSendRecordClient;
import com.github.walkvoid.zone.auth.client.ZoneSystemServiceName;
import com.github.walkvoid.zone.user.client.RoleClient;
import com.github.walkvoid.zone.user.client.UserInfoClient;
import com.github.walkvoid.zone.user.client.ZoneUserServiceName;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@EnableDiscoveryClient
@ImportHttpServices(group = ZoneUserServiceName.SERVICE_NAME, types = {
        UserInfoClient.class,
        RoleClient.class
})
@ImportHttpServices(group = ZoneSystemServiceName.SERVICE_NAME, types = {
        SmsSendRecordClient.class
})
@MapperScan("com.github.walkvoid.zone.auth.db.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
