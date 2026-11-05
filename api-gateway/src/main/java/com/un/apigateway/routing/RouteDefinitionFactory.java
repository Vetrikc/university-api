package com.un.apigateway.routing;


import com.un.apigateway.config.GatewayProperties;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Преобразует конфигурационный маршрут ({@link RouteProperties.Route})
 * в декларативную модель Spring Cloud Gateway ({@link RouteDefinition}).
 * <p>
 * Вся валидация выполняется здесь — на старте приложения, а не на первом
 * запросе. Некорректный маршрут ломает старт с понятным сообщением.
 */
public final class RouteDefinitionFactory {

    private RouteDefinitionFactory() {
    }

    public static RouteDefinition create(String id,
                                         RouteProperties.Route route,
                                         GatewayProperties globals) {
        validate(id, route);

        RouteDefinition definition = new RouteDefinition();
        definition.setId(id);
        definition.setUri(route.getUri());
        definition.setPredicates(List.of(pathPredicate(route.getPath())));
        definition.setMetadata(buildMetadata(route, globals));
        return definition;
    }

    private static PredicateDefinition pathPredicate(String path) {
        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");
        predicate.addArg("pattern", path);
        return predicate;
    }

    private static Map<String, Object> buildMetadata(RouteProperties.Route route,
                                                     GatewayProperties globals) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        Duration timeout = route.getTimeout() != null
                ? route.getTimeout()
                : globals.getDefaultResponseTimeout();
        if (timeout != null && !timeout.isZero() && !timeout.isNegative()) {
            // NettyRoutingFilter читает response-timeout из metadata (мс).
            metadata.put("response-timeout", timeout.toMillis());
        }

        if (route.getMaxRequestSize() != null) {
            metadata.put("max-request-size", route.getMaxRequestSize().toBytes());
        } else if (globals.getMaxRequestSize() != null) {
            metadata.put("max-request-size", globals.getMaxRequestSize().toBytes());
        }

        if (route.getAuthRequired() != null) {
            metadata.put("auth-required", route.getAuthRequired());
        }

        return metadata;
    }

    private static void validate(String id, RouteProperties.Route route) {
        if (!StringUtils.hasText(route.getPath())) {
            throw new IllegalStateException(
                    "Route '" + id + "': path must not be blank");
        }
        if (!route.getPath().startsWith("/")) {
            throw new IllegalStateException(
                    "Route '" + id + "': path must start with '/', got: " + route.getPath());
        }

        URI uri = route.getUri();
        if (uri == null) {
            throw new IllegalStateException("Route '" + id + "': uri must not be null");
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalStateException(
                    "Route '" + id + "': uri scheme must be http or https, got: " + scheme);
        }
        if (uri.getHost() == null) {
            throw new IllegalStateException(
                    "Route '" + id + "': uri must contain a host: " + uri);
        }
    }
}