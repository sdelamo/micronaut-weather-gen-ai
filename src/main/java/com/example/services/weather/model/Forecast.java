package com.example.services.weather.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Forecast(ForecastProperties properties) {
}
