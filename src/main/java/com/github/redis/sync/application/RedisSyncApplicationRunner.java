package com.github.redis.sync.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 数据同步入口
 *
 * @author mq
 */
@Slf4j
@Component
@Order(value = 1)
public class RedisSyncApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Redis数据同步启动....");
        Thread.sleep(1300L);
        log.info("Redis数据同步完成，耗时:{}s", stopWatch.getTime(TimeUnit.SECONDS));
    }
}
