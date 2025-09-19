package tech.chhsich.backend.exception;

/**
 * 认证异常类
 * 用于处理认证相关的异常
 *
 * @author chhsich
 * @since 2025-09-19
 */
public class AuthenticationException extends RuntimeException {

    private int code;
    private String message;

    public AuthenticationException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public AuthenticationException(String message) {
        super(message);
        this.code = 401;
        this.message = message;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.code = 401;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}