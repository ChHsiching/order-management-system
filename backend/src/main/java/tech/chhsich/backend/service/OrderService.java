package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.entity.OrderInfo;
import tech.chhsich.backend.entity.OrderEntry;
import tech.chhsich.backend.mapper.MenuMapper;
import tech.chhsich.backend.mapper.OrderInfoMapper;
import tech.chhsich.backend.mapper.OrderEntryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderEntryMapper orderEntryMapper;
    private final UserService userService;
    private final MenuMapper menuMapper;

    public OrderService(OrderInfoMapper orderInfoMapper, OrderEntryMapper orderEntryMapper,
                       UserService userService, MenuMapper menuMapper) {
        this.orderInfoMapper = orderInfoMapper;
        this.orderEntryMapper = orderEntryMapper;
        this.userService = userService;
        this.menuMapper = menuMapper;
    }

    @Transactional
    public OrderInfo createOrder(String username, List<OrderItemRequest> items, String address, String phone) {
        // 检查用户是否存在
        if (userService.getUserByUsername(username) == null) {
            throw new RuntimeException("用户不存在");
        }

        OrderInfo order = new OrderInfo();
        order.setOrderId(generateOrderId());
        order.setUsername(username);
        order.setAddress(address);
        order.setPhone(phone);
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(0); // 0-待受理

        double totalPrice = 0;

        // 计算总价并准备订单项
        for (OrderItemRequest item : items) {
            Menu menu = menuMapper.selectById(item.getMenuId());
            if (menu == null || menu.getProductLock() == 1) {
                throw new RuntimeException("商品不存在或已下架: " + item.getMenuId());
            }

            totalPrice += menu.getHotPrice() * item.getQuantity();
        }

        order.setTotalPrice(totalPrice);

        // 插入订单主信息
        orderInfoMapper.insert(order);

        // 插入订单项
        for (OrderItemRequest item : items) {
            Menu menu = menuMapper.selectById(item.getMenuId());

            OrderEntry entry = new OrderEntry();
            entry.setProductId(menu.getId());
            entry.setProductName(menu.getName());
            entry.setPrice(menu.getHotPrice()); // 使用热销价
            entry.setProductNum(item.getQuantity());
            entry.setOrderId(order.getOrderId());

            orderEntryMapper.insert(entry);

            // 更新销量
            menu.setSales((menu.getSales() != null ? menu.getSales() : 0) + item.getQuantity());
            menuMapper.updateById(menu);
        }

        return order;
    }

    public List<OrderInfo> getUserOrders(String username) {
        return orderInfoMapper.findByUsername(username);
    }

    public OrderInfo getOrderById(String orderid) {
        return orderInfoMapper.findByOrderid(orderid);
    }

    public List<OrderEntry> getOrderItems(String orderid) {
        return orderEntryMapper.findByOrderid(orderid);
    }

    @Transactional
    public boolean cancelOrder(String orderid) {
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        if (order != null && order.getStatus() == 0) {
            order.setStatus(2); // 2-已取消
            orderInfoMapper.updateById(order);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateOrderStatus(String orderid, Integer status) {
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        if (order != null) {
            order.setStatus(status);
            orderInfoMapper.updateById(order);
            return true;
        }
        return false;
    }

    private String generateOrderId() {
        return "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public static class OrderItemRequest {
        private Long menuId;
        private Integer quantity;

        // Getters and Setters
        public Long getMenuId() { return menuId; }
        public void setMenuId(Long menuId) { this.menuId = menuId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}