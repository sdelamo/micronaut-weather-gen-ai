package com.example.conf;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("app")
public interface ImageGeneratorConfiguration {
    String getDefaultWeatherImageUrl();
    String getDefaultWeatherImageMimeType();
}
