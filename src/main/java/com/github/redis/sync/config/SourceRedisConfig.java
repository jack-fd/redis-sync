package com.github.redis.sync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class SourceRedisConfig extends RedisConfig {

    @Value("${spring.source.database}")
    private int dbIndex;

    @Value("${spring.source.host}")
    private String host;

    @Value("${spring.source.port}")
    private int port;

    @Value("${spring.source.password}")
    private String password;

    @Primary
    @Bean
    public JedisConnectionFactory sourceRedisConnectionFactory() {
        return newJedisConnectionFactory(dbIndex, host, port, password);
    }

    @Primary
    @Bean
    public RedisTemplate redisTemplate() {
        RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(sourceRedisConnectionFactory());
        setSerializer(template);
        template.afterPropertiesSet();
        return template;
    }

    @Primary
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(sourceRedisConnectionFactory());
        template.afterPropertiesSet();
        return template;
    }
}

