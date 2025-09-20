package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ltypes")
public class Ltype {//分类信息实体类
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("catelock")
    private Integer cateLock;//0未删除，1已删除

    @TableField("catename")
    private String cateName;

    @TableField("address")
    private String address;

    @TableField("productname")
    private String productName;
}
