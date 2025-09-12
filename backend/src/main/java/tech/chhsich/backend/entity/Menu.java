package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("createtime")
    private LocalDateTime createTime;

    private String imgpath;
    private String info5;
    private String name;

    @TableField("newstuijian")
    private Integer newStuijian;

    private Double price1;
    private Double price2;

    @TableField("productlock")
    private Integer productLock;

    @TableField("xiaoliang")
    private Integer xiaoliang;

    @TableField("cateid")
    private Long cateid;
}