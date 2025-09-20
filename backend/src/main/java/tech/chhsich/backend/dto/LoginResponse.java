package tech.chhsich.backend.dto;

/**
 * 用户登录响应DTO
 * 包含JWT token和用户信息
 *
 * @author chhsich
 * @since 2025-09-20
 */
public class LoginResponse {

    private String token;
    private Object userInfo;
    private String message;
    private int code;

    public LoginResponse() {}

    public LoginResponse(int code, String message, String token, Object userInfo) {
        this.code = code;
        this.message = message;
        this.token = token;
        this.userInfo = userInfo;
    }

    public static LoginResponse success(String token, Object userInfo) {
        return new LoginResponse(200, "登录成功", token, userInfo);
    }

    public static LoginResponse error(String message) {
        return new LoginResponse(400, message, null, null);
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Object getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Object userInfo) {
        this.userInfo = userInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}