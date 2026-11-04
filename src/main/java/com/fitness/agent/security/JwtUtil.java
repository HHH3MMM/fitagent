package com.fitness.agent.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：只负责"造令牌"和"验令牌"，不管 HTTP 请求。
 * 流程：登录成功 -> generateToken -> 前端每次请求带上 ->
 * JwtAuthenticationFilter 调 parse 验签，验过就放行。
 * 令牌里只放 uid + username 两个必要声明，不放敏感信息（JWT 只是签名防篡改，内容可被解码）。
 */
@Component
public class JwtUtil {

    private final String secret;
    private final long expireMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-hours}") long expireHours) {
        this.secret = secret;
        this.expireMillis = expireHours * 3600_000L;
    }

    private SecretKey key() {
        // HS256 要求密钥至少 32 字节，配置里的 secret 已满足
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key())
                .compact();
    }

    /**
     * 验签并解析。签名不对/过期会抛异常，由过滤器捕获后按"未登录"处理。
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
