package com.github.redis.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisSyncApplication.class, args);
    }

}
