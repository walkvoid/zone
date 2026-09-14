package com.github.walkvoid.zone.system;

import com.github.walkvoid.zone.user.client.RoleClient;
import com.github.walkvoid.zone.user.client.RoleMenuRelClient;
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
        RoleClient.class,
        RoleMenuRelClient.class
})
@MapperScan("com.github.walkvoid.zone.system.db.mapper")
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
