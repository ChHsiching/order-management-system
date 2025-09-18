package tech.chhsich.backend.controller;

import lombok.Data;
import tech.chhsich.backend.entity.OrderInfo;
import tech.chhsich.backend.entity.OrderEntry;
import tech.chhsich.backend.enums.OrderStatus;
import tech.chhsich.backend.exception.OrderStatusTransitionException;
import tech.chhsich.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "订单创建、查询和管理接口")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new OrderController wired with an OrderService.
     *
     * The provided OrderService is used to perform order-related operations delegated by this controller.
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 创建订单请求DTO
    @Data
    public static class OrderCreateRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotEmpty(message = "订单项不能为空")
        @Valid
        private List<OrderService.OrderItemRequest> items;

        @NotBlank(message = "收货地址不能为空")
        private String address;

        @NotBlank(message = "联系电话不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
    }

    /**
     * Create a new order from the provided request payload.
     *
     * The request is validated with Jakarta Bean Validation annotations on the DTO.
     *
     * @param request the order creation payload (username, items, address, phone)
     * @return 200 OK with the created OrderInfo on success; 400 Bad Request with an error message on failure
     */
    @Operation(summary = "创建订单", description = "创建新的订单")
    @ApiResponse(responseCode = "200", description = "订单创建成功")
    @ApiResponse(responseCode = "400", description = "订单创建失败")
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        try {
            OrderInfo order = orderService.createOrder(
                    request.getUsername(),
                    request.getItems(),
                    request.getAddress(),
                    request.getPhone()
            );
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieve all orders for a given user.
     *
     * Returns the list of OrderInfo for the specified username.
     *
     * @param username the username whose orders are requested
     * @return a ResponseEntity containing a list of OrderInfo (HTTP 200)
     */
    @Operation(summary = "获取用户订单", description = "根据用户名获取所有订单")
    @GetMapping("/user/{username}")
    public ResponseEntity<List<OrderInfo>> getUserOrders(
            @Parameter(description = "用户名", required = true) @PathVariable String username) {
        List<OrderInfo> orders = orderService.getUserOrders(username);
        return ResponseEntity.ok(orders);
    }

    /**
     * Retrieve order details by order ID.
     *
     * Returns the order when found (HTTP 200) or a 404 response when no order exists with the given ID.
     *
     * @param orderId the ID of the order to retrieve
     * @return a ResponseEntity containing the OrderInfo (200) or a 404 Not Found response if not present
     */
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "订单不存在")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderInfo> getOrderById(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        OrderInfo order = orderService.getOrderById(orderId);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieve all items for a given order.
     *
     * Returns HTTP 200 with the order's items on success, or HTTP 400 with the exception message
     * if retrieval fails.
     *
     * @param orderId the ID of the order whose items should be fetched
     * @return ResponseEntity containing the list of order items on success or an error message on failure
     */
    @Operation(summary = "获取订单项", description = "根据订单ID获取所有订单项")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "400", description = "获取失败")
    @GetMapping("/{orderId}/items")
    public ResponseEntity<?> getOrderItems(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        try {
            return ResponseEntity.ok(orderService.getOrderItems(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== 订单状态管理 API ==========

    /**
     * 支付订单
     */
    @Operation(summary = "支付订单", description = "将订单状态更新为已支付")
    @ApiResponse(responseCode = "200", description = "支付成功")
    @ApiResponse(responseCode = "400", description = "支付失败")
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        try {
            OrderInfo order = orderService.payOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (OrderStatusTransitionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 开始配送
     */
    @Operation(summary = "开始配送", description = "将订单状态更新为配送中")
    @ApiResponse(responseCode = "200", description = "操作成功")
    @ApiResponse(responseCode = "400", description = "操作失败")
    @PostMapping("/{orderId}/start-delivery")
    public ResponseEntity<?> startDelivery(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        try {
            OrderInfo order = orderService.startDelivery(orderId);
            return ResponseEntity.ok(order);
        } catch (OrderStatusTransitionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @Operation(summary = "确认收货", description = "将订单状态更新为已完成")
    @ApiResponse(responseCode = "200", description = "操作成功")
    @ApiResponse(responseCode = "400", description = "操作失败")
    @PostMapping("/{orderId}/confirm-delivery")
    public ResponseEntity<?> confirmDelivery(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        try {
            OrderInfo order = orderService.confirmDelivery(orderId);
            return ResponseEntity.ok(order);
        } catch (OrderStatusTransitionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @Operation(summary = "取消订单", description = "将订单状态更新为已取消")
    @ApiResponse(responseCode = "200", description = "取消成功")
    @ApiResponse(responseCode = "400", description = "取消失败")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId,
            @Parameter(description = "取消原因", required = true) @RequestParam String reason) {
        try {
            OrderInfo order = orderService.cancelOrder(orderId, reason);
            return ResponseEntity.ok(order);
        } catch (OrderStatusTransitionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 申请退款
     */
    @Operation(summary = "申请退款", description = "将订单状态更新为退款中")
    @ApiResponse(responseCode = "200", description = "申请成功")
    @ApiResponse(responseCode = "400", description = "申请失败")
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> requestRefund(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId,
            @Parameter(description = "退款原因", required = true) @RequestParam String reason) {
        try {
            OrderInfo order = orderService.requestRefund(orderId, reason);
            return ResponseEntity.ok(order);
        } catch (OrderStatusTransitionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 完成退款
     */
    @Operation(summary = "完成退款", description = "将订单状态更新为已退款")
    @ApiResponse(responseCode = "200", description = "退款完成")
    @ApiResponse(responseCode = "400", description = "退款失败")
    @PostMapping("/{orderId}/complete-refund")
    public ResponseEntity<?> completeRefund(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId,
            @Parameter(description = "退款完成原因", required = true) @RequestParam String reason) {
        try {
            OrderInfo order = orderService.completeRefund(orderId, reason);
            return ResponseEntity.ok(order);
        } catch (OrderStatusTransitionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== 订单条目管理 API ==========

    /**
     * 添加订单条目
     */
    @Operation(summary = "添加订单条目", description = "向指定订单添加新的商品条目")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @ApiResponse(responseCode = "400", description = "添加失败")
    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addOrderEntry(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId,
            @Valid @RequestBody OrderEntryCreateRequest request) {
        try {
            boolean success = orderService.addOrderEntry(orderId, request.getMenuId(), request.getQuantity());
            if (success) {
                return ResponseEntity.ok(Map.of("message", "添加成功"));
            } else {
                return ResponseEntity.badRequest().body("添加失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新订单条目数量
     */
    @Operation(summary = "更新订单条目数量", description = "更新指定订单条目的商品数量")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "400", description = "更新失败")
    @PutMapping("/{orderId}/items/{entryId}")
    public ResponseEntity<?> updateOrderEntryQuantity(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId,
            @Parameter(description = "条目ID", required = true) @PathVariable Long entryId,
            @Valid @RequestBody OrderEntryUpdateRequest request) {
        try {
            boolean success = orderService.updateOrderEntryQuantity(orderId, entryId, request.getQuantity());
            if (success) {
                return ResponseEntity.ok(Map.of("message", "更新成功"));
            } else {
                return ResponseEntity.badRequest().body("更新失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除订单条目
     */
    @Operation(summary = "删除订单条目", description = "从指定订单中删除商品条目")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @ApiResponse(responseCode = "400", description = "删除失败")
    @DeleteMapping("/{orderId}/items/{entryId}")
    public ResponseEntity<?> removeOrderEntry(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId,
            @Parameter(description = "条目ID", required = true) @PathVariable Long entryId) {
        try {
            boolean success = orderService.removeOrderEntry(orderId, entryId);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "删除成功"));
            } else {
                return ResponseEntity.badRequest().body("删除失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 重新计算订单总额
     */
    @Operation(summary = "重新计算订单总额", description = "根据订单条目重新计算订单总金额")
    @ApiResponse(responseCode = "200", description = "计算成功")
    @ApiResponse(responseCode = "400", description = "计算失败")
    @PostMapping("/{orderId}/recalculate")
    public ResponseEntity<?> recalculateOrderTotal(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        try {
            double newTotal = orderService.recalculateOrderTotal(orderId);
            return ResponseEntity.ok(Map.of("totalPrice", newTotal));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== 订单状态查询 API ==========

    /**
     * 获取订单状态描述
     */
    @Operation(summary = "获取订单状态描述", description = "根据状态代码获取状态描述")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/status/{statusCode}")
    public ResponseEntity<?> getStatusDescription(
            @Parameter(description = "状态代码", required = true) @PathVariable int statusCode) {
        String description = orderService.getOrderStatusDescription(statusCode);
        return ResponseEntity.ok(Map.of("statusCode", statusCode, "description", description));
    }

    /**
     * 获取订单的可用状态转换
     */
    @Operation(summary = "获取可用状态转换", description = "获取订单当前状态下可转换的状态列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "订单不存在")
    @GetMapping("/{orderId}/available-transitions")
    public ResponseEntity<?> getAvailableTransitions(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        OrderStatus[] transitions = orderService.getAvailableStatusTransitions(orderId);
        return ResponseEntity.ok(Map.of("availableTransitions", transitions));
    }

    /**
     * 检查订单是否为最终状态
     */
    @Operation(summary = "检查最终状态", description = "检查订单是否为最终状态（已完成、已取消、已退款）")
    @ApiResponse(responseCode = "200", description = "检查成功")
    @GetMapping("/{orderId}/is-final")
    public ResponseEntity<?> isFinalStatus(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        boolean isFinal = orderService.isOrderFinalStatus(orderId);
        return ResponseEntity.ok(Map.of("isFinal", isFinal));
    }

    // ========== DTO 类定义 ==========

    /**
     * 创建订单条目请求DTO
     */
    @Data
    public static class OrderEntryCreateRequest {
        @NotNull(message = "菜品ID不能为空")
        private Long menuId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;
    }

    /**
     * 更新订单条目请求DTO
     */
    @Data
    public static class OrderEntryUpdateRequest {
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;
    }
}