package tech.chhsich.backend.enums;

/**
 * 订单状态枚举
 *
 * 定义订单的完整生命周期状态，支持从待支付到已完成的完整流转。
 * 基于项目设计文档第5.4节订单信息表(cg_info)的状态字段设计。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
public enum OrderStatus {

    /**
     * 待支付状态
     * 订单已创建，等待用户支付
     */
    PENDING_PAYMENT(0, "待支付"),

    /**
     * 已支付状态
     * 用户已完成支付，等待商家处理
     */
    PAID(1, "已支付"),

    /**
     * 配送中状态
     * 订单正在配送途中
     */
    DELIVERING(2, "配送中"),

    /**
     * 已完成状态
     * 订单已成功完成配送
     */
    COMPLETED(3, "已完成"),

    /**
     * 已取消状态
     * 订单已被用户或系统取消
     */
    CANCELLED(4, "已取消"),

    /**
     * 退款中状态
     * 用户申请退款，正在处理中
     */
    REFUNDING(5, "退款中"),

    /**
     * 已退款状态
     * 退款已完成
     */
    REFUNDED(6, "已退款");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态代码获取对应的订单状态枚举
     *
     * @param code 状态代码
     * @return 对应的订单状态枚举
     * @throws IllegalArgumentException 如果状态代码无效
     */
    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid order status code: " + code);
    }

    /**
     * 检查状态转换是否合法
     *
     * @param currentStatus 当前状态
     * @param newStatus 新状态
     * @return 如果转换合法返回true，否则返回false
     */
    public static boolean isValidTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return true; // 允许相同状态
        }

        switch (currentStatus) {
            case PENDING_PAYMENT:
                // 待支付 -> 已支付、已取消
                return newStatus == PAID || newStatus == CANCELLED;
            case PAID:
                // 已支付 -> 配送中、退款中、已取消
                return newStatus == DELIVERING || newStatus == REFUNDING || newStatus == CANCELLED;
            case DELIVERING:
                // 配送中 -> 已完成
                return newStatus == COMPLETED;
            case COMPLETED:
                // 已完成 -> 退款中
                return newStatus == REFUNDING;
            case REFUNDING:
                // 退款中 -> 已退款
                return newStatus == REFUNDED;
            case CANCELLED:
            case REFUNDED:
                // 已取消和已退款是最终状态，不能转换
                return false;
            default:
                return false;
        }
    }

    /**
     * 获取状态的可转换状态列表
     *
     * @param currentStatus 当前状态
     * @return 可转换的状态数组
     */
    public static OrderStatus[] getAvailableTransitions(OrderStatus currentStatus) {
        switch (currentStatus) {
            case PENDING_PAYMENT:
                return new OrderStatus[]{PAID, CANCELLED};
            case PAID:
                return new OrderStatus[]{DELIVERING, REFUNDING, CANCELLED};
            case DELIVERING:
                return new OrderStatus[]{COMPLETED};
            case COMPLETED:
                return new OrderStatus[]{REFUNDING};
            case REFUNDING:
                return new OrderStatus[]{REFUNDED};
            case CANCELLED:
            case REFUNDED:
            default:
                return new OrderStatus[0];
        }
    }

    /**
     * 检查状态是否为最终状态
     *
     * @param status 要检查的状态
     * @return 如果是最终状态返回true，否则返回false
     */
    public static boolean isFinalStatus(OrderStatus status) {
        return status == CANCELLED || status == REFUNDED;
    }

    /**
     * 检查状态是否允许取消
     *
     * @param status 要检查的状态
     * @return 如果允许取消返回true，否则返回false
     */
    public static boolean isCancellable(OrderStatus status) {
        return status == PENDING_PAYMENT || status == PAID;
    }

    /**
     * 检查状态是否允许退款
     *
     * @param status 要检查的状态
     * @return 如果允许退款返回true，否则返回false
     */
    public static boolean isRefundable(OrderStatus status) {
        return status == PAID || status == DELIVERING || status == COMPLETED;
    }

    @Override
    public String toString() {
        return this.description + "(" + this.code + ")";
    }
}