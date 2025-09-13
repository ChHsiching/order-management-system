package tech.chhsich.backend.controller;

import lombok.Data;
import tech.chhsich.backend.entity.OrderInfo;
import tech.chhsich.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "订单创建、查询和管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

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

    @Operation(summary = "获取用户订单", description = "根据用户名获取所有订单")
    @GetMapping("/user/{username}")
    public ResponseEntity<List<OrderInfo>> getUserOrders(
            @Parameter(description = "用户名", required = true) @PathVariable String username) {
        List<OrderInfo> orders = orderService.getUserOrders(username);
        return ResponseEntity.ok(orders);
    }

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
}