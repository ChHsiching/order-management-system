package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("the_order_entry")
public class TheOrderEntry {//订单条目实体类
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("price")
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
