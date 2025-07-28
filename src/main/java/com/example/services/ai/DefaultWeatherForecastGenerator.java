package com.example.services.ai;

import com.example.services.weather.WeatherClient;
import com.example.services.weather.model.Location;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

@Singleton
public class DefaultWeatherForecastGenerator implements WeatherForecastGenerator {
    private final WeatherClient weatherClient;

    public DefaultWeatherForecastGenerator(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @Cacheable(cacheNames = "forecast")
    @Override
    @NonNull
    public String generate(@NonNull Location location) {
        return weatherClient.formattedForecast(location);
    }
}
