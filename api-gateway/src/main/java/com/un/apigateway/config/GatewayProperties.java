package com.un.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.gateway")
@Component
@Getter
@Setter
public class GatewayProperties {

    @NotNull
    @Positive
    private Duration defaultResponseTimeout = Duration.ofSeconds(30);

    @NotNull
    @DataSizeUnit(DataUnit.MEGABYTES)
    private DataSize maxRequestSize = DataSize.ofMegabytes(2);
}