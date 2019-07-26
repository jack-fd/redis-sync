package com.github.redis.sync.application;

import com.github.redis.sync.service.RedisDataSyncService;
import com.github.redis.sync.service.RedisSourceScanService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis 数据同步入口
 *
 * @author mq
 */
@Slf4j
@Component
@Order(value = 1)
public class RedisSyncApplicationRunner implements ApplicationRunner {

    @Autowired
    private RedisSourceScanService redisSourceScanService;

    @Autowired
    private RedisDataSyncService redisDataSyncService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Redis数据同步启动....");
        Set<String> scanKeys = redisSourceScanService.execute();
        log.info("scanKeys:{}", scanKeys.size());
        List<String> stringList = Lists.newArrayList(scanKeys);
        List<List<String>> list = Lists.partition(stringList, 10000);
        CountDownLatch countDownLatch = new CountDownLatch(list.size());
        AtomicLong atomicLong = new AtomicLong();
        for (List<String> strings : list) {
            redisDataSyncService.execute(Sets.newHashSet(strings), countDownLatch, atomicLong);
        }
        countDownLatch.await();
        log.info(atomicLong.toString());
        log.info("Redis数据同步完成，耗时:{}ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }
}
