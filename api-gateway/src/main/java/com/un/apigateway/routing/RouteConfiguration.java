package com.un.apigateway.routing;

import com.un.apigateway.config.GatewayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import reactor.core.publisher.Flux;

/**
 * Регистрирует маршруты из {@link RouteProperties} в Spring Cloud Gateway.
 * <p>
 * Используется {@link RouteDefinitionLocator}, а не {@code RouteLocator},
 * чтобы:
 * <ul>
 *   <li>маршруты были видны в {@code /actuator/gateway/routes};</li>
 *   <li>их можно было комбинировать с другими локаторами
 *       (yaml-маршруты, discovery).</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties({RouteProperties.class, GatewayProperties.class})
public class RouteConfiguration {

    @Bean
    public RouteDefinitionLocator configuredRoutes(RouteProperties routeProperties,
                                                   GatewayProperties gatewayProperties) {
        // Материализуем заранее, чтобы ошибки конфигурации ломали старт,
        // а не всплывали на первом запросе.
        List<RouteDefinition> definitions = routeProperties.getRoutes().entrySet().stream()
                .map(entry -> RouteDefinitionFactory.create(
                        entry.getKey(), entry.getValue(), gatewayProperties))
                .toList();

        return new RouteDefinitionLocator() {
            @Override
            public Flux<RouteDefinition> getRouteDefinitions() {
                return Flux.fromIterable(definitions);
            }
        };
    }
}