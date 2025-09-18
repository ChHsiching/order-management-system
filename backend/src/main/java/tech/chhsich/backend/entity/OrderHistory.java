package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单历史记录实体类
 *
 * 记录订单状态变更的完整历史，包括状态转换时间、操作原因、操作人员等信息。
 * 基于项目设计文档要求，确保订单状态变更的可追溯性。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@Data
@TableName("order_history")
public class OrderHistory {

    /**
     * 历史记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 原状态代码
     */
    private Integer fromStatus;

    /**
     * 新状态代码
     */
    private Integer toStatus;

    /**
     * 原状态描述
     */
    private String fromStatusDesc;

    /**
     * 新状态描述
     */
    private String toStatusDesc;

    /**
     * 状态变更原因
     */
    private String changeReason;

    /**
     * 操作人员
     */
    private String operator;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 备注
     */
    private String remarks;
}