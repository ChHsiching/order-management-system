package tech.chhsich.backend.controller;

import tech.chhsich.backend.entity.OrderInfo;
import tech.chhsich.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "订单创建、查询和管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "创建订单", description = "创建新的订单")
    @ApiResponse(responseCode = "200", description = "订单创建成功")
    @ApiResponse(responseCode = "400", description = "订单创建失败")
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "订单项列表", required = true) @RequestBody List<OrderService.OrderItemRequest> items,
            @Parameter(description = "配送地址", required = true) @RequestParam String address,
            @Parameter(description = "联系电话", required = true) @RequestParam String phone) {
        try {
            OrderInfo order = orderService.createOrder(username, items, address, phone);
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
    @GetMapping("/{orderid}")
    public ResponseEntity<OrderInfo> getOrderById(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderid) {
        OrderInfo order = orderService.getOrderById(orderid);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "获取订单项", description = "根据订单ID获取所有订单项")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "400", description = "获取失败")
    @GetMapping("/{orderid}/items")
    public ResponseEntity<?> getOrderItems(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderid) {
        try {
            return ResponseEntity.ok(orderService.getOrderItems(orderid));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}