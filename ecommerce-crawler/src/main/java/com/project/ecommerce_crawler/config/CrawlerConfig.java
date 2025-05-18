package com.project.ecommerce_crawler.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CrawlerConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(30))
                ))
                .build();
    }
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000));
        return cacheManager;
    }
    
    @Bean
    public BloomFilter<String> urlBloomFilter() {
        return BloomFilter.create(
                Funnels.stringFunnel(Charset.defaultCharset()),
                1000000,
                0.01);
    }
}