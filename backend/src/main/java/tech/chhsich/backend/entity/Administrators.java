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
@TableName("administrators")
public class Administrators {//管理员用户实体类
    @TableId(type = IdType.INPUT)
    private String username;

    @TableField("createtime")
    private LocalDateTime createTime;

    @TableField("email")
    private String email;

    @TableField("password")
    private String password;

    @TableField("phone")
    private String phone;

    @TableField("qq")
    private String qq;

    @TableField("role")
    private Integer Role;//0会员，1管理员，2接单员
}
