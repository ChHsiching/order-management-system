package tech.chhsich.backend.pojo;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data//生成常用方法
@NoArgsConstructor//生成无参构造函数
@AllArgsConstructor//生成全参构造函数
@Entity//标记实体类
@Table(name="administrators")//指定表名,指定映射哪个表
public class Administrators {//管理员用户实体类
    @Id//主键
    @Column(name="username",nullable = false,length = 155)
    private String username;

    @Column(name="createtime")
    private LocalDateTime createTime;

    @Column(name = "email",nullable = false,length = 255)
    private String email;

    @Column(name = "password",nullable = false,length = 255)
    private String password;

    @Column(name = "nickname",nullable = false,length = 255)
    private String phone;

    @Column(name = "qq",nullable = false,length = 255)
    private String qq;

    @Column(name = "role")
    private Integer role;//0会员，1管理员，2接单员
}
