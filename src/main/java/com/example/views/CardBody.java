package com.example.views;

import io.micronaut.core.annotation.ReflectiveAccess;

@ReflectiveAccess
public record CardBody(String title, String text) {
}
