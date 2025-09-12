package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("administrators")
public class Administrator {
    @TableId
    @Schema(description = "用户名", example = "admin123")
    private String username;

    @TableField("createtime")
    @Schema(description = "创建时间", example = "2023-01-01T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "密码", example = "password123")
    private String password;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "QQ号", example = "123456789")
    private String qq;

    @Schema(description = "角色权限", example = "0")
    private Integer role;
}