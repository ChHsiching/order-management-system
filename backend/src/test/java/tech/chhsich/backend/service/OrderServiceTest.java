package tech.chhsich.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tech.chhsich.backend.entity.OrderInfo;
import tech.chhsich.backend.entity.OrderEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单服务测试类
 *
 * 测试订单管理和外键约束功能。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    /**
     * 测试创建订单的基本功能
     */
    @Test
    public void testCreateOrder() {
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 2),
            createOrderItemRequest(2L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");

        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertEquals("test_admin", order.getUsername());
        assertEquals("测试地址", order.getAddress());
        assertEquals("13900139000", order.getPhone());
        assertEquals(0, order.getStatus());
        assertTrue(order.getTotalPrice() > 0);
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

    /**
     * 测试创建订单时的用户验证
     */
    @Test
    public void testCreateOrderWithInvalidUser() {
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder("nonexistent_user", items, "测试地址", "13900139000");
        });
    }

    /**
     * 测试创建订单时的菜品验证
     */
    @Test
    public void testCreateOrderWithInvalidMenu() {
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(999L, 1) // 不存在的菜品ID
        );

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder("admin", items, "测试地址", "13900139000");
        });
    }

    /**
     * 测试创建订单时的参数验证
     */
    @Test
    public void testCreateOrderParameterValidation() {
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        // 测试空地址
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder("test_admin", items, "", "13900139000");
        });

        // 测试空电话
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder("test_admin", items, "测试地址", "");
        });

        // 测试空菜单ID
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder("test_admin", List.of(createOrderItemRequest(null, 1)), "测试地址", "13900139000");
        });

        // 测试无效数量
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder("test_admin", List.of(createOrderItemRequest(1L, 0)), "测试地址", "13900139000");
        });
    }

    /**
     * 测试取消订单功能
     */
    @Test
    public void testCancelOrder() {
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");
        assertNotNull(order);

        // 取消订单
        boolean result = orderService.cancelOrder(order.getOrderId());
        assertTrue(result);

        // 验证订单状态已更新
        OrderInfo cancelledOrder = orderService.getOrderById(order.getOrderId());
        assertEquals(2, cancelledOrder.getStatus());
    }

    /**
     * 测试删除订单功能
     */
    @Test
    public void testDeleteOrder() {
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");
        assertNotNull(order);

        // 删除订单
        boolean result = orderService.deleteOrder(order.getOrderId());
        assertTrue(result);

        // 验证订单已被删除
        OrderInfo deletedOrder = orderService.getOrderById(order.getOrderId());
        assertNull(deletedOrder);
    }

    /**
     * 测试删除已受理订单的限制
     */
    @Test
    public void testDeleteProcessedOrder() {
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");
        assertNotNull(order);

        // 更新订单状态为已受理
        orderService.updateOrderStatus(order.getOrderId(), 1);

        // 尝试删除已受理的订单，应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            orderService.deleteOrder(order.getOrderId());
        });
    }

    /**
     * 测试获取订单条目功能
     */
    @Test
    public void testGetOrderItems() {
        List<OrderService.OrderItemRequest> items = List.of(
            createOrderItemRequest(1L, 2),
            createOrderItemRequest(2L, 1)
        );

        OrderInfo order = orderService.createOrder("test_admin", items, "测试地址", "13900139000");
        assertNotNull(order);

        List<OrderEntry> orderItems = orderService.getOrderItems(order.getOrderId());
        assertNotNull(orderItems);
        assertEquals(2, orderItems.size());

        // 验证订单条目信息
        for (OrderEntry entry : orderItems) {
            assertEquals(order.getOrderId(), entry.getOrderId());
            assertNotNull(entry.getProductName());
            assertTrue(entry.getPrice() > 0);
            assertTrue(entry.getProductNum() > 0);
        }
    }
}