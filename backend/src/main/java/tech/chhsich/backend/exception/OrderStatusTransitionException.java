package tech.chhsich.backend.exception;

/**
 * 订单状态转换异常
 *
 * 当订单状态转换不合法时抛出此异常。
 * 基于订单状态机的业务规则进行异常处理。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
public class OrderStatusTransitionException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public OrderStatusTransitionException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause 原始异常
     */
    public OrderStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param cause 原始异常
     */
    public OrderStatusTransitionException(Throwable cause) {
        super(cause);
    }
}