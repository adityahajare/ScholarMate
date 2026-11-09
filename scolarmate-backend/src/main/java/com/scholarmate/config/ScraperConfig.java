package com.scholarmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ScraperConfig {

    @Bean
    public ExecutorService scraperExecutor(@Value("${scholarmate.scraper.max-concurrent-sources:6}") int threads) {
        return Executors.newFixedThreadPool(threads);
    }
}
