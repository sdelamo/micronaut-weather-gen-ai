package com.example.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.uri.UriBuilder;

@Controller
class HomeController {
    private static final String DEFAULT_LOCATION = "austin";

    @Get
    HttpResponse<?> index() {
        return HttpResponse.seeOther(
                UriBuilder.of("weather").path(DEFAULT_LOCATION).build());
    }
}
