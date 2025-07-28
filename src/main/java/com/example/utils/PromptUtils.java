package com.example.utils;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public final class PromptUtils {
    private PromptUtils() {

    }

    public static String loadPrompt(ResourceLoader resourceLoader, String classpath, Supplier<? extends Throwable> exceptionSupplier) {
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
