package com.study.FlowTrack.config.security.jwt;

import com.study.FlowTrack.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, AuthService authService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {

                // 1. Идентификация: Извлечь логин из токена
                String username = jwtTokenProvider.getUsernameFromJWT(jwt);

                // 2. Загрузка данных: Получить данные пользователя (права, роли) из БД
                UserDetails userDetails = authService.loadUserByUsername(username);

                // 3. Создание "Отметки": Создать токен аутентификации Spring Security
                // ПАРОЛЬ НЕ НУЖЕН (null), т.к. токен уже проверен
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Добавить детали запроса для лучшей трассировки
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. Установка Контекста: Сохранить "Отметку" на общую доску
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ex) {
            logger.error("Не удалось установить аутентификацию пользователя в контексте безопасности: {}", ex.getMessage());
        }

        // В. Передать запрос следующему фильтру в цепочке
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // Мы ищем заголовок "Authorization: Bearer <token>"
        String bearerToken = request.getHeader("Authorization");

        // Проверяем, что заголовок существует и начинается с "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Возвращаем токен без префикса "Bearer " (7 символов)
            return bearerToken.substring(7);
        }
        return null;
    }
}


