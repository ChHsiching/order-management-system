-- 创建购物车表
CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_product (username, product_id),
    INDEX idx_username (username),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    username VARCHAR(255) NOT NULL,
    total_price DOUBLE NOT NULL COMMENT '订单总金额',
    address VARCHAR(500) NOT NULL COMMENT '收货地址',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    status INT NOT NULL DEFAULT 0 COMMENT '订单状态：0待支付，1已支付，2制作中，3配送中，4已完成，5已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 创建订单详情表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL COMMENT '下单时单价',
    quantity INT NOT NULL COMMENT '购买数量',
    subtotal DOUBLE NOT NULL COMMENT '小计金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- 插入测试数据
INSERT INTO shopping_cart (username, product_id, product_name, price, quantity) VALUES
('newuser123', 1, '巨无霸汉堡', 22.0, 2),
('newuser123', 2, '香辣鸡腿堡', 20.0, 1);

-- 查看表结构
DESCRIBE shopping_cart;
DESCRIBE orders;
DESCRIBE order_items;

-- 查看测试数据
SELECT * FROM shopping_cart;