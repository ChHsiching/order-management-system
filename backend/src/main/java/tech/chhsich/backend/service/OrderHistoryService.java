package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.OrderHistory;
import tech.chhsich.backend.entity.OrderInfo;
import tech.chhsich.backend.enums.OrderStatus;
import tech.chhsich.backend.mapper.OrderHistoryMapper;
import tech.chhsich.backend.mapper.OrderInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单历史记录服务
 *
 * 管理订单状态变更的历史记录，确保所有状态转换都有完整的日志记录。
 * 提供历史记录查询、统计分析等功能。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@Service
public class OrderHistoryService {

    private final OrderHistoryMapper orderHistoryMapper;
    private final OrderInfoMapper orderInfoMapper;

    /**
     * 构造函数
     *
     * @param orderHistoryMapper 订单历史记录数据访问层
     * @param orderInfoMapper 订单信息数据访问层
     */
    public OrderHistoryService(OrderHistoryMapper orderHistoryMapper, OrderInfoMapper orderInfoMapper) {
        this.orderHistoryMapper = orderHistoryMapper;
        this.orderInfoMapper = orderInfoMapper;
    }

    /**
     * 记录订单状态变更
     *
     * @param order 订单对象
     * @param newStatus 新状态
     * @param reason 变更原因
     * @param operator 操作人员
     * @param remarks 备注
     */
    @Transactional
    public void recordStatusChange(OrderInfo order, OrderStatus newStatus, String reason, String operator, String remarks) {
        OrderHistory history = new OrderHistory();
        history.setOrderId(order.getOrderId());

        // 获取原状态信息
        OrderStatus fromStatus = OrderStatus.fromCode(order.getStatus());
        history.setFromStatus(fromStatus.getCode());
        history.setFromStatusDesc(fromStatus.getDescription());

        // 设置新状态信息
        history.setToStatus(newStatus.getCode());
        history.setToStatusDesc(newStatus.getDescription());

        // 设置其他信息
        history.setChangeReason(reason);
        history.setOperator(operator);
        history.setOperationTime(LocalDateTime.now());
        history.setRemarks(remarks);

        // 保存历史记录
        orderHistoryMapper.insert(history);
    }

    /**
     * 获取订单的完整历史记录
     *
     * @param orderId 订单ID
     * @return 该订单的所有历史记录
     */
    public List<OrderHistory> getOrderHistory(String orderId) {
        return orderHistoryMapper.findByOrderId(orderId);
    }

    /**
     * 获取订单历史记录并包含状态描述
     *
     * @param orderId 订单ID
     * @return 格式化的历史记录列表
     */
    public List<OrderHistory> getFormattedOrderHistory(String orderId) {
        List<OrderHistory> history = orderHistoryMapper.findByOrderId(orderId);

        // 确保所有历史记录都有完整的状态描述
        for (OrderHistory record : history) {
            if (record.getFromStatusDesc() == null) {
                try {
                    OrderStatus status = OrderStatus.fromCode(record.getFromStatus());
                    record.setFromStatusDesc(status.getDescription());
                } catch (IllegalArgumentException e) {
                    record.setFromStatusDesc("未知状态");
                }
            }

            if (record.getToStatusDesc() == null) {
                try {
                    OrderStatus status = OrderStatus.fromCode(record.getToStatus());
                    record.setToStatusDesc(status.getDescription());
                } catch (IllegalArgumentException e) {
                    record.setToStatusDesc("未知状态");
                }
            }
        }

        return history;
    }

    /**
     * 获取指定时间范围内的历史记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间范围内的历史记录
     */
    public List<OrderHistory> getHistoryByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return orderHistoryMapper.findByTimeRange(startTime, endTime);
    }

    /**
     * 获取指定操作人员的历史记录
     *
     * @param operator 操作人员
     * @return 该操作人员的历史记录
     */
    public List<OrderHistory> getHistoryByOperator(String operator) {
        return orderHistoryMapper.findByOperator(operator);
    }

    /**
     * 获取订单的最新状态变更记录
     *
     * @param orderId 订单ID
     * @return 最新的历史记录，如果没有则返回null
     */
    public OrderHistory getLatestHistory(String orderId) {
        List<OrderHistory> history = orderHistoryMapper.findByOrderId(orderId);
        return history.isEmpty() ? null : history.get(0);
    }

    /**
     * 检查订单是否存在指定状态的历史记录
     *
     * @param orderId 订单ID
     * @param status 要检查的状态
     * @return 如果存在该状态的历史记录返回true，否则返回false
     */
    public boolean hasStatusHistory(String orderId, OrderStatus status) {
        List<OrderHistory> history = orderHistoryMapper.findByOrderId(orderId);
        for (OrderHistory record : history) {
            if (record.getToStatus().equals(status.getCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取订单状态变更次数统计
     *
     * @param orderId 订单ID
     * @return 状态变更次数
     */
    public int getStatusChangeCount(String orderId) {
        return orderHistoryMapper.findByOrderId(orderId).size();
    }

    /**
     * 清理过期的历史记录（可选的管理功能）
     *
     * @param daysToKeep 保留天数
     * @return 清理的记录数
     */
    @Transactional
    public int cleanExpiredHistory(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);

        // 使用MyBatis-Plus的删除方法
        return orderHistoryMapper.deleteByTimeRange(cutoffTime, LocalDateTime.now());
    }
}