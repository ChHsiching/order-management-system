package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 记录用户操作日志
 *
 * @author chhsich
 * @since 2025-09-19
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private Integer role;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 执行结果
     */
    private String result;

    /**
     * 执行时间(毫秒)
     */
    private Long executionTime;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 操作状态 (0:失败, 1:成功)
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}