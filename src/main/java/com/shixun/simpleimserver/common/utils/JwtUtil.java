package com.shixun.simpleimserver.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 1. 密钥 (在实际生产中，这个应该写在 application.yml 里，且要非常复杂)
    private final String SECRET_KEY = "SimpleImSecretKeyForNoviceDevelopersKeepItSafe";

    // 2. 过期时间 (这里设为 24 小时: 1000毫秒 * 60秒 * 60分 * 24小时)
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    // ================== 生成 Token ==================

    /**
     * 根据用户信息生成 Token
     * @param userDetails Spring Security 的用户对象
     * @return Token 字符串
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // TODO 你可以在 claims 里放入自定义信息，比如 userId, role 等
        // claims.put("userId", 123);
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // 自定义载荷
                .setSubject(subject) // 主题 (通常是用户名)
                .setIssuedAt(new Date(System.currentTimeMillis())) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 签名算法和密钥
                .compact();
    }

    // ================== 解析/验证 Token ==================

    /**
     * 验证 Token 是否有效
     * 1. 签名正确
     * 2. 用户名匹配
     * 3. 未过期
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 从 Token 中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从 Token 中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 泛型方法：提取 Token 中的某一项信息
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析 Token 内部的所有 Claims (需要密钥)
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查 Token 是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}