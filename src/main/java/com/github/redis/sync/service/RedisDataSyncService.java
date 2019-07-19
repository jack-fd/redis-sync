package com.github.redis.sync.service;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * redis 数据同步
 *
 * @author mq
 */
public interface RedisDataSyncService {

    void execute(Set<String> scanKeys, AtomicLong atomicLong);
}