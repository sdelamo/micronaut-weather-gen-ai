package com.example.controllers;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.CookieValue;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.uri.UriBuilder;
import jakarta.validation.constraints.Pattern;

import static com.example.controllers.UseOracleOfficeWeatherController.*;
import static com.example.controllers.UseOracleOfficeWeatherController.COOKIE_NAME_ORACLE_OFFICE;

@Controller
class HomeController {
    private static final String DEFAULT_LOCATION = "austin";

    @Get
    HttpResponse<?> index(@Nullable @CookieValue(COOKIE_NAME_ORACLE_OFFICE) @Pattern(regexp = REGEX) String name) {
        return HttpResponse.seeOther(
                UriBuilder.of("weather").path(name != null ? name : DEFAULT_LOCATION).build());
    }
}
