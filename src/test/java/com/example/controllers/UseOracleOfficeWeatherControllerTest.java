package com.example.controllers;

import com.example.services.ai.DefaultWeatherCommentGenerator;
import com.example.services.ai.DefaultWeatherImageGeneration;
import com.example.services.ai.WeatherCommentGenerator;
import com.example.services.ai.WeatherImageGeneration;
import com.example.services.weather.model.Location;
import com.example.views.CardBody;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisabledInNativeImage
@MicronautTest
class UseOracleOfficeWeatherControllerTest {
    private final double austinLatitude = 30.3066;
    private final double austinLongitude = -97.7498;
    private static final CardBody CARD = new CardBody("Austin", """
    Get ready for the weather rollercoaster! Today you'll sizzle at 92°F but might get rained on just enough to smear your sunglasses. Mother Nature’s got trust issues, so don’t trust her—pack sunscreen and an umbrella, just in case.""");
    private static final String IMAGE_URL =
            "https://oaidalleapiprodscus.blob.core.windows.net/private/org-DnFzHMeMDIUON73ywCGSinMt/user-H9ynsd1daAU72qPFEmA3pLHh/img-Z4ktfg6QWXLtMMM38TxHDNdQ.png?st=2025-07-23T08%3A02%3A59Z&se=2025-07-23T10%3A02%3A59Z&sp=r&sv=2024-08-04&sr=b&rscd=inline&rsct=image/png&skoid=cc612491-d948-4d2e-9821-2683df3719f5&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2025-07-23T03%3A02%3A25Z&ske=2025-07-24T03%3A02%3A25Z&sks=b&skv=2024-08-04&sig=EOp5y222H0Dxg4lD45xU2/cOaK7OwKK%2Bi8jaiUI95fw%3D";

    @Inject
    WeatherCommentGenerator weatherCommentGenerator;

    @Inject
    WeatherImageGeneration weatherImageGeneration;

    @Test
    void weatherForecastCard(@Client("/") HttpClient httpClient) {
        when(weatherCommentGenerator.generate(new Location(austinLatitude, austinLongitude))).thenReturn(CARD);
        BlockingHttpClient client = httpClient.toBlocking();
        var request = HttpRequest.GET("/weather/austin/forecast/card").accept(MediaType.TEXT_HTML);
        assertDoesNotThrow(() -> client.exchange(request));
    }

    @Test
    void weatherForecastCardImage(@Client("/") HttpClient httpClient) {
        when(weatherImageGeneration.forecastImageBase64DataUrl(new Location(austinLatitude, austinLongitude))).thenReturn(IMAGE_URL);
        BlockingHttpClient client = httpClient.toBlocking();
        var request = HttpRequest.GET("/weather/austin/forecast/image").accept(MediaType.TEXT_HTML);
        assertDoesNotThrow(() -> client.exchange(request));
    }

    @Test
    void weather(@Client("/") HttpClient httpClient) {
        BlockingHttpClient client = httpClient.toBlocking();
        var request = HttpRequest.GET("/weather/austin").accept(MediaType.TEXT_HTML);
        assertDoesNotThrow(() -> client.exchange(request));
    }

    @MockBean(DefaultWeatherImageGeneration.class)
    WeatherImageGeneration weatherImageGeneration() {
        return mock(WeatherImageGeneration.class);
    }

    @MockBean(DefaultWeatherCommentGenerator.class)
    WeatherCommentGenerator weatherCommentGenerator() {
        return mock(WeatherCommentGenerator.class);
    }
}
