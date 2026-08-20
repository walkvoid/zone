package com.github.walkvoid.zone.system.business;

import com.github.walkvoid.zone.user.api.client.RoleFeignClient;
import com.github.walkvoid.zone.user.api.client.RoleMenuRelFeignClient;
import com.github.walkvoid.zone.user.api.client.UserInfoFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {UserInfoFeignClient.class, RoleFeignClient.class, RoleMenuRelFeignClient.class})
@MapperScan("com.github.walkvoid.zone.system.business.db.mapper")
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
