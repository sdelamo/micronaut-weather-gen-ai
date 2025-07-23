package com.example.services.ai;

import com.example.views.ForecastView;
import com.example.services.weather.model.Location;

public interface WeatherChatBot {
    ForecastView forecast(Location location);
}