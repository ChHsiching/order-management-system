package tech.chhsich.backend.pojo;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Data//生成常用方法
@Getter
@Setter
@ToString
@NoArgsConstructor//生成无参构造函数
@AllArgsConstructor//生成全参构造函数
@Entity//标记实体类
@Table(name="administrators")//指定表名,指定映射哪个表
public class Administrators {//管理员用户实体类
    @Id//主键
    @Column(name="username",nullable = false,length = 155)
    private String Username;

    @Column(name="createtime")
    private LocalDateTime CreateTime;

    @Column(name = "email",nullable = false,length = 255)
    private String Email;

    @Column(name = "password",nullable = false,length = 255)
    private String Password;

    @Column(name = "phone",nullable = false,length = 255)
    private String Phone;

    @Column(name = "qq",nullable = false,length = 255)
    private String Qq;

    @Column(name = "role")
    private Integer Role;//0会员，1管理员，2接单员

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public LocalDateTime getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        CreateTime = createTime;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getQq() {
        return Qq;
    }

    public void setQq(String qq) {
        Qq = qq;
    }

    public Integer getRole() {
        return Role;
    }

    public void setRole(Integer role) {
        Role = role;
    }
}
