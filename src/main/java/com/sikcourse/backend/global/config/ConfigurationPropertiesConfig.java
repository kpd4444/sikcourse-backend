package com.sikcourse.backend.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        TourApiProperties.class,
        JwtProperties.class,
        GeminiProperties.class,
        CorsProperties.class,
        AiMenuMasterProperties.class,
        AdminProperties.class
})
public class ConfigurationPropertiesConfig {
}
