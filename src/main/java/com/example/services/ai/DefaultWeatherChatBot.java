package com.example.services.ai;

import com.example.conf.ImageGeneratorConfiguration;
import com.example.conf.UsOracleOffice;
import com.example.views.CardBody;
import com.example.services.weather.model.Location;
import com.example.services.weather.WeatherClient;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.ResourceLoader;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

@CacheConfig("forecasts")
@Singleton
public class DefaultWeatherChatBot implements WeatherChatBot {
    private final SystemMessage systemMessage;
    private final String cityPrompt;
    private final String commentaryPrompt;
    private final String imagePrompt;
    private final WeatherClient weatherClient;
    private final ChatModel chatModel;
    private final ImageModel imageModel;
    private final ImageGeneratorConfiguration imageGeneratorConfiguration;
    public DefaultWeatherChatBot(WeatherClient weatherClient,
                                 ChatModel chatModel,
                                 @Nullable ImageModel imageModel,
                                 ImageGeneratorConfiguration imageGeneratorConfiguration,
                                 ResourceLoader resourceLoader) {
        this.weatherClient = weatherClient;
        this.chatModel = chatModel;
        this.imageModel = imageModel;
        this.imageGeneratorConfiguration = imageGeneratorConfiguration;
        systemMessage = SystemMessage.from(loadPrompt(resourceLoader,
                "classpath:prompts/system.txt",
                () -> new ConfigurationException("Could not find system prompt")));
        imagePrompt = loadPrompt(resourceLoader,
                "classpath:prompts/forecastImage.txt",
                () -> new ConfigurationException("Could not find image prompt"));
        commentaryPrompt = loadPrompt(resourceLoader,
                "classpath:prompts/forecastCommentary.txt",
                () -> new ConfigurationException("Could not find commentary prompt"));
        cityPrompt = loadPrompt(resourceLoader,
                "classpath:prompts/coordinatesCity.txt",
                () -> new ConfigurationException("Could not find city prompt"));
    }

    private String loadPrompt(ResourceLoader resourceLoader, String classpath, Supplier<? extends Throwable> exceptionSupplier) {
        try {
            try {
                InputStream inputStream = resourceLoader.getResourceAsStream(classpath).orElseThrow(exceptionSupplier);
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                throw exceptionSupplier.get();
            }
        } catch (Throwable throwable) {
            throw new ConfigurationException("error while loading prompt");
        }
    }

    @Override
    @NonNull
    @Cacheable(cacheNames = "forecastCard")
    public CardBody forecastCard(@NonNull Location location) {
        String forecast = weatherForecast(location);
        return new CardBody(cityName(location), forecastComment(forecast));
    }

    @Override
    @NonNull
    @Cacheable(cacheNames = "forecastImageUrl")
    public String forecastImageUrl(@NonNull Location location) {
        String forecast = weatherForecast(location);
        CardBody card = forecastCard(location);
        return generateImageUrl(forecast, card);
    }

    @Cacheable(cacheNames = "forecast")
    public String weatherForecast(Location location) {
        return weatherClient.formattedForecast(location);
    }

    private String cityName(Location location) {
        String str = String.format(cityPrompt, location.latitude(), location.longitude());
        List<ChatMessage> messages = List.of(systemMessage, UserMessage.from(str));
        ChatResponse chatResponse = chatModel.chat(messages);
        return chatResponse.aiMessage().text();
    }

    public String generateImageUrl(String forecast, CardBody card) {
        if (imageModel == null) {
            return imageGeneratorConfiguration.getDefaultWeatherImageUrl();
        }
        Response<Image> image = imageModel.generate(String.format(imagePrompt, card.text(), card.title(), forecast));
        return image.content().url().toString();
    }

    public String forecastComment(String forecast) {
        List<ChatMessage> messages = messages(forecast);
        ChatResponse chatResponse = chatModel.chat(messages);
        return chatResponse.aiMessage().text();
    }

    private List<ChatMessage> messages(String forecast) {
        return List.of(systemMessage, UserMessage.from(
                String.format(commentaryPrompt, forecast)
        ));
    }
}
