package com.pathonxgen.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Redis connection/serialization configured via application.yml
    // (spring.data.redis.*); default RedisCacheManager auto-configured
    // by Spring Boot is sufficient for module-level @Cacheable usage.
}
