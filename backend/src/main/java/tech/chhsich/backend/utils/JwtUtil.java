package tech.chhsich.backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final String KEY = "qxzm";

	//接收业务数据,生成token并返回
    public static String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)//附加载荷  一般就是当前登录用户信息
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))//设置有效时间
                .sign(Algorithm.HMAC256(KEY));//设置算法 秘钥
    }

	//接收token,验证token,并返回业务数据
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(KEY))//将token字符串反向解析维Map
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }


}
