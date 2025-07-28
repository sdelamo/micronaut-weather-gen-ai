package com.example.services.ai;

import com.example.services.weather.model.Location;
import io.micronaut.core.annotation.NonNull;

public interface WeatherImageGeneration {
    @NonNull
    String forecastImageBase64DataUrl(@NonNull Location location);
}
