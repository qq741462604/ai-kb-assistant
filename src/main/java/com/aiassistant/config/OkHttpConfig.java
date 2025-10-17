package com.aiassistant.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpConfig {

    @Bean
    @ConfigurationProperties(prefix = "okhttp")
    public OkHttpProperties okHttpProperties() {
        return new OkHttpProperties();
    }

    @Bean
    public OkHttpClient okHttpClient(OkHttpProperties properties) {
        return new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(properties.isRetryOnConnectionFailure())
                .connectionPool(new ConnectionPool(
                        properties.getMaxIdleConnections(),
                        properties.getKeepAliveDuration(),
                        TimeUnit.MILLISECONDS
                ))
                .build();
    }
}