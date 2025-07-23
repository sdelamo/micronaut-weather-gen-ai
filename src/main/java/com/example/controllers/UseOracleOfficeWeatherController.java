package com.example.controllers;

import com.example.conf.UsOracleOffice;
import com.example.services.ai.WeatherChatBot;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.View;
import jakarta.validation.constraints.Pattern;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Controller
class UseOracleOfficeWeatherController {
    public static final String REGEX = "austin|redwoodshores|morrisville|hillsboro|malvern|nashville|sanantonio|lehi|arlington|reston|seattle|irvine";
    public static final String COOKIE_NAME_ORACLE_OFFICE = "oracleoffice";

    private final Map<String, UsOracleOffice> offices;
    private final WeatherChatBot weatherChatBot;

    UseOracleOfficeWeatherController(Map<String, UsOracleOffice> offices,
                                     WeatherChatBot weatherChatBot) {
        this.offices = offices;
        this.weatherChatBot = weatherChatBot;
    }

    @Produces(MediaType.TEXT_HTML)
    @Get("/weather/{name}")
    @View("index")
    HttpResponse<Map<String, Object>> index(@PathVariable @Pattern(regexp = REGEX) String name) {
        UsOracleOffice usOracleOffice = offices.get(name);
        return HttpResponse.ok(model(usOracleOffice)).cookie(Cookie.of(COOKIE_NAME_ORACLE_OFFICE, name).path("/"));
    }

    @ExecuteOn(TaskExecutors.BLOCKING)
    @Produces(MediaType.TEXT_HTML)
    @View("card")
    @Get("/weather/{name}/forecast")
    Map<String, Object> forecast(@PathVariable @Pattern(regexp = REGEX) String name) {
        UsOracleOffice usOracleOffice = offices.get(name);
        Map<String, Object> result = new HashMap<>(model(usOracleOffice));
        result.put("forecast", weatherChatBot.forecast(usOracleOffice.location()));
        return result;
    }

    private Map<String, Object> model(UsOracleOffice usOracleOffice) {
        return Map.of(
                "offices", offices.values().stream().sorted(new Comparator<UsOracleOffice>() {
                    @Override
                    public int compare(UsOracleOffice o1, UsOracleOffice o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                }),
                "city", StringUtils.capitalize(usOracleOffice.getName()),
                "name", usOracleOffice.getName());
    }
}
