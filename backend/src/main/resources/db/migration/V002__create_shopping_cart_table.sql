-- 创建购物车表
CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL COMMENT '用户名',
    productid BIGINT NOT NULL COMMENT '商品ID',
    productname VARCHAR(255) NOT NULL COMMENT '商品名称',
    price DOUBLE NOT NULL COMMENT '商品价格',
    quantity INT NOT NULL DEFAULT 1 COMMENT '商品数量',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_productid (productid),
    INDEX idx_username_productid (username, productid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';