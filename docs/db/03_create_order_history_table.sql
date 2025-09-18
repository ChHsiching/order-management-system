-- 创建订单历史记录表
-- 用于记录订单状态变更的完整历史，确保可追溯性

CREATE TABLE IF NOT EXISTS order_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL COMMENT '订单ID',
    from_status INT NOT NULL COMMENT '原状态代码',
    to_status INT NOT NULL COMMENT '新状态代码',
    from_status_desc VARCHAR(50) COMMENT '原状态描述',
    to_status_desc VARCHAR(50) COMMENT '新状态描述',
    change_reason VARCHAR(255) COMMENT '状态变更原因',
    operator VARCHAR(50) COMMENT '操作人员',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    remarks TEXT COMMENT '备注',
    INDEX idx_order_id (order_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_operator (operator),
    INDEX idx_status (from_status, to_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单历史记录表';

-- 添加外键约束（可选，确保数据完整性）
ALTER TABLE order_history
ADD CONSTRAINT fk_order_history_order
FOREIGN KEY (order_id) REFERENCES cg_info (orderid)
ON DELETE CASCADE
ON UPDATE CASCADE;