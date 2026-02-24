package com.modoria.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import java.util.Objects;

/**
 * Redis cache configuration.
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

        private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Objects.requireNonNull(Duration.ofMinutes(30)))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                                .disableCachingNullValues();

                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
                cacheConfigurations.put("products",
                                defaultConfig.entryTtl(Objects.requireNonNull(Duration.ofHours(1))));
                cacheConfigurations.put("featured-products",
                                defaultConfig.entryTtl(Objects.requireNonNull(Duration.ofMinutes(15))));
                cacheConfigurations.put("categories",
                                defaultConfig.entryTtl(Objects.requireNonNull(Duration.ofHours(2))));
                cacheConfigurations.put("current-season",
                                defaultConfig.entryTtl(Objects.requireNonNull(Duration.ofHours(1))));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }

        @Override
        public CacheErrorHandler errorHandler() {
                return new SimpleCacheErrorHandler() {
                        @Override
                        public void handleCacheGetError(@NonNull RuntimeException exception,
                                        @NonNull org.springframework.cache.Cache cache, @NonNull Object key) {
                                log.error("Redis cache GET error for key {}: {}", key, exception.getMessage());
                        }

                        @Override
                        public void handleCachePutError(@NonNull RuntimeException exception,
                                        @NonNull org.springframework.cache.Cache cache, @NonNull Object key,
                                        @Nullable Object value) {
                                log.error("Redis cache PUT error for key {}: {}", key, exception.getMessage());
                        }

                        @Override
                        public void handleCacheEvictError(@NonNull RuntimeException exception,
                                        @NonNull org.springframework.cache.Cache cache, @NonNull Object key) {
                                log.error("Redis cache EVICT error for key {}: {}", key, exception.getMessage());
                        }

                        @Override
                        public void handleCacheClearError(@NonNull RuntimeException exception,
                                        @NonNull org.springframework.cache.Cache cache) {
                                log.error("Redis cache CLEAR error: {}", exception.getMessage());
                        }
                };
        }
}

