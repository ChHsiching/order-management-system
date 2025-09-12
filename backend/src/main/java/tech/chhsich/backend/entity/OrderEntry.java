package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("the_order_entry")
public class OrderEntry {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Double price;

    @TableField("productid")
    private Long productId;

    @TableField("productname")
    private String productName;

    @TableField("productnum")
    private Integer productNum;

    @TableField("orderid")
    private String orderId;
}