package com.example.views;

import io.micronaut.core.annotation.ReflectiveAccess;

@ReflectiveAccess
public record ForecastView(String city,
                           String comment,
                           String imageUrl) {
}
