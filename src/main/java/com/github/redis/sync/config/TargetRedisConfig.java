package com.github.redis.sync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class TargetRedisConfig extends RedisConfig {

    @Value("${spring.target.database}")
    private int dbIndex;

    @Value("${spring.target.host}")
    private String host;

    @Value("${spring.target.port}")
    private int port;

    @Value("${spring.target.password}")
    private String password;

    @Bean
    public JedisConnectionFactory targetRedisConnectionFactory() {
        return newJedisConnectionFactory(dbIndex, host, port, password);
    }

    @Bean
    public RedisTemplate targetRedisTemplate() {
        RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(targetRedisConnectionFactory());
        setSerializer(template);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate targetStringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(targetRedisConnectionFactory());
        template.afterPropertiesSet();
        return template;
    }
}