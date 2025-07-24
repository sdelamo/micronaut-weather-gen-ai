package com.example.services.ai;

import com.example.conf.ImageGeneratorConfiguration;
import com.example.views.CardBody;
import com.example.services.weather.model.Location;
import com.example.services.weather.WeatherClient;
import dev.langchain4j.community.model.oracle.oci.genai.OciGenAiChatModel;
import dev.langchain4j.community.model.oracle.oci.genai.OciGenAiCohereChatModel;
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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

import java.util.List;

@CacheConfig("forecasts")
@Singleton
public class DefaultWeatherChatBot implements WeatherChatBot {
    private static final SystemMessage SYSTEM_MSG = SystemMessage.from("""
            You are a crazy-powerful weather chatbot that delivers hilariously twisted forecasts.
            The snarky character makes the app fun and engaging.""");
    private final WeatherClient weatherClient;
    private final ChatModel chatModel;
    private final ImageModel imageModel;
    private final ImageGeneratorConfiguration imageGeneratorConfiguration;
    public DefaultWeatherChatBot(WeatherClient weatherClient,
                                 List<ChatModel> chatModels,
                                 ImageModel imageModel,
                                 ImageGeneratorConfiguration imageGeneratorConfiguration) {
        this.weatherClient = weatherClient;
        this.chatModel = chatModels.stream().filter(m -> m instanceof OciGenAiChatModel || m instanceof OciGenAiCohereChatModel).findFirst().orElseThrow();
        this.imageModel = imageModel;
        this.imageGeneratorConfiguration = imageGeneratorConfiguration;
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
        return generateImageUrl(forecast);
    }

    @Cacheable(cacheNames = "forecast")
    public String weatherForecast(Location location) {
        return weatherClient.formattedForecast(location);
    }

    private String cityName(Location location) {
        String str = String.format("Give the city name given the latitude %s and longitude %s . The answer should be only the city name. Don't add any extra comments.", location.latitude(), location.longitude());
        List<ChatMessage> messages = List.of(SYSTEM_MSG, UserMessage.from(str));
        ChatResponse chatResponse = chatModel.chat(messages);
        return chatResponse.aiMessage().text();
    }

    public String generateImageUrl(String forecast) {
        if (imageModel == null) {
            return imageGeneratorConfiguration.getDefaultWeatherImageUrl();
        }
        Response<Image> image = imageModel.generate("Generate an image for the following weather forecast. Only focus on today's weather. The image should contain the temperature and also an image for the weather conditions (for example, sun, rain clouds, lighting, etc). Use a blue background. This is the forecast: " +  forecast);
        return image.content().url().toString();
    }

    public String forecastComment(String forecast) {
        List<ChatMessage> messages = messages(forecast);
        ChatResponse chatResponse = chatModel.chat(messages);
        return chatResponse.aiMessage().text();
    }

    private static List<ChatMessage> messages(String forecast) {
        return List.of(SYSTEM_MSG, UserMessage.from(
                String.format("Generate a snarky comment, maximum 255 characters, about today's weather for this weather forecast %s", forecast)
        ));
    }
}
