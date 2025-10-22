package com.study.FlowTrack.config.security.jwt;

import com.study.FlowTrack.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;


    public String generateToken(UserDetails userDetails) {
        User user = (User) userDetails;

        String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Date now = new Date();
        Date expiryDate =  new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("id", user.getId())
                .claim("roles", authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String authToken) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken);
            return true; // Если парсинг прошел успешно, токен валиден

        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature"); // Подпись токена недействительна (подделан?)
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token"); // Токен имеет неправильный формат
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token"); // Срок действия токена истек
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token"); // Неподдерживаемый токен
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty."); // Токен пустой
        }
        return false;
    }
}
