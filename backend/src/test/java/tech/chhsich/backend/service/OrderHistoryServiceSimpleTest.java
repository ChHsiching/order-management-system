package tech.chhsich.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tech.chhsich.backend.entity.OrderHistory;
import tech.chhsich.backend.entity.OrderInfo;
import tech.chhsich.backend.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单历史记录服务简单测试类
 *
 * 测试订单历史记录的基本功能，避免复杂的事务问题。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@SpringBootTest
@Transactional
public class OrderHistoryServiceSimpleTest {

    @Autowired
    private OrderHistoryService orderHistoryService;

    @Autowired
    private OrderService orderService;

    /**
     * 测试记录订单状态变更
     */
    @Test
    public void testRecordStatusChange() {
        // 首先创建一个订单
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 2)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");
        assertNotNull(order);

        // 记录状态变更
        orderHistoryService.recordStatusChange(
            order,
            OrderStatus.PAID,
            "用户支付完成",
            "测试用户",
            "支付完成测试"
        );

        // 验证历史记录
        List<OrderHistory> history = orderHistoryService.getOrderHistory(order.getOrderId());
        assertNotNull(history);
        assertEquals(1, history.size());

        OrderHistory record = history.get(0);
        assertEquals(order.getOrderId(), record.getOrderId());
        assertEquals(OrderStatus.PENDING_PAYMENT.getCode(), record.getFromStatus());
        assertEquals(OrderStatus.PAID.getCode(), record.getToStatus());
        assertEquals("用户支付完成", record.getChangeReason());
        assertEquals("测试用户", record.getOperator());
    }

    /**
     * 测试获取订单历史记录数量
     */
    @Test
    public void testGetStatusChangeCount() {
        // 创建订单
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");

        // 初始状态应该为0条历史记录（因为还没有状态变更）
        int initialCount = orderHistoryService.getStatusChangeCount(order.getOrderId());
        assertEquals(0, initialCount);

        // 记录状态变更
        orderHistoryService.recordStatusChange(order, OrderStatus.PAID, "支付完成", "用户", "支付测试");

        // 现在应该有1条记录
        int afterCount = orderHistoryService.getStatusChangeCount(order.getOrderId());
        assertEquals(1, afterCount);
    }

    /**
     * 测试检查状态历史
     */
    @Test
    public void testHasStatusHistory() {
        // 创建订单
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");

        // 记录状态变更
        orderHistoryService.recordStatusChange(order, OrderStatus.PAID, "支付完成", "用户", "支付测试");

        // 检查状态历史
        assertTrue(orderHistoryService.hasStatusHistory(order.getOrderId(), OrderStatus.PAID));
        assertFalse(orderHistoryService.hasStatusHistory(order.getOrderId(), OrderStatus.DELIVERING));
    }

    /**
     * 测试按时间范围查询历史记录
     */
    @Test
    public void testGetHistoryByTimeRange() {
        // 创建订单
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");

        // 记录状态变更
        orderHistoryService.recordStatusChange(order, OrderStatus.PAID, "支付完成", "用户", "支付测试");

        // 查询时间范围内的历史记录
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(5);
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(5);

        List<OrderHistory> history = orderHistoryService.getHistoryByTimeRange(startTime, endTime);
        assertNotNull(history);
        // 应该至少有一条记录（可能包含其他测试的记录）
        assertTrue(history.size() >= 1);
    }

    /**
     * 测试按操作人员查询历史记录
     */
    @Test
    public void testGetHistoryByOperator() {
        // 创建订单
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");

        // 记录状态变更
        orderHistoryService.recordStatusChange(order, OrderStatus.PAID, "支付完成", "测试操作员", "支付测试");

        // 查询操作人员的历史记录
        List<OrderHistory> history = orderHistoryService.getHistoryByOperator("测试操作员");
        assertNotNull(history);
        assertFalse(history.isEmpty());

        // 验证所有记录都是该操作员的
        for (OrderHistory record : history) {
            assertEquals("测试操作员", record.getOperator());
        }
    }

    /**
     * 测试获取最新历史记录
     */
    @Test
    public void testGetLatestHistory() {
        // 创建订单
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");

        // 记录状态变更
        orderHistoryService.recordStatusChange(order, OrderStatus.PAID, "支付完成", "用户", "支付测试");

        // 获取最新记录
        OrderHistory latest = orderHistoryService.getLatestHistory(order.getOrderId());
        assertNotNull(latest);
        assertEquals(OrderStatus.PAID.getCode(), latest.getToStatus());
        assertEquals("支付完成", latest.getChangeReason());
    }

    /**
     * 创建订单项请求的辅助方法
     */
    private OrderService.OrderItemRequest createOrderItemRequest(Long menuId, Integer quantity) {
        OrderService.OrderItemRequest request = new OrderService.OrderItemRequest();
        request.setMenuId(menuId);
        request.setQuantity(quantity);
        return request;
    }
}