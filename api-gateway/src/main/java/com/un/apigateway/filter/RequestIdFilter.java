package com.un.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Проставляет каждому запросу уникальный {@code X-Request-Id}.
 * <p>
 * Логика:
 * <ol>
 *   <li>Если клиент прислал {@code X-Request-Id} и он прошёл валидацию —
 *       используем его.</li>
 *   <li>Иначе генерируем новый UUID.</li>
 *   <li>Кладём значение в request headers (downstream увидит его).</li>
 *   <li>Кладём в response headers (клиент сможет процитировать ID в тикете).</li>
 *   <li>Кладём в MDC для логов на время обработки.</li>
 * </ol>
 *
 * <p>Порядок фильтра — максимально ранний ({@link Ordered#HIGHEST_PRECEDENCE} + 10),
 * чтобы requestId был доступен всем последующим фильтрам, включая
 * rate-limit и auth.
 */
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);

    /**
     * Разрешённые символы во входящем ID — буквы, цифры, дефис.
     * Отсекает пробелы, переводы строк и прочий мусор, который может
     * привести к header injection или поломке логов.
     */
    private static final Pattern ALLOWED = Pattern.compile("^[A-Za-z0-9-]{8,64}$");

    /** Порядок раньше всех бизнес-фильтров, но после системных. */
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = resolve(exchange.getRequest().getHeaders().getFirst(HEADER));

        // 1. Мутируем запрос: downstream должен получить X-Request-Id
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(h -> h.set(HEADER, requestId))
                .build();

        // 2. Кладём в response: клиент увидит его в ответе
        exchange.getResponse().getHeaders().set(HEADER, requestId);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // 3. MDC для логов в рамках обработки запроса
        MDC.put(MDC_KEY, requestId);
        log.debug("Request [{}] {} {}", requestId,
                mutatedRequest.getMethod(), mutatedRequest.getURI().getPath());

        return chain.filter(mutatedExchange)
                .doFinally(signal -> MDC.remove(MDC_KEY));
    }

    private String resolve(String incoming) {
        if (incoming != null && ALLOWED.matcher(incoming).matches()) {
            return incoming;
        }
        if (incoming != null) {
            log.warn("Ignoring malformed X-Request-Id from client: '{}'", incoming);
        }
        return UUID.randomUUID().toString();
    }
}