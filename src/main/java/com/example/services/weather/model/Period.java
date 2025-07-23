package com.example.services.weather.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Period(
    String name,
    int temperature,
    String temperatureUnit,
    String windSpeed,
    String windDirection,
    String detailedForecast) {
}
