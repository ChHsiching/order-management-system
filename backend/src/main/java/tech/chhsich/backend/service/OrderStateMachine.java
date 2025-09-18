package tech.chhsich.backend.service;

import tech.chhsich.backend.enums.OrderStatus;
import tech.chhsich.backend.entity.OrderInfo;
import tech.chhsich.backend.exception.OrderStatusTransitionException;
import tech.chhsich.backend.service.OrderHistoryService;
import org.springframework.stereotype.Service;

/**
 * 订单状态机
 *
 * 管理订单状态流转的业务逻辑，确保状态转换的合法性和一致性。
 * 基于项目设计文档第5.4节订单信息表(cg_info)的状态管理要求。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@Service
public class OrderStateMachine {

    private final OrderHistoryService orderHistoryService;

    /**
     * 构造函数
     *
     * @param orderHistoryService 订单历史记录服务
     */
    public OrderStateMachine(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    /**
     * 执行订单状态转换
     *
     * @param order 订单对象
     * @param newStatus 新状态
     * @param reason 状态转换原因
     * @return 转换后的订单对象
     * @throws OrderStatusTransitionException 如果状态转换不合法
     */
    public OrderInfo transitionOrderStatus(OrderInfo order, OrderStatus newStatus, String reason)
            throws OrderStatusTransitionException {
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        if (!OrderStatus.isValidTransition(currentStatus, newStatus)) {
            throw new OrderStatusTransitionException(
                String.format("无法从 %s 转换到 %s", currentStatus.getDescription(), newStatus.getDescription())
            );
        }

        // 记录状态变更历史
        orderHistoryService.recordStatusChange(order, newStatus, reason, "系统", reason);

        // 更新订单状态
        order.setStatus(newStatus.getCode());

        // 这里可以添加状态转换的日志记录
        logStatusTransition(order, currentStatus, newStatus, reason);

        return order;
    }

    /**
     * 检查状态转换是否合法
     *
     * @param order 订单对象
     * @param newStatus 新状态
     * @return 如果转换合法返回true，否则返回false
     */
    public boolean isValidTransition(OrderInfo order, OrderStatus newStatus) {
        try {
            OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());
            return OrderStatus.isValidTransition(currentStatus, newStatus);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取订单的可用状态转换列表
     *
     * @param order 订单对象
     * @return 可转换的状态数组
     */
    public OrderStatus[] getAvailableTransitions(OrderInfo order) {
        try {
            OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());
            return OrderStatus.getAvailableTransitions(currentStatus);
        } catch (IllegalArgumentException e) {
            return new OrderStatus[0];
        }
    }

    /**
     * 取消订单
     *
     * @param order 订单对象
     * @param reason 取消原因
     * @return 转换后的订单对象
     * @throws OrderStatusTransitionException 如果订单无法取消
     */
    public OrderInfo cancelOrder(OrderInfo order, String reason) throws OrderStatusTransitionException {
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        if (!OrderStatus.isCancellable(currentStatus)) {
            throw new OrderStatusTransitionException(
                String.format("当前状态 %s 不允许取消", currentStatus.getDescription())
            );
        }

        return transitionOrderStatus(order, OrderStatus.CANCELLED, reason);
    }

    /**
     * 申请退款
     *
     * @param order 订单对象
     * @param reason 退款原因
     * @return 转换后的订单对象
     * @throws OrderStatusTransitionException 如果订单无法申请退款
     */
    public OrderInfo requestRefund(OrderInfo order, String reason) throws OrderStatusTransitionException {
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        if (!OrderStatus.isRefundable(currentStatus)) {
            throw new OrderStatusTransitionException(
                String.format("当前状态 %s 不允许申请退款", currentStatus.getDescription())
            );
        }

        return transitionOrderStatus(order, OrderStatus.REFUNDING, reason);
    }

    /**
     * 确认收货（完成订单）
     *
     * @param order 订单对象
     * @return 转换后的订单对象
     * @throws OrderStatusTransitionException 如果订单无法确认收货
     */
    public OrderInfo confirmDelivery(OrderInfo order) throws OrderStatusTransitionException {
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        if (currentStatus != OrderStatus.DELIVERING) {
            throw new OrderStatusTransitionException(
                String.format("当前状态 %s 不允许确认收货", currentStatus.getDescription())
            );
        }

        return transitionOrderStatus(order, OrderStatus.COMPLETED, "用户确认收货");
    }

    /**
     * 开始配送
     *
     * @param order 订单对象
     * @return 转换后的订单对象
     * @throws OrderStatusTransitionException 如果订单无法开始配送
     */
    public OrderInfo startDelivery(OrderInfo order) throws OrderStatusTransitionException {
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        if (currentStatus != OrderStatus.PAID) {
            throw new OrderStatusTransitionException(
                String.format("当前状态 %s 不允许开始配送", currentStatus.getDescription())
            );
        }

        return transitionOrderStatus(order, OrderStatus.DELIVERING, "开始配送");
    }

    /**
     * 支付订单
     *
     * @param order 订单对象
     * @return 转换后的订单对象
     * @throws OrderStatusTransitionException 如果订单无法支付
     */
    public OrderInfo payOrder(OrderInfo order) throws OrderStatusTransitionException {
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        if (currentStatus != OrderStatus.PENDING_PAYMENT) {
            throw new OrderStatusTransitionException(
                String.format("当前状态 %s 不允许支付", currentStatus.getDescription())
            );
        }

        return transitionOrderStatus(order, OrderStatus.PAID, "用户支付完成");
    }

    /**
     * 完成退款
     *
     * @param order 订单对象
     * @param reason 退款完成原因
     * @return 转换后的订单对象
     * @throws OrderStatusTransitionException 如果订单无法完成退款
     */
    public OrderInfo completeRefund(OrderInfo order, String reason) throws OrderStatusTransitionException {
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        if (currentStatus != OrderStatus.REFUNDING) {
            throw new OrderStatusTransitionException(
                String.format("当前状态 %s 不允许完成退款", currentStatus.getDescription())
            );
        }

        return transitionOrderStatus(order, OrderStatus.REFUNDED, reason);
    }

    /**
     * 记录状态转换日志
     *
     * @param order 订单对象
     * @param fromStatus 原状态
     * @param toStatus 新状态
     * @param reason 转换原因
     */
    private void logStatusTransition(OrderInfo order, OrderStatus fromStatus, OrderStatus toStatus, String reason) {
        // TODO: 实现状态转换日志记录
        // 这里可以将状态转换记录到数据库或日志文件中
        System.out.println(String.format(
            "订单 %s 状态变更: %s -> %s, 原因: %s",
            order.getOrderId(),
            fromStatus.getDescription(),
            toStatus.getDescription(),
            reason
        ));
    }

    /**
     * 获取订单状态描述
     *
     * @param statusCode 状态代码
     * @return 状态描述
     */
    public static String getStatusDescription(int statusCode) {
        try {
            return OrderStatus.fromCode(statusCode).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }

    /**
     * 检查订单是否为最终状态
     *
     * @param order 订单对象
     * @return 如果是最终状态返回true，否则返回false
     */
    public boolean isFinalStatus(OrderInfo order) {
        try {
            OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());
            return OrderStatus.isFinalStatus(currentStatus);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}