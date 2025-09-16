package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("cg_info")
public class OrderInfo {//订单信息实体类
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("address")
    private String address;

    @TableField("createtime")
    private LocalDateTime createTime;

    @TableField("orderid")
    private String orderId;

    @TableField("phone")
    private String phone;

    @TableField("status")
    private Integer status;//0待受理，1已受理

    @TableField("totalprice")
    private Double totalPrice;

    @TableField("username")
    private String username;
}
