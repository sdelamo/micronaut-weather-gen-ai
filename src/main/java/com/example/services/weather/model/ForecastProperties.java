package com.example.services.weather.model;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record ForecastProperties(
    List<Period> periods) {
}
