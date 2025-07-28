package com.example.services.ai;

import com.example.conf.ImageGeneratorConfiguration;
import com.example.conf.UsOracleOffice;
import com.example.utils.ImageUtils;
import com.example.views.CardBody;
import com.example.services.weather.model.Location;
import com.example.services.weather.WeatherClient;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Singleton
public class DefaultWeatherChatBot implements WeatherChatBot, ImageGeneration {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultWeatherChatBot.class);
    private final SystemMessage systemMessage;
    private final String cityPrompt;
    private final String commentaryPrompt;
    private final String imagePrompt;
    private final WeatherClient weatherClient;
    private final ChatModel chatModel;
    private final ImageModel imageModel;
    private final List<UsOracleOffice> offices;
    private final ImageGeneratorConfiguration imageGeneratorConfiguration;

    public DefaultWeatherChatBot(WeatherClient weatherClient,
                                 ChatModel chatModel,
                                 @Nullable ImageModel imageModel,
                                 List<UsOracleOffice> offices,
                                 ImageGeneratorConfiguration imageGeneratorConfiguration,
                                 ResourceLoader resourceLoader) {
        this.weatherClient = weatherClient;
        this.chatModel = chatModel;
        this.imageModel = imageModel;
        this.offices = offices;
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

    @Override
    @NonNull
    public CardBody forecastCard(@NonNull Location location) {
        String comment = forecastComment(location);
        if (comment == null) {
            comment = "";
        }
        return new CardBody(cityName(location), comment);
    }

    @Cacheable(cacheNames = "forecastcomment")
    public String forecastComment(@NonNull Location location) {
        String forecast = weatherForecast(location);
        try {
            return forecastComment(forecast);
        } catch (LangChain4jException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("could not generate forecast comment", e);
            }
        }
        return null;
    }

    @Cacheable(cacheNames = "forecast")
    public String weatherForecast(Location location) {
        return weatherClient.formattedForecast(location);
    }

    @Cacheable(cacheNames = "forecastimage")
    @Nullable
    public String forecastGenAiImageBase64DataUrl(@NonNull Location location) throws LangChain4jException {
        GenAiImage genAiImage = forecastGenAiImage(location);
        if (genAiImage != null) {
            try {
                return ImageUtils.toBase64DataUrl(genAiImage.url().toString(), genAiImage.mimeType());
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("error generating bas64 of default image", e);
                }
            }
        }
        return null;
    }

    @Override
    @Nullable
    public String forecastImageBase64DataUrl(@NonNull Location location) {
        String base64DataUrl =  forecastGenAiImageBase64DataUrl(location);
        if (StringUtils.isNotEmpty(base64DataUrl)) {
            return base64DataUrl;
        }
        if (StringUtils.isNotEmpty(imageGeneratorConfiguration.getDefaultWeatherImageUrl())) {
            try {
                return ImageUtils.toBase64DataUrl(imageGeneratorConfiguration.getDefaultWeatherImageUrl(), imageGeneratorConfiguration.getDefaultWeatherImageMimeType());
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("error generating bas64 of default image", e);
                }
            }
        }
        return null;
    }

    public GenAiImage forecastGenAiImage(@NonNull Location location) throws LangChain4jException {
        String forecast = weatherForecast(location);
        CardBody card = forecastCard(location);
        try {
            Optional<Image> imageOptional = generateImageUrl(forecast, card);
            if (imageOptional.isPresent()) {
                Image image = imageOptional.get();

                String mimeType = image.mimeType();
                if (StringUtils.isNotEmpty(mimeType) && mimeType.contains(MediaType.IMAGE_PNG)) {
                    mimeType = MediaType.IMAGE_PNG;
                }
                return new GenAiImage(image.url(), mimeType);
            }
        } catch (LangChain4jException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("error generating bas64 image data", e);
            }
        }
        return null;
    }

    @Cacheable(cacheNames = "forecastcity")
    @NonNull
    public String cityName(Location location) {
        return offices.stream()
                .filter(office -> office.location().equals(location))
                .map(UsOracleOffice::getCity)
                .findFirst()
                .orElseGet(() -> {
                    String str = String.format(cityPrompt, location.latitude(), location.longitude());
                    List<ChatMessage> messages = List.of(systemMessage, UserMessage.from(str));
                    try {
                        ChatResponse chatResponse = chatModel.chat(messages);
                        return chatResponse.aiMessage().text();
                    } catch (LangChain4jException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("error generating image", e);
                        }
                    }
                    return "";
                });
    }

    public Optional<Image> generateImageUrl(String forecast, CardBody card) throws LangChain4jException {
        if (imageModel == null) {
            return Optional.empty();
        }
        Response<Image> image = imageModel.generate(String.format(imagePrompt, card.text(), card.title(), forecast));
        return Optional.of(image.content());
    }

    public String forecastComment(String forecast) throws LangChain4jException {
        List<ChatMessage> messages = messages(forecast);
        ChatResponse chatResponse = chatModel.chat(messages);
        return chatResponse.aiMessage().text();
    }

    private List<ChatMessage> messages(String forecast) {
        return List.of(systemMessage, UserMessage.from(
                String.format(commentaryPrompt, forecast)
        ));
    }

    private static String loadPrompt(ResourceLoader resourceLoader, String classpath, Supplier<? extends Throwable> exceptionSupplier) {
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
}
