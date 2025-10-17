package com.aiassistant.config;

import com.aiassistant.common.JsonArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MyBatisTypeHandlerConfig {

    @Bean
    public ConfigurationCustomizer typeHandlerCustomizer() {
        return configuration -> configuration.getTypeHandlerRegistry()
                .register(List.class, JdbcType.OTHER, new JsonArrayTypeHandler());
    }
}
