package com.github.walkvoid.zone.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author walkvoid
 * @version 1.0
 * @date 2025/12/5
 * @desc
 */

@SpringBootApplication
public class ZoneGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZoneGatewayApplication.class, args);

//        UPDATE `jinkoscf_transaction`.`pay_trade` SET pay_status='wait_commit',id=1996716154263302147 WHERE id = 1996716154263302146;
//
//        UPDATE `jinkoscf_transaction`.`pay_trade` SET pay_status='wait_commit',id=1996716153399275522 WHERE id = 1996716153399275521;
    }
}
