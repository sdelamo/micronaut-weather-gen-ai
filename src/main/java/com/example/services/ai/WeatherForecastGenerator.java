package com.example.services.ai;

import com.example.services.weather.model.Location;
import io.micronaut.core.annotation.NonNull;

public interface WeatherForecastGenerator {
    @NonNull
    String generate(@NonNull Location location);
}
