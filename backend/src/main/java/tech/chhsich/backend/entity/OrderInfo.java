package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cg_info")
public class OrderInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String address;

    @TableField("createtime")
    private LocalDateTime createTime;

    private String orderid;
    private String phone;
    private Integer status;

    @TableField("totalprice")
    private Double totalPrice;

    private String username;
}