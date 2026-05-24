package com.hdl.soar.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.hdl.soar.server",
        "com.hdl.soar.module"
})
public class SoarApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoarApplication.class, args);
    }

}