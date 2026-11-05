package com.un.apigateway.routing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Маршруты gateway, загружаемые из конфигурации.
 * <p>
 * Каждый маршрут идентифицируется ключом в `app.gateway.routes`.
 * Добавление нового upstream'а не требует изменений в Java-коде —
 * достаточно дописать блок в `application.yml`.
 * <p>
 * Пример:
 * <pre>
 * app:
 *   gateway:
 *     routes:
 *       users:
 *         path: /api/users/**
 *         uri: http://localhost:8081
 *       orders:
 *         path: /api/orders/**
 *         uri: http://localhost:8082
 * </pre>
 */
@ConfigurationProperties(prefix = "app.gateway")
@Validated
public class RouteProperties {

    /**
     * Список маршрутов. Ключ карты — идентификатор маршрута (используется
     * в метриках, логах и в `/actuator/gateway/routes`).
     * <p>
     * По умолчанию: пустая карта (маршруты не сконфигурированы).
     */
    @NotNull
    @Valid
    private Map<String, Route> routes = new LinkedHashMap<>();

    public Map<String, Route> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, Route> routes) {
        this.routes = routes;
    }

    /**
     * Описание одного upstream'а.
     */
    public static class Route {

        /**
         * Path-шаблон (Spring PathPattern), по которому запрос попадает
         * в этот маршрут. Должен начинаться с `/`.
         * Пример: `/api/users/**`.
         * <p>
         * Обязательное поле.
         */
        @NotBlank
        private String path;

        /**
         * Адрес upstream-сервиса. Схема должна быть `http` или `https`.
         * Пример: `http://localhost:8081`, `https://billing.internal:8443`.
         * <p>
         * Обязательное поле.
         */
        @NotNull
        private URI uri;

        /**
         * Таймаут ответа для этого маршрута. Переопределяет
         * `app.gateway.default-response-timeout`.
         * Формат ISO-8601: `30s`, `500ms`, `2m`.
         * <p>
         * Если не задан — применяется глобальный дефолт.
         */
        private Duration timeout;

        /**
         * Требуется ли аутентификация для этого маршрута.
         * <p>
         * Если не задан — применяется значение по умолчанию
         * из блока security (пока — `false`).
         */
        private Boolean authRequired;

        /**
         * Максимальный размер тела запроса для этого маршрута.
         * Переопределяет `app.gateway.max-request-size`.
         * Формат: `2MB`, `512KB`.
         * <p>
         * Если не задан — применяется глобальный дефолт.
         */
        private DataSize maxRequestSize;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public URI getUri() {
            return uri;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public Boolean getAuthRequired() {
            return authRequired;
        }

        public void setAuthRequired(Boolean authRequired) {
            this.authRequired = authRequired;
        }

        public DataSize getMaxRequestSize() {
            return maxRequestSize;
        }

        public void setMaxRequestSize(DataSize maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }
    }
}