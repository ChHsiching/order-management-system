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