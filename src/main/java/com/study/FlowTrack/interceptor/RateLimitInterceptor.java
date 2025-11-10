package com.study.FlowTrack.interceptor;

import com.study.FlowTrack.service.redis.RateLimiterService;
import com.study.FlowTrack.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    private static final int MAX_REQUESTS = 100; // 100 запросов
    private static final long TIME_WINDOW_SECONDS = 60; // за 60 секунд
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:ip:";

    public RateLimitInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. Получаем IP-адрес клиента
        String clientIp = ClientIpResolver.getClientIp(request);

        // 2. Формируем уникальный ключ для Redis
        String key = RATE_LIMIT_KEY_PREFIX + clientIp;

        // 3. Проверяем и увеличиваем счетчик
        // Если лимит превышен, RateLimiterService бросит RateLimitExceededException
        rateLimiterService.checkAndIncrement(key, MAX_REQUESTS, TIME_WINDOW_SECONDS);

        // Если исключение не брошено, запрос разрешен
        return true;
    }

}