package tech.chhsich.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.entity.ResponseMessage;
import tech.chhsich.backend.entity.ShoppingCart;
import tech.chhsich.backend.service.ShoppingCartService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@Validated
@Tag(name = "购物车管理", description = "用户购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 获取用户购物车商品列表
     *
     * 根据用户名获取当前登录用户的购物车中所有商品信息。
     * 此接口符合RESTful最佳实践，使用标准GET方法获取资源。
     *
     * @param username 用户名，用于标识购物车归属
     * @return ResponseMessage 包含购物车商品列表的响应对象
     * @throws Exception 当获取购物车信息失败时抛出异常
     */
    @GetMapping
    @Operation(summary = "获取用户购物车", description = "获取当前登录用户的购物车商品列表")
    public ResponseMessage getUserCart(@RequestParam @Parameter(description = "用户名") String username) {
        try {
            List<ShoppingCart> cartItems = shoppingCartService.getUserCart(username);
            return ResponseMessage.success(cartItems);
        } catch (Exception e) {
            return ResponseMessage.error("获取购物车失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户购物车商品列表（兼容接口）
     *
     * 此接口为Issue #41要求的兼容性接口，内部转发到主要的getUserCart方法。
     * 保持了与现有系统的兼容性，同时满足特定需求。
     *
     * @param username 用户名，用于标识购物车归属
     * @return ResponseMessage 包含购物车商品列表的响应对象
     * @throws Exception 当获取购物车信息失败时抛出异常
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户购物车列表（兼容）", description = "获取当前登录用户的购物车商品列表（兼容接口）")
    public ResponseMessage getUserCartList(@RequestParam @Parameter(description = "用户名") String username) {
        return getUserCart(username);
    }

    /**
     * 添加商品到购物车
     *
     * 将指定商品添加到用户的购物车中。如果商品已存在，将更新数量。
     * 基于项目设计文档第6.3.5节的点餐管理功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @param productId 商品ID，关联到menu表的主键
     * @param quantity 商品数量，必须大于0
     * @return ResponseMessage 包含操作结果的响应对象
     * @throws Exception 当添加商品到购物车失败时抛出异常
     */
    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车", description = "将指定商品添加到用户购物车")
    public ResponseMessage addToCart(
            @RequestParam @Parameter(description = "用户名") String username,
            @RequestParam @Parameter(description = "商品ID") Long productId,
            @RequestParam @Parameter(description = "商品数量") Integer quantity) {
        try {
            if (quantity <= 0) {
                return ResponseMessage.error("商品数量必须大于0");
            }

            boolean success = shoppingCartService.addToCart(username, productId, quantity);
            if (success) {
                return ResponseMessage.success("添加到购物车成功");
            } else {
                return ResponseMessage.error("添加到购物车失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("添加到购物车失败: " + e.getMessage());
        }
    }

    /**
     * 更新购物车商品数量
     *
     * 更新购物车中指定商品的数量。支持增加或减少商品数量。
     * 基于项目设计文档第4.1节的购物车管理功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @param productId 商品ID，关联到menu表的主键
     * @param quantity 新的商品数量，必须大于0
     * @return ResponseMessage 包含操作结果的响应对象
     * @throws Exception 当更新购物车商品数量失败时抛出异常
     */
    @PutMapping("/update")
    @Operation(summary = "更新购物车商品数量", description = "更新购物车中指定商品的数量")
    public ResponseMessage updateCartItem(
            @RequestParam @Parameter(description = "用户名") String username,
            @RequestParam @Parameter(description = "商品ID") Long productId,
            @RequestParam @Parameter(description = "新的商品数量") Integer quantity) {
        try {
            boolean success = shoppingCartService.updateCartItem(username, productId, quantity);
            if (success) {
                return ResponseMessage.success("更新购物车成功");
            } else {
                return ResponseMessage.error("更新购物车失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("更新购物车失败: " + e.getMessage());
        }
    }

    /**
     * 从购物车移除商品
     *
     * 从用户的购物车中移除指定的商品。
     * 基于项目设计文档第6.3.5节的点餐管理功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @param productId 商品ID，关联到menu表的主键
     * @return ResponseMessage 包含操作结果的响应对象
     * @throws Exception 当移除购物车商品失败时抛出异常
     */
    @DeleteMapping("/remove")
    @Operation(summary = "从购物车移除商品", description = "从购物车中移除指定的商品")
    public ResponseMessage removeFromCart(
            @RequestParam @Parameter(description = "用户名") String username,
            @RequestParam @Parameter(description = "商品ID") Long productId) {
        try {
            boolean success = shoppingCartService.removeFromCart(username, productId);
            if (success) {
                return ResponseMessage.success("移除商品成功");
            } else {
                return ResponseMessage.error("移除商品失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("移除商品失败: " + e.getMessage());
        }
    }

    /**
     * 清空购物车
     *
     * 清空当前用户的所有购物车商品。通常用于订单提交后或用户主动清空。
     * 基于项目设计文档第4.1节的购物车管理功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @return ResponseMessage 包含操作结果的响应对象
     * @throws Exception 当清空购物车失败时抛出异常
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车", description = "清空当前用户的所有购物车商品")
    public ResponseMessage clearUserCart(@RequestParam @Parameter(description = "用户名") String username) {
        try {
            boolean success = shoppingCartService.clearUserCart(username);
            if (success) {
                return ResponseMessage.success("清空购物车成功");
            } else {
                return ResponseMessage.error("清空购物车失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("清空购物车失败: " + e.getMessage());
        }
    }

    /**
     * 计算购物车总金额
     *
     * 计算当前用户购物车中所有商品的总金额。
     * 为订单提交提供金额基础，基于项目设计文档第4.1节的购物车功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @return ResponseMessage 包含总金额的响应对象，格式为 {"total": 金额}
     * @throws Exception 当计算总金额失败时抛出异常
     */
    @GetMapping("/total")
    @Operation(summary = "计算购物车总金额", description = "计算当前用户购物车中所有商品的总金额")
    public ResponseMessage calculateTotal(@RequestParam @Parameter(description = "用户名") String username) {
        try {
            Double total = shoppingCartService.calculateTotal(username);
            return ResponseMessage.success(Map.of("total", total));
        } catch (Exception e) {
            return ResponseMessage.error("计算总金额失败: " + e.getMessage());
        }
    }

    /**
     * 获取购物车商品数量
     *
     * 获取当前用户购物车中的商品总数量（不是商品种类数，而是所有商品的数量总和）。
     * 用于前端显示购物车图标上的数量标记。
     *
     * @param username 用户名，用于标识购物车归属
     * @return ResponseMessage 包含商品总数量的响应对象，格式为 {"count": 数量}
     * @throws Exception 当获取购物车商品数量失败时抛出异常
     */
    @GetMapping("/count")
    @Operation(summary = "获取购物车商品数量", description = "获取当前用户购物车中的商品总数量")
    public ResponseMessage getCartItemCount(@RequestParam @Parameter(description = "用户名") String username) {
        try {
            int count = shoppingCartService.getCartItemCount(username);
            return ResponseMessage.success(Map.of("count", count));
        } catch (Exception e) {
            return ResponseMessage.error("获取购物车数量失败: " + e.getMessage());
        }
    }

    /**
     * 检查购物车中特定商品数量
     *
     * 获取购物车中指定商品的数量。如果商品不存在于购物车中，返回0。
     * 用于前端显示购物车中单个商品的数量状态。
     *
     * @param username 用户名，用于标识购物车归属
     * @param productId 商品ID，关联到menu表的主键
     * @return ResponseMessage 包含特定商品数量的响应对象，格式为 {"quantity": 数量}
     * @throws Exception 当获取特定商品数量失败时抛出异常
     */
    @GetMapping("/item-count")
    @Operation(summary = "检查购物车中特定商品数量", description = "获取购物车中指定商品的数量")
    public ResponseMessage getCartItemQuantity(
            @RequestParam @Parameter(description = "用户名") String username,
            @RequestParam @Parameter(description = "商品ID") Long productId) {
        try {
            ShoppingCart cartItem = shoppingCartService.findByUsernameAndProductId(username, productId);
            int quantity = cartItem != null ? cartItem.getQuantity() : 0;
            return ResponseMessage.success(Map.of("quantity", quantity));
        } catch (Exception e) {
            return ResponseMessage.error("获取商品数量失败: " + e.getMessage());
        }
    }
}