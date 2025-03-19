package com.minionslab.core.common.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for caching in the application
 */
@Configuration
@EnableCaching
public class CacheConfig {

  /**
   * Configure the cache manager with Caffeine
   */
  @Bean
  @Primary
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();

    // Configure default cache settings
    cacheManager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(30))
        .recordStats());

    // Pre-define our caches
    cacheManager.setCacheNames(java.util.Arrays.asList(
        "agentCapabilities",
        "bestAgentForTask",
        "agentPrompts"
    ));

    return cacheManager;
  }

  /**
   * Configure a separate cache manager for long-lived caches
   */
  @Bean
  public CacheManager longLivedCacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();

    // Configure with longer expiration times
    cacheManager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(Duration.ofHours(12))
        .recordStats());

    return cacheManager;
  }

  /**
   * Configure a separate cache manager for short-lived caches
   */
  @Bean
  public CacheManager shortLivedCacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();

    // Configure with shorter expiration times for transient data
    cacheManager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(2000)
        .expireAfterWrite(Duration.ofMinutes(5))
        .recordStats());

    return cacheManager;
  }
}