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
@TableName("menu")
public class Menu {//菜单/菜品实体类
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("createtime")
    private LocalDateTime createTime;

    @TableField("imgpath")
    private String imgPath;

    @TableField("info5")
    private String info;

    @TableField("name")
    private String name;

    @TableField("newstuijian")
    private Integer isRecommend;//0不推荐，1推荐

    @TableField("price1")
    private Double originalPrice;

    @TableField("price2")
    private Double hotPrice;

    @TableField("productlock")
    private Integer productLock;//0未下架，1已下架

    @TableField("xiaoliang")
    private Integer sales;

    @TableField("cateid")
    private Long categoryId;
}
