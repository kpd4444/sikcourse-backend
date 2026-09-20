package com.sikcourse.backend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ai.menu-master")
public record AiMenuMasterProperties(
        String baseUrl
) {
}
