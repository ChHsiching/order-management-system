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
@TableName("shopping_cart")
public class ShoppingCart {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("productid")
    private Long productId;

    @TableField("productname")
    private String productName;

    @TableField("price")
    private Double price;

    @TableField("quantity")
    private Integer quantity;

    @TableField("createtime")
    private LocalDateTime createTime;

    @TableField("updatetime")
    private LocalDateTime updateTime;
}