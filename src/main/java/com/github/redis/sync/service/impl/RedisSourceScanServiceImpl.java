package com.github.redis.sync.service.impl;

import com.github.redis.sync.service.RedisSourceScanService;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class RedisSourceScanServiceImpl implements RedisSourceScanService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${filter.key.list}")
    private String keyListStr;

    @Value("${scan.key_number:100}")
    private Integer scanNumber;

    @Override
    public Set<String> execute() {
        if (StringUtils.isBlank(keyListStr)) {
            keyListStr = "*";
        }
        String[] keyList = keyListStr.split(",");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Set<String> result = Sets.newHashSet();
        for (String matchKey : keyList) {
            String key = matchKey.trim() + "*";
            result.addAll(execute(key));
        }
        log.info("scan:{}, 耗时{}ms", keyListStr, stopWatch.getTime());
        return result;
    }

    @Override
    public Set<String> execute(String matchKey) {
        return redisTemplate.execute((RedisConnection connection) -> {
            Set<String> sets = Sets.newHashSet();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(matchKey).count(scanNumber).build());
            while (cursor.hasNext()) {
                sets.add(new String(cursor.next()));
            }
            return sets;
        });
    }
}
