package tech.chhsich.backend.controller;

import tech.chhsich.backend.entity.OrderHistory;
import tech.chhsich.backend.service.OrderHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单历史记录控制器
 *
 * 提供订单历史记录的查询和管理API，包括状态变更历史、统计分析等功能。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@RestController
@RequestMapping("/api/order-history")
@Tag(name = "订单历史记录", description = "订单状态变更历史记录查询和管理接口")
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    /**
     * 构造函数
     *
     * @param orderHistoryService 订单历史记录服务
     */
    public OrderHistoryController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    /**
     * 获取订单的完整历史记录
     */
    @Operation(summary = "获取订单历史", description = "根据订单ID获取完整的状态变更历史记录")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "订单不存在")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderHistory>> getOrderHistory(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        List<OrderHistory> history = orderHistoryService.getFormattedOrderHistory(orderId);
        return ResponseEntity.ok(history);
    }

    /**
     * 获取订单历史记录数量
     */
    @Operation(summary = "获取历史记录数量", description = "获取指定订单的状态变更次数")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/order/{orderId}/count")
    public ResponseEntity<Map<String, Integer>> getHistoryCount(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        int count = orderHistoryService.getStatusChangeCount(orderId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 获取订单最新状态变更记录
     */
    @Operation(summary = "获取最新历史记录", description = "获取指定订单的最新状态变更记录")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "订单不存在")
    @GetMapping("/order/{orderId}/latest")
    public ResponseEntity<OrderHistory> getLatestHistory(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId) {
        OrderHistory latest = orderHistoryService.getLatestHistory(orderId);
        if (latest != null) {
            return ResponseEntity.ok(latest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 检查订单是否有指定状态的历史记录
     */
    @Operation(summary = "检查状态历史", description = "检查订单是否存在指定状态的历史记录")
    @ApiResponse(responseCode = "200", description = "检查成功")
    @GetMapping("/order/{orderId}/has-status")
    public ResponseEntity<Map<String, Boolean>> hasStatusHistory(
            @Parameter(description = "订单ID", required = true) @PathVariable String orderId,
            @Parameter(description = "状态代码", required = true) @RequestParam Integer statusCode) {
        boolean hasHistory = orderHistoryService.hasStatusHistory(orderId,
            tech.chhsich.backend.enums.OrderStatus.fromCode(statusCode));
        return ResponseEntity.ok(Map.of("hasStatusHistory", hasHistory));
    }

    /**
     * 获取指定时间范围内的历史记录（管理员功能）
     */
    @Operation(summary = "按时间范围查询", description = "获取指定时间范围内的订单历史记录")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/time-range")
    public ResponseEntity<List<OrderHistory>> getHistoryByTimeRange(
            @Parameter(description = "开始时间", required = true) @RequestParam String startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<OrderHistory> history = orderHistoryService.getHistoryByTimeRange(start, end);
        return ResponseEntity.ok(history);
    }

    /**
     * 获取指定操作人员的历史记录（管理员功能）
     */
    @Operation(summary = "按操作人员查询", description = "获取指定操作人员的订单历史记录")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/operator/{operator}")
    public ResponseEntity<List<OrderHistory>> getHistoryByOperator(
            @Parameter(description = "操作人员", required = true) @PathVariable String operator) {
        List<OrderHistory> history = orderHistoryService.getHistoryByOperator(operator);
        return ResponseEntity.ok(history);
    }

    /**
     * 清理过期历史记录（管理员功能）
     */
    @Operation(summary = "清理过期记录", description = "清理指定天数之前的过期历史记录")
    @ApiResponse(responseCode = "200", description = "清理成功")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredHistory(
            @Parameter(description = "保留天数", required = true) @RequestParam Integer daysToKeep) {
        int cleanedCount = orderHistoryService.cleanExpiredHistory(daysToKeep);
        return ResponseEntity.ok(Map.of(
            "cleanedCount", cleanedCount,
            "message", "成功清理 " + cleanedCount + " 条过期历史记录"
        ));
    }
}