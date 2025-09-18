package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.entity.OrderInfo;
import tech.chhsich.backend.entity.OrderEntry;
import tech.chhsich.backend.enums.OrderStatus;
import tech.chhsich.backend.exception.OrderStatusTransitionException;
import tech.chhsich.backend.mapper.MenuMapper;
import tech.chhsich.backend.mapper.OrderInfoMapper;
import tech.chhsich.backend.mapper.OrderEntryMapper;
import tech.chhsich.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderEntryMapper orderEntryMapper;
    private final UserService userService;
    private final MenuMapper menuMapper;
    private final OrderStateMachine orderStateMachine;

    /**
     * Constructs an OrderService with the required persistence mappers and user service.
     *
     * These dependencies are used to create and manage orders, order entries, and to validate users.
     */
    public OrderService(OrderInfoMapper orderInfoMapper, OrderEntryMapper orderEntryMapper,
                       UserService userService, MenuMapper menuMapper, OrderStateMachine orderStateMachine) {
        this.orderInfoMapper = orderInfoMapper;
        this.orderEntryMapper = orderEntryMapper;
        this.userService = userService;
        this.menuMapper = menuMapper;
        this.orderStateMachine = orderStateMachine;
    }

    /**
     * Creates a new order for the given user by validating the user, validating and pricing each requested menu item,
     * persisting the order and its entries, and updating menu sales.
     *
     * <p>The method generates a new order ID, sets create time and initial status (0 = pending), calculates the total
     * price from each item's hot price and quantity, inserts the order and its OrderEntry records, and increments the
     * corresponding Menu.sales for each ordered item.</p>
     *
     * @param username the username of the purchaser
     * @param items a list of OrderItemRequest describing menu IDs and quantities to include in the order
     * @param address delivery address for the order
     * @param phone contact phone number for the order
     * @return the persisted OrderInfo containing the generated orderId, totalPrice, and other persisted fields
     * @throws RuntimeException if the user does not exist or if any referenced menu item is missing or marked off-shelf
     */
    @Transactional
    public OrderInfo createOrder(String username, List<OrderItemRequest> items, String address, String phone) {
        // 检查用户是否存在
        if (userService.getUserByUsername(username) == null) {
            throw new RuntimeException("用户不存在，无法创建订单");
        }

        // 验证订单基本信息的完整性
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("送货地址不能为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("联系电话不能为空");
        }

        OrderInfo order = new OrderInfo();
        order.setOrderId(generateOrderId());
        order.setUsername(username);
        order.setAddress(address);
        order.setPhone(phone);
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode()); // 待支付

        double totalPrice = 0;

        // 预验证所有菜品并计算总价
        for (OrderItemRequest item : items) {
            if (item.getMenuId() == null) {
                throw new RuntimeException("菜品ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("菜品数量必须大于0");
            }

            Menu menu = menuMapper.selectById(item.getMenuId());
            if (menu == null) {
                throw new RuntimeException("菜品不存在: " + item.getMenuId());
            }
            if (menu.getProductLock() == 1) {
                throw new RuntimeException("菜品已下架，无法购买: " + menu.getName());
            }

            totalPrice += menu.getHotPrice() * item.getQuantity();
        }

        // 验证总价
        if (totalPrice <= 0) {
            throw new RuntimeException("订单总价必须大于0");
        }

        order.setTotalPrice(totalPrice);

        try {
            // 插入订单主信息
            orderInfoMapper.insert(order);

            // 插入订单项并更新销量
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
        } catch (Exception e) {
            // 如果订单创建失败，抛出运行时异常让事务回滚
            throw new RuntimeException("订单创建失败: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all orders belonging to the specified user.
     *
     * @param username the username whose orders should be returned
     * @return a list of OrderInfo objects for the user; an empty list if the user has no orders
     */
    public List<OrderInfo> getUserOrders(String username) {
        return orderInfoMapper.findByUsername(username);
    }

    /**
     * Retrieves an order by its order identifier.
     *
     * @param orderid the order identifier (e.g., produced by {@code generateOrderId})
     * @return the matching OrderInfo, or {@code null} if no order with the given id exists
     */
    public OrderInfo getOrderById(String orderid) {
        return orderInfoMapper.findByOrderid(orderid);
    }

    /**
     * Retrieves all order line items for the specified order.
     *
     * @param orderid the order identifier (e.g. the value produced by {@code generateOrderId})
     * @return a list of {@code OrderEntry} records belonging to the order; may be empty if the order has no items
     */
    public List<OrderEntry> getOrderItems(String orderid) {
        return orderEntryMapper.findByOrderid(orderid);
    }

    /**
     * Attempts to cancel a pending order identified by the given order ID.
     *
     * If the order exists and its status is 0 (pending), this method sets the status
     * to 2 (canceled), persists the change, and returns true. If the order does not
     * exist or is not pending, no change is made and the method returns false.
     *
     * @param orderid the order identifier
     * @return true if the order was found and successfully cancelled; false otherwise
     */
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

    /**
     * Update the status of an existing order.
     *
     * If an order with the given `orderid` exists, sets its status to `status` and persists the change.
     *
     * @param orderid the unique identifier of the order to update
     * @param status  the new status code to set on the order
     * @return true if the order was found and updated; false if no such order exists
     */
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

    /**
     * 删除订单（级联删除订单条目）
     *
     * 删除订单时会自动删除相关的订单条目，保证数据一致性
     */
    @Transactional
    public boolean deleteOrder(String orderid) {
        // 检查订单是否存在
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        if (order == null) {
            return false;
        }

        // 检查订单状态，只有特定状态的订单可以删除
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode() &&
            order.getStatus() != OrderStatus.CANCELLED.getCode()) {
            throw new RuntimeException("只有待支付或已取消的订单才能删除");
        }

        try {
            // 删除订单条目（由于数据库外键约束的CASCADE设置，会自动删除）
            // 但为了确保数据一致性，我们手动删除
            List<OrderEntry> orderEntries = orderEntryMapper.findByOrderid(orderid);
            for (OrderEntry entry : orderEntries) {
                orderEntryMapper.deleteById(entry.getId());
            }

            // 删除订单主信息
            return orderInfoMapper.deleteById(order.getId()) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a new order identifier.
     *
     * The identifier is "ORD" followed by the first 12 hexadecimal characters of a UUID
     * (dashes removed) in uppercase, e.g. "ORD3F1A2B4C5D6E".
     *
     * @return a newly generated order ID string
     */
    private String generateOrderId() {
        return "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public static class OrderItemRequest {
        @NotNull(message = "菜品ID不能为空")
        private Long menuId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;

        /**
 * Returns the menu (product) identifier for this order item.
 *
 * @return the menuId
 */
        public Long getMenuId() { return menuId; }
        /**
 * Set the menu item identifier for this OrderItemRequest.
 *
 * @param menuId the menu item's database ID; must not be null
 */
public void setMenuId(Long menuId) { this.menuId = menuId; }

        /**
 * Returns the ordered quantity for this item.
 *
 * @return the number of units requested (minimum 1)
 */
public Integer getQuantity() { return quantity; }
        /**
 * Sets the quantity for this order item.
 *
 * The value must be non-null and at least 1 (enforced by the {@code @NotNull} and {@code @Min(1)} constraints on the field).
 *
 * @param quantity the quantity of the menu item
 */
public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    // ========== 订单状态管理方法 ==========

    /**
     * 支付订单
     *
     * @param orderid 订单ID
     * @return 更新后的订单信息
     * @throws OrderStatusTransitionException 如果状态转换不合法
     */
    @Transactional
    public OrderInfo payOrder(String orderid) throws OrderStatusTransitionException {
        OrderInfo order = getOrderByOrderIdOrThrow(orderid);
        return orderStateMachine.payOrder(order);
    }

    /**
     * 开始配送
     *
     * @param orderid 订单ID
     * @return 更新后的订单信息
     * @throws OrderStatusTransitionException 如果状态转换不合法
     */
    @Transactional
    public OrderInfo startDelivery(String orderid) throws OrderStatusTransitionException {
        OrderInfo order = getOrderByOrderIdOrThrow(orderid);
        return orderStateMachine.startDelivery(order);
    }

    /**
     * 确认收货
     *
     * @param orderid 订单ID
     * @return 更新后的订单信息
     * @throws OrderStatusTransitionException 如果状态转换不合法
     */
    @Transactional
    public OrderInfo confirmDelivery(String orderid) throws OrderStatusTransitionException {
        OrderInfo order = getOrderByOrderIdOrThrow(orderid);
        return orderStateMachine.confirmDelivery(order);
    }

    /**
     * 取消订单
     *
     * @param orderid 订单ID
     * @param reason 取消原因
     * @return 更新后的订单信息
     * @throws OrderStatusTransitionException 如果状态转换不合法
     */
    @Transactional
    public OrderInfo cancelOrder(String orderid, String reason) throws OrderStatusTransitionException {
        OrderInfo order = getOrderByOrderIdOrThrow(orderid);
        return orderStateMachine.cancelOrder(order, reason);
    }

    /**
     * 申请退款
     *
     * @param orderid 订单ID
     * @param reason 退款原因
     * @return 更新后的订单信息
     * @throws OrderStatusTransitionException 如果状态转换不合法
     */
    @Transactional
    public OrderInfo requestRefund(String orderid, String reason) throws OrderStatusTransitionException {
        OrderInfo order = getOrderByOrderIdOrThrow(orderid);
        return orderStateMachine.requestRefund(order, reason);
    }

    /**
     * 完成退款
     *
     * @param orderid 订单ID
     * @param reason 退款完成原因
     * @return 更新后的订单信息
     * @throws OrderStatusTransitionException 如果状态转换不合法
     */
    @Transactional
    public OrderInfo completeRefund(String orderid, String reason) throws OrderStatusTransitionException {
        OrderInfo order = getOrderByOrderIdOrThrow(orderid);
        return orderStateMachine.completeRefund(order, reason);
    }

    // ========== 订单条目管理方法 ==========

    /**
     * 添加订单条目
     *
     * @param orderid 订单ID
     * @param menuId 菜品ID
     * @param quantity 数量
     * @return 是否添加成功
     */
    @Transactional
    public boolean addOrderEntry(String orderid, Long menuId, Integer quantity) {
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有待支付状态可以添加条目
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new RuntimeException("只有待支付状态的订单才能添加商品");
        }

        Menu menu = menuMapper.selectById(menuId);
        if (menu == null || menu.getProductLock() == 1) {
            throw new RuntimeException("菜品不存在或已下架");
        }

        // 检查是否已存在相同菜品
        OrderEntry existingEntry = orderEntryMapper.findByOrderIdAndProductId(orderid, menuId);
        if (existingEntry != null) {
            // 更新数量
            existingEntry.setProductNum(existingEntry.getProductNum() + quantity);
            return orderEntryMapper.updateById(existingEntry) > 0;
        } else {
            // 新增条目
            OrderEntry entry = new OrderEntry();
            entry.setOrderId(orderid);
            entry.setProductId(menuId);
            entry.setProductName(menu.getName());
            entry.setPrice(menu.getHotPrice());
            entry.setProductNum(quantity);
            return orderEntryMapper.insert(entry) > 0;
        }
    }

    /**
     * 更新订单条目数量
     *
     * @param orderid 订单ID
     * @param entryId 条目ID
     * @param newQuantity 新数量
     * @return 是否更新成功
     */
    @Transactional
    public boolean updateOrderEntryQuantity(String orderid, Long entryId, Integer newQuantity) {
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有待支付状态可以修改条目
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new RuntimeException("只有待支付状态的订单才能修改商品数量");
        }

        if (newQuantity <= 0) {
            throw new RuntimeException("商品数量必须大于0");
        }

        OrderEntry entry = orderEntryMapper.selectById(entryId);
        if (entry == null || !entry.getOrderId().equals(orderid)) {
            throw new RuntimeException("订单条目不存在");
        }

        entry.setProductNum(newQuantity);
        return orderEntryMapper.updateById(entry) > 0;
    }

    /**
     * 删除订单条目
     *
     * @param orderid 订单ID
     * @param entryId 条目ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean removeOrderEntry(String orderid, Long entryId) {
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有待支付状态可以删除条目
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new RuntimeException("只有待支付状态的订单才能删除商品");
        }

        OrderEntry entry = orderEntryMapper.selectById(entryId);
        if (entry == null || !entry.getOrderId().equals(orderid)) {
            throw new RuntimeException("订单条目不存在");
        }

        return orderEntryMapper.deleteById(entryId) > 0;
    }

    /**
     * 重新计算订单总额
     *
     * @param orderid 订单ID
     * @return 新的订单总额
     */
    @Transactional
    public double recalculateOrderTotal(String orderid) {
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        List<OrderEntry> entries = orderEntryMapper.findByOrderid(orderid);
        double newTotal = entries.stream()
                .mapToDouble(entry -> entry.getPrice() * entry.getProductNum())
                .sum();

        order.setTotalPrice(newTotal);
        orderInfoMapper.updateById(order);

        return newTotal;
    }

    // ========== 订单查询方法 ==========

    /**
     * 获取订单状态描述
     *
     * @param statusCode 状态代码
     * @return 状态描述
     */
    public String getOrderStatusDescription(int statusCode) {
        return OrderStateMachine.getStatusDescription(statusCode);
    }

    /**
     * 获取订单的可用状态转换
     *
     * @param orderid 订单ID
     * @return 可转换的状态数组
     */
    public OrderStatus[] getAvailableStatusTransitions(String orderid) {
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        if (order == null) {
            return new OrderStatus[0];
        }
        return orderStateMachine.getAvailableTransitions(order);
    }

    /**
     * 检查订单是否为最终状态
     *
     * @param orderid 订单ID
     * @return 如果是最终状态返回true
     */
    public boolean isOrderFinalStatus(String orderid) {
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        return order != null && orderStateMachine.isFinalStatus(order);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 根据订单ID获取订单，如果不存在则抛出异常
     *
     * @param orderid 订单ID
     * @return 订单信息
     * @throws RuntimeException 如果订单不存在
     */
    private OrderInfo getOrderByOrderIdOrThrow(String orderid) {
        OrderInfo order = orderInfoMapper.findByOrderid(orderid);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderid);
        }
        return order;
    }
}