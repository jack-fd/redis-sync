package com.github.redis.sync.service;

import java.util.Set;

public interface RedisSourceScanService {

    Set<String> execute();

    Set<String> execute(String matchKey);
}
