package tech.chhsich.backend.exception;

/**
 * 授权异常类
 * 用于处理权限相关的异常
 *
 * @author chhsich
 * @since 2025-09-19
 */
public class AuthorizationException extends RuntimeException {

    private int code;
    private String message;

    public AuthorizationException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public AuthorizationException(String message) {
        super(message);
        this.code = 403;
        this.message = message;
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
        this.code = 403;
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