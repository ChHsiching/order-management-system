package tech.chhsich.backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * JWT工具类
 * 提供JWT token的生成、验证和解析功能
 *
 * @author chhsich
 * @since 2025-09-19
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:qwqw}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private Long expiration;

    /**
     * 生成JWT token
     *
     * @param username 用户名
     * @return JWT token
     */
    public String generateToken(String username) {
        return generateToken(username, null);
    }

    /**
     * 生成带用户信息的JWT token
     *
     * @param username 用户名
     * @param claims 用户信息
     * @return JWT token
     */
    public String generateToken(String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration * 1000);

        Algorithm algorithm = Algorithm.HMAC256(secret);

        com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(expirationDate);

        if (claims != null && !claims.isEmpty()) {
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    builder.withClaim(entry.getKey(), (String) value);
                } else if (value instanceof Integer) {
                    builder.withClaim(entry.getKey(), (Integer) value);
                } else if (value instanceof Long) {
                    builder.withClaim(entry.getKey(), (Long) value);
                } else if (value instanceof Boolean) {
                    builder.withClaim(entry.getKey(), (Boolean) value);
                } else if (value instanceof Double) {
                    builder.withClaim(entry.getKey(), (Double) value);
                }
            }
        }

        return builder.sign(algorithm);
    }

    /**
     * 生成刷新token
     *
     * @param username 用户名
     * @return 刷新token
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration * 1000 * 7); // 7天

        return JWT.create()
                .withSubject(username)
                .withClaim("type", "refresh")
                .withIssuedAt(now)
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 验证JWT token
     *
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 解析JWT token
     *
     * @param token JWT token
     * @return 解析后的JWT对象
     * @throws JWTVerificationException token无效时抛出异常
     */
    public DecodedJWT parseToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm).build().verify(token);
    }

    /**
     * 从token中获取用户名
     *
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = parseToken(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 从token中获取用户信息
     *
     * @param token JWT token
     * @return 用户信息
     */
    public Map<String, Object> getClaimsFromToken(String token) {
        try {
            DecodedJWT jwt = parseToken(token);
            Map<String, Object> claims = new HashMap<>();
            jwt.getClaims().forEach((key, claim) -> {
                if (claim.asString() != null) {
                    claims.put(key, claim.asString());
                } else if (claim.asInt() != null) {
                    claims.put(key, claim.asInt());
                } else if (claim.asLong() != null) {
                    claims.put(key, claim.asLong());
                } else if (claim.asBoolean() != null) {
                    claims.put(key, claim.asBoolean());
                } else if (claim.asDouble() != null) {
                    claims.put(key, claim.asDouble());
                }
            });
            return claims;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 检查token是否过期
     *
     * @param token JWT token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = parseToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }

    /**
     * 获取token过期时间
     *
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            DecodedJWT jwt = parseToken(token);
            return jwt.getExpiresAt();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 从Authorization Header中提取token
     *
     * @param authHeader Authorization Header
     * @return JWT token
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
