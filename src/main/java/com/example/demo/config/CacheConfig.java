package com.example.demo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Multi-layer caching configuration for optimal performance.
 * 
 * Strategy:
 * - Layer 1 (L1): Caffeine (in-memory, fast, per instance)
 * - Layer 2 (L2): Redis (distributed, shared across instances)
 * 
 * Use Caffeine for:
 * - Frequently accessed data
 * - Small data sets
 * - Read-heavy operations
 * 
 * Use Redis for:
 * - Distributed caching across multiple app instances
 * - Larger data sets
 * - Session sharing
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * Caffeine (local) cache manager - Fast L1 cache.
     * Used when Redis is not available or for local-only caching.
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "users", "posts", "topics", "groups", 
                "userProfile", "onlineUsers", "trendingPosts",
                "notifications", "comments"
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000) // Max 10k entries
                .expireAfterWrite(15, TimeUnit.MINUTES) // Expire after 15 min
                .expireAfterAccess(10, TimeUnit.MINUTES) // Expire if not accessed for 10 min
                .recordStats() // Enable statistics
        );
        
        log.info("Caffeine cache manager initialized with 9 caches");
        return cacheManager;
    }

    /**
     * Redis cache manager - Distributed L2 cache.
     * Only active when Redis is available.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 30 minutes default TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Custom TTL for specific caches
        var cacheConfigurations = java.util.Map.of(
                "users", defaultConfig.entryTtl(Duration.ofHours(1)),
                "posts", defaultConfig.entryTtl(Duration.ofMinutes(15)),
                "topics", defaultConfig.entryTtl(Duration.ofHours(2)),
                "groups", defaultConfig.entryTtl(Duration.ofHours(1)),
                "trendingPosts", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                "onlineUsers", defaultConfig.entryTtl(Duration.ofMinutes(1)),
                "notifications", defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();

        log.info("Redis cache manager initialized with distributed caching");
        return cacheManager;
    }
}
