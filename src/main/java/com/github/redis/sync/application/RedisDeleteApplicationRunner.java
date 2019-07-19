package com.github.redis.sync.application;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j

public class RedisDeleteApplicationRunner implements ApplicationRunner {

    @Autowired
    @Qualifier("targetRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Set<String> keys = redisTemplate.execute((RedisConnection connection) -> {
            Set<String> sets = Sets.newHashSet();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("*").count(1000).build());
            while (cursor.hasNext()) {
                sets.add(new String(cursor.next()));
            }
            return sets;
        });
        log.info(String.valueOf(keys));
        redisTemplate.delete(keys);
    }
}
