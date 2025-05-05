package com.rawneeded;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.rawneeded.*")
public class RawNeededApplication {

    public static void main(String[] args) {
        System.setProperty("server.servlet.context-path", "/raw-needed");
        SpringApplication.run(RawNeededApplication.class, args);
    }


}
