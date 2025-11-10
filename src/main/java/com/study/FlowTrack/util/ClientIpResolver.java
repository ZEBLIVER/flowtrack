package com.study.FlowTrack.util;

import jakarta.servlet.http.HttpServletRequest;

public class ClientIpResolver {
    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
    };

    // Извлекает реальный IP-адрес клиента из HTTP-запроса, учитывая прокси и балансировщики.
    public static String getClientIp(HttpServletRequest request) {
        // 1. Проверяем заголовки, используемые прокси и балансировщиками
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For может содержать список IP, берем первый (реальный клиент)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // 2. Если заголовки не помогли (или прямое соединение), берем базовый адрес
        return request.getRemoteAddr();
    }
}
