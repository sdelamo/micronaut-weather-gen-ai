package com.example.services.weather.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Feature(
    String id,
    String type,
    Object geometry,
    Properties properties) {
}
