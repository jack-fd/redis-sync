package com.github.redis.sync.service.impl;

import com.github.redis.sync.service.RedisDataSyncService;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class RedisDataSyncServiceImpl implements RedisDataSyncService {

    @Autowired
    private RedisTemplate<String, Object> sourceRedisTemplate;

    @Autowired
    private StringRedisTemplate sourceStringRedisTemplate;

    @Autowired
    @Qualifier("targetRedisTemplate")
    private RedisTemplate<String, Object> targetRedisTemplate;

    @Autowired
    @Qualifier("targetStringRedisTemplate")
    private StringRedisTemplate targetStringRedisTemplate;

    @Value("${rewrite:true}")
    private Boolean rewrite;

    @Async("asyncServiceExecutor")
    @Override
    public void execute(Set<String> keys, CountDownLatch countDownLatch, AtomicLong atomicLong) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            for (String key : keys) {
                Boolean hasKey = targetRedisTemplate.hasKey(key);
                if (Objects.nonNull(hasKey) && hasKey && !rewrite) {
                    continue;
                }
                DataType dataType = sourceRedisTemplate.type(key);
                if (Objects.isNull(dataType)) {
                    continue;
                }
                log.info("key:{}, type:{}, 进行中....", key, dataType);
                atomicLong.incrementAndGet();
                switch (dataType) {
                    case NONE:
                        log.warn("none, 暂时没处理");
                        break;
                    case STRING:
                        copyString(key);
                        break;
                    case LIST:
                        copyList(key);
                        break;
                    case SET:
                        copySet(key);
                        break;
                    case ZSET:
                        copyZset(key);
                        break;
                    case HASH:
                        copyHash(key);
                        break;
                    default:
                        log.info("错误数据类型, KEY:{}, dateType:{}", key, dataType);
                        break;
                }
            }
        } finally {
            log.info("数据同步共耗时:{}ms", stopWatch.getTime());
            countDownLatch.countDown();
        }
    }

    private void copyString(String key) {
        Long expire = sourceStringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        String value = sourceStringRedisTemplate.opsForValue().get(key);
        if (Objects.isNull(value)) {
            return;
        }
        if (Objects.nonNull(expire) && expire > -1) {
            targetStringRedisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        } else {
            targetStringRedisTemplate.opsForValue().set(key, value);
        }
    }

    private void copyList(String key) {
        List<Object> value = sourceRedisTemplate.opsForList().range(key, 0, -1);
        if (CollectionUtils.isEmpty(value)) {
            return;
        }
        targetRedisTemplate.opsForList().leftPushAll(key, value);
        setExpire(key);
    }

    private void copySet(String key) {
//        Cursor<Object> cursor = sourceRedisTemplate.opsForSet().scan(key, ScanOptions.NONE);
        Set<Object> set = sourceRedisTemplate.opsForSet().members(key);
//        Set<Object> set = Sets.newHashSet();

        if (CollectionUtils.isEmpty(set)) {
            return;
        }
        for (Object o : set) {
            targetRedisTemplate.opsForSet().add(key, o);
        }
        setExpire(key);
    }

    private void copyZset(String key) {
        Cursor<ZSetOperations.TypedTuple<Object>> cursor = sourceRedisTemplate.opsForZSet().scan(key, ScanOptions.NONE);
        Set<ZSetOperations.TypedTuple<Object>> set = Sets.newHashSet();
        if (CollectionUtils.isEmpty(set)) {
            return;
        }
        while (cursor.hasNext()) {
            set.add(cursor.next());
        }
        targetRedisTemplate.opsForZSet().add(key, set);
        setExpire(key);
    }

    private void copyHash(String key) {
        Map map = sourceRedisTemplate.opsForHash().entries(key);
        targetRedisTemplate.opsForHash().putAll(key, map);
        setExpire(key);
    }

    private void setExpire(String key) {
        Long expire = sourceRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (Objects.isNull(expire)) {
            targetRedisTemplate.expire(key, 10, TimeUnit.SECONDS);
        }
        if (expire > -1) {
            targetRedisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }
}
