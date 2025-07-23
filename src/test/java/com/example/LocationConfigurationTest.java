package com.example;

import com.example.conf.UsOracleOffice;
import io.micronaut.context.BeanContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class LocationConfigurationTest {
    private final double austinLatitude = 30.3066;
    private final double austinLongitude = -97.7498;

    @Inject
    BeanContext beanContext;

    @Test
    void locationIsSet() {
        UsOracleOffice austin = beanContext.getBean(UsOracleOffice.class, Qualifiers.byName("austin"));
        assertEquals(austinLatitude, austin.getLatitude());
        assertEquals(austinLongitude, austin.getLongitude());

    }
}