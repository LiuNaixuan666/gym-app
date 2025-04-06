package com.liu.gymmanagement.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {


        @Value("${jwt.secret}")
        private String secret;

        @Value("${jwt.expiration}")
        private long expiration;

        // 生成Token
        public String generateToken(String userId, int roleId) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("roleId", roleId);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact();
        }

        // 从Token中获取用户ID
        public String getUserIdFromToken(String token) {
            return getClaimsFromToken(token).getSubject();
        }

        // 从Token中获取角色ID
        public int getRoleIdFromToken(String token) {
            return (int) getClaimsFromToken(token).get("roleId");
        }

        // 从请求中获取Token
        public String getTokenFromRequest(HttpServletRequest request) {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            return null;
        }

        // 从请求中获取用户ID
        public String getUserIdFromRequest(HttpServletRequest request) {
            String token = getTokenFromRequest(request);
            if (token != null) {
                return getUserIdFromToken(token);
            }
            return null;
        }

        // 验证Token
        public boolean validateToken(String token) {
            try {
                getClaimsFromToken(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private Claims getClaimsFromToken(String token) {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

}
//    private static String secretString;  // 移除 static，通过 setter 注入
//    private static SecretKey SECRET_KEY; // 密钥对象
//
//    private static final String TOKEN_PREFIX = "Bearer ";
//    private static final String HEADER_STRING = "Authorization";
//
//    // 通过 setter 注入配置值
//    @Value("${jwt.secret}")
//    public void setSecretString(String secret) {
//        JwtUtil.secretString = secret;
//    }
//
//    // 初始化密钥
//    @PostConstruct
//    public void init() {
//        SECRET_KEY = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
//    }
//
//    // 解析 JWT
//    public Claims parseJwt(String token) {
//        return Jwts.parser()
//                .verifyWith(SECRET_KEY)
//                .build()
//                .parseSignedClaims(token.replace(TOKEN_PREFIX, ""))
//                .getPayload();
//    }
//
//    // 从请求头获取 studentId
//    public String getStudentIdFromRequest(HttpServletRequest request) {
//        String token = request.getHeader(HEADER_STRING);
//        if (token != null && token.startsWith(TOKEN_PREFIX)) {
//            Claims claims = parseJwt(token);
//            return String.valueOf(claims.get("studentId"));
//        }
//        throw new RuntimeException("无效的 JWT Token");
//    }
//
//    // 生成 Token（适配 JJWT 0.12.x）
//    public String generateToken(String studentId) {
//        return TOKEN_PREFIX + Jwts.builder()
//                .subject("GymReservation")           // 设置主题
//                .claim("studentId", studentId)       // 自定义声明
//                .issuedAt(new Date())                // 颁发时间
//                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24小时过期
//                .signWith(SECRET_KEY)                // 直接使用 Key 签名
//                .compact();
//    }
//
//    // 返回密钥字符串（可选）
//    public String getSecretKeyString() {
//        return secretString;
//    }
//
//    // 返回密钥对象（按需使用）
//    public SecretKey getSecretKey() {
//        return SECRET_KEY;
//    }
//}