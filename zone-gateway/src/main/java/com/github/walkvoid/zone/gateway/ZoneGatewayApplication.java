package com.github.walkvoid.zone.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ZoneGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZoneGatewayApplication.class, args);
    }
}
