package com.example.services.ai;

import com.example.services.weather.model.Location;
import com.example.views.CardBody;
import io.micronaut.core.annotation.NonNull;

public interface WeatherCommentGenerator {
    @NonNull
    CardBody generate(@NonNull Location location);
}
