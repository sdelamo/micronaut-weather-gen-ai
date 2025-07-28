package com.example.services.ai;

import com.example.conf.ImageGeneratorConfiguration;
import com.example.conf.UsOracleOffice;
import com.example.utils.ImageUtils;
import com.example.views.CardBody;
import com.example.services.weather.model.Location;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.exception.LangChain4jException;
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

import java.util.List;
import java.util.Optional;

import static com.example.utils.PromptUtils.loadPrompt;

@Singleton
public class DefaultWeatherImageGeneration implements WeatherImageGeneration {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultWeatherImageGeneration.class);
    private final String imagePrompt;
    private final ImageModel imageModel;
    private final ImageGeneratorConfiguration imageGeneratorConfiguration;
    private final WeatherForecastGenerator weatherForecastGenerator;
    private final WeatherCommentGenerator weatherCommentGenerator;

    public DefaultWeatherImageGeneration(ResourceLoader resourceLoader,
                                 List<UsOracleOffice> offices,
                                 ImageGeneratorConfiguration imageGeneratorConfiguration,
                                 WeatherForecastGenerator weatherForecastGenerator,
                                 @Nullable ImageModel imageModel,
                                 WeatherCommentGenerator weatherCommentGenerator) {
        this.weatherForecastGenerator = weatherForecastGenerator;
        this.imageModel = imageModel;
        this.weatherCommentGenerator = weatherCommentGenerator;
        this.imageGeneratorConfiguration = imageGeneratorConfiguration;
        imagePrompt = loadPrompt(resourceLoader,
                "classpath:prompts/forecastImage.txt",
                () -> new ConfigurationException("Could not find image prompt"));
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
        String forecast = weatherForecastGenerator.generate(location);
        CardBody card = weatherCommentGenerator.generate(location);
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

    public Optional<Image> generateImageUrl(String forecast, CardBody card) throws LangChain4jException {
        if (imageModel == null) {
            return Optional.empty();
        }
        Response<Image> image = imageModel.generate(String.format(imagePrompt, card.text(), card.title(), forecast));
        return Optional.of(image.content());
    }
}
