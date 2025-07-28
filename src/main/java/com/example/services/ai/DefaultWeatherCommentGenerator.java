package com.example.services.ai;

import com.example.conf.UsOracleOffice;
import com.example.services.weather.model.Location;
import com.example.views.CardBody;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.ResourceLoader;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.example.utils.PromptUtils.loadPrompt;

@Singleton
public class DefaultWeatherCommentGenerator implements WeatherCommentGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultWeatherCommentGenerator.class);
    private final WeatherForecastGenerator weatherForecastGenerator;
    private final ChatModel chatModel;
    private final SystemMessage systemMessage;
    private final String commentaryPrompt;
    private final List<UsOracleOffice> offices;

    public DefaultWeatherCommentGenerator(List<UsOracleOffice> offices,
                                   ResourceLoader resourceLoader,
                                   ChatModel chatModel,
                                   WeatherForecastGenerator weatherForecastGenerator) {
        this.offices = offices;
        this.weatherForecastGenerator = weatherForecastGenerator;
        this.chatModel = chatModel;
        systemMessage = SystemMessage.from(loadPrompt(resourceLoader,
                "classpath:prompts/system.txt",
                () -> new ConfigurationException("Could not find system prompt")));
        commentaryPrompt = loadPrompt(resourceLoader,
                "classpath:prompts/forecastCommentary.txt",
                () -> new ConfigurationException("Could not find commentary prompt"));

    }

    @Override
    @NonNull
    public CardBody generate(@NonNull Location location) {
        String cityName = offices
                .stream()
                .filter(office -> office.location().equals(location))
                .map(UsOracleOffice::getCity)
                .findFirst().orElseThrow();
        return new CardBody(cityName, generateComment(location));
    }

    @Cacheable(cacheNames = "forecastcomment")
    @NonNull
    public String generateComment(@NonNull Location location) {
        String forecast = weatherForecastGenerator.generate(location);
        try {
            return forecastComment(forecast);
        } catch (LangChain4jException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("could not generate forecast comment", e);
            }
        }
        return null;
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
}
