package com.example.conf;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.naming.Named;
import com.example.services.weather.model.Location;
import io.micronaut.core.util.StringUtils;

@ReflectiveAccess
@EachProperty("oracle.offices")
public class UsOracleOffice implements Named {
    private String name;
    private Double latitude;
    private Double longitude;
    private String city;

    public UsOracleOffice(@Parameter String name) {
        this.name = name;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    public Location location() {
        return new Location(getLatitude(), getLongitude());
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        if (this.city == null) {
            return StringUtils.capitalize(getName());
        }
        return this.city;
    }
}
