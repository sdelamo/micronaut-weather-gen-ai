package com.example.services.ai;

import com.example.views.CardBody;
import com.example.services.weather.model.Location;
import io.micronaut.core.annotation.NonNull;

public interface WeatherChatBot {
    @NonNull
    CardBody forecastCard(@NonNull Location location);

    @NonNull
    String forecastImageUrl(@NonNull Location location);
}