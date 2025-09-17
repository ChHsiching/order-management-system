package tech.chhsich.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.entity.ShoppingCart;
import tech.chhsich.backend.mapper.MenuMapper;
import tech.chhsich.backend.mapper.ShoppingCartMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final MenuMapper menuMapper;

    public ShoppingCartService(ShoppingCartMapper shoppingCartMapper, MenuMapper menuMapper) {
        this.shoppingCartMapper = shoppingCartMapper;
        this.menuMapper = menuMapper;
    }

    /**
     * 添加商品到购物车
     */
    public boolean addToCart(String username, Long productId, Integer quantity) {
        Menu menu = menuMapper.selectById(productId);
        if (menu == null || menu.getProductLock() == 1) {
            throw new RuntimeException("商品不存在或已下架");
        }

        ShoppingCart existingItem = shoppingCartMapper.findByUsernameAndProductId(username, productId);

        if (existingItem != null) {
            // 更新数量
            shoppingCartMapper.updateQuantity(username, productId, quantity);
        } else {
            // 新增购物车项
            ShoppingCart cartItem = new ShoppingCart();
            cartItem.setUsername(username);
            cartItem.setProductId(productId);
            cartItem.setProductName(menu.getName());
            cartItem.setPrice(menu.getHotPrice()); // 使用热销价
            cartItem.setQuantity(quantity);
            cartItem.setCreateTime(LocalDateTime.now());
            cartItem.setUpdateTime(LocalDateTime.now());

            shoppingCartMapper.insert(cartItem);
        }

        return true;
    }

    /**
     * 获取用户的购物车列表
     */
    public List<ShoppingCart> getUserCart(String username) {
        return shoppingCartMapper.findByUsername(username);
    }

    /**
     * 更新购物车商品数量
     */
    public boolean updateCartItem(String username, Long productId, Integer quantity) {
        if (quantity <= 0) {
            return removeFromCart(username, productId);
        }

        ShoppingCart cartItem = shoppingCartMapper.findByUsernameAndProductId(username, productId);
        if (cartItem == null) {
            throw new RuntimeException("购物车中不存在该商品");
        }

        cartItem.setQuantity(quantity);
        cartItem.setUpdateTime(LocalDateTime.now());

        return shoppingCartMapper.updateById(cartItem) > 0;
    }

    /**
     * 从购物车移除商品
     */
    public boolean removeFromCart(String username, Long productId) {
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                   .eq("productid", productId);

        return shoppingCartMapper.delete(queryWrapper) > 0;
    }

    /**
     * 清空用户购物车
     */
    public boolean clearUserCart(String username) {
        return shoppingCartMapper.deleteByUsername(username) > 0;
    }

    /**
     * 计算购物车总金额
     */
    public Double calculateTotal(String username) {
        List<ShoppingCart> cartItems = getUserCart(username);
        return cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    /**
     * 获取购物车商品数量
     */
    public int getCartItemCount(String username) {
        return shoppingCartMapper.countByUsername(username);
    }

    /**
     * 根据用户名和商品ID查找购物车项
     */
    public ShoppingCart findByUsernameAndProductId(String username, Long productId) {
        return shoppingCartMapper.findByUsernameAndProductId(username, productId);
    }
}