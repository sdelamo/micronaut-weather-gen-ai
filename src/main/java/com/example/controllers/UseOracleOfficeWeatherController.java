package com.example.controllers;

import com.example.conf.UsOracleOffice;
import com.example.services.ai.WeatherCommentGenerator;
import com.example.services.ai.WeatherImageGeneration;
import com.example.views.CardBody;
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
import java.util.HashMap;
import java.util.Map;

@Controller
class UseOracleOfficeWeatherController {
    public static final String REGEX = "austin|redwoodshores|morrisville|hillsboro|malvern|nashville|sanantonio|lehi|arlington|reston|seattle|irvine";
    public static final String COOKIE_NAME_ORACLE_OFFICE = "oracleoffice";
    private static final String KEY_OFFICES = "offices";
    private static final String KEY_CITY = "city";
    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE_URL = "imageUrl";
    private static final String KEY_FORECAST = "forecast";

    private final Map<String, UsOracleOffice> offices;
    private final WeatherImageGeneration weatherImageGeneration;
    private final WeatherCommentGenerator weatherCommentGenerator;

    UseOracleOfficeWeatherController(Map<String, UsOracleOffice> offices,
                                     WeatherImageGeneration weatherImageGeneration,
                                     WeatherCommentGenerator weatherCommentGenerator) {
        this.offices = offices;
        this.weatherImageGeneration = weatherImageGeneration;
        this.weatherCommentGenerator = weatherCommentGenerator;
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
    @View("cardBody")
    @Get("/weather/{name}/forecast/card")
    Map<String, Object> forecastCard(@PathVariable @Pattern(regexp = REGEX) String name) {
        UsOracleOffice usOracleOffice = offices.get(name);
        Map<String, Object> result = new HashMap<>(model(usOracleOffice));
        CardBody card = weatherCommentGenerator.generate(usOracleOffice.location());
        result.put(KEY_FORECAST, card);
        return result;
    }

    @ExecuteOn(TaskExecutors.BLOCKING)
    @Produces(MediaType.TEXT_HTML)
    @View("cardImage")
    @Get("/weather/{name}/forecast/image")
    Map<String, Object> forecastImageUrl(@PathVariable @Pattern(regexp = REGEX) String name) {
        UsOracleOffice usOracleOffice = offices.get(name);
        Map<String, Object> result = new HashMap<>(model(usOracleOffice));
        result.put(KEY_IMAGE_URL, weatherImageGeneration.forecastImageBase64DataUrl(usOracleOffice.location()));
        return result;
    }

    private Map<String, Object> model(UsOracleOffice usOracleOffice) {
        return Map.of(
                KEY_OFFICES, offices.values().stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())),
                KEY_CITY, StringUtils.capitalize(usOracleOffice.getName()),
                KEY_NAME, usOracleOffice.getName());
    }
}
