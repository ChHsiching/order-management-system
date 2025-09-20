package tech.chhsich.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.entity.ShoppingCart;
import tech.chhsich.backend.mapper.MenuMapper;
import tech.chhsich.backend.mapper.ShoppingCartMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车服务类
 *
 * 提供完整的购物车业务逻辑实现，包括商品添加、数量更新、删除、清空等功能。
 * 基于项目设计文档第4.1节的购物车管理功能和第6.3.5节的点餐管理功能实现。
 * 购物车是连接菜单浏览和订单提交的核心环节，支持注册会员的订餐流程。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@Service
public class ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final MenuMapper menuMapper;

    /**
     * 构造函数注入依赖
     *
     * @param shoppingCartMapper 购物车数据访问层
     * @param menuMapper 菜单数据访问层
     */
    public ShoppingCartService(ShoppingCartMapper shoppingCartMapper, MenuMapper menuMapper) {
        this.shoppingCartMapper = shoppingCartMapper;
        this.menuMapper = menuMapper;
    }

    /**
     * 添加商品到购物车
     *
     * 将指定商品添加到用户的购物车中。如果商品已存在，将更新数量；
     * 如果商品不存在，将创建新的购物车项。
     * 基于项目设计文档第6.3.5节的点餐管理功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @param productId 商品ID，关联到menu表的主键
     * @param quantity 商品数量，必须大于0
     * @return boolean 添加成功返回true，失败返回false
     * @throws RuntimeException 当商品不存在、已下架或添加失败时抛出异常
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
     *
     * 根据用户名获取该用户购物车中的所有商品信息。
     * 用于前端展示购物车内容和订单确认页面。
     *
     * @param username 用户名，用于标识购物车归属
     * @return List<ShoppingCart> 购物车商品列表，如果没有商品则返回空列表
     */
    public List<ShoppingCart> getUserCart(String username) {
        return shoppingCartMapper.findByUsername(username);
    }

    /**
     * 更新购物车商品数量
     *
     * 更新购物车中指定商品的数量。如果数量小于等于0，将自动移除该商品。
     * 基于项目设计文档第4.1节的购物车管理功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @param productId 商品ID，关联到menu表的主键
     * @param quantity 新的商品数量，必须大于0
     * @return boolean 更新成功返回true，失败返回false
     * @throws RuntimeException 当购物车中不存在该商品时抛出异常
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
     *
     * 从用户的购物车中移除指定的商品。
     * 基于项目设计文档第6.3.5节的点餐管理功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @param productId 商品ID，关联到menu表的主键
     * @return boolean 移除成功返回true，失败返回false
     */
    public boolean removeFromCart(String username, Long productId) {
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                   .eq("product_id", productId);

        return shoppingCartMapper.delete(queryWrapper) > 0;
    }

    /**
     * 清空用户购物车
     *
     * 清空指定用户的所有购物车商品。通常用于订单提交后或用户主动清空。
     * 基于项目设计文档第4.1节的购物车管理功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @return boolean 清空成功返回true，失败返回false
     */
    public boolean clearUserCart(String username) {
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return shoppingCartMapper.delete(queryWrapper) > 0;
    }

    /**
     * 计算购物车总金额
     *
     * 计算用户购物车中所有商品的总金额（单价 × 数量的总和）。
     * 为订单提交提供金额基础，基于项目设计文档第4.1节的购物车功能实现。
     *
     * @param username 用户名，用于标识购物车归属
     * @return Double 购物车总金额，如果没有商品则返回0.0
     */
    public Double calculateTotal(String username) {
        List<ShoppingCart> cartItems = getUserCart(username);
        return cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    /**
     * 获取购物车商品数量
     *
     * 获取用户购物车中的商品总数量（所有商品数量的总和，不是商品种类数）。
     * 用于前端显示购物车图标上的数量标记。
     *
     * @param username 用户名，用于标识购物车归属
     * @return int 购物车商品总数量，如果没有商品则返回0
     */
    public int getCartItemCount(String username) {
        return shoppingCartMapper.countByUsername(username);
    }

    /**
     * 根据用户名和商品ID查找购物车项
     *
     * 查找指定用户购物车中的特定商品项。用于检查商品是否存在或获取商品数量。
     *
     * @param username 用户名，用于标识购物车归属
     * @param productId 商品ID，关联到menu表的主键
     * @return ShoppingCart 找到的购物车项，如果不存在则返回null
     */
    public ShoppingCart findByUsernameAndProductId(String username, Long productId) {
        return shoppingCartMapper.findByUsernameAndProductId(username, productId);
    }
}