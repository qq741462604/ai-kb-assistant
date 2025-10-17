package com.aiassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "okhttp")
public class OkHttpProperties {

    /**
     * 连接超时时间(毫秒)
     */
    private long connectTimeout = 10000;

    /**
     * 读取超时时间(毫秒)
     */
    private long readTimeout = 10000;

    /**
     * 写入超时时间(毫秒)
     */
    private long writeTimeout = 10000;

    /**
     * 连接失败时是否重试
     */
    private boolean retryOnConnectionFailure = true;

    /**
     * 最大空闲连接数
     */
    private int maxIdleConnections = 5;

    /**
     * 保持连接时间(毫秒)
     */
    private long keepAliveDuration = 300000;

    // getter and setter methods
    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public boolean isRetryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    public void setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
        this.retryOnConnectionFailure = retryOnConnectionFailure;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public void setKeepAliveDuration(long keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }
}