package tech.chhsich.backend.dto;

import jakarta.validation.constraints.*;

public class ExtendedUserRegistrationRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "QQ号不能为空")
    @Pattern(regexp = "^[1-9]\\d{4,11}$", message = "QQ号格式不正确")
    private String qq;

    // 扩展字段 - 用于适配前端注册表单
    private String name;

    @Pattern(regexp = "^(男|女)$", message = "性别只能是男或女")
    private String gender = "男";

    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 120, message = "年龄不能超过120")
    private Integer age;

    private String address;

    // 默认构造函数
    public ExtendedUserRegistrationRequest() {}

    // 基础信息构造函数
    public ExtendedUserRegistrationRequest(String username, String password, String email, String phone, String qq) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.qq = qq;
    }

    // Getter 和 Setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 验证扩展字段是否有效
     * 如果扩展字段为空，将使用默认值
     */
    public void validateAndSetDefaults() {
        // 如果姓名为空，使用用户名作为姓名
        if (this.name == null || this.name.trim().isEmpty()) {
            this.name = this.username;
        }

        // 如果地址为空，使用默认地址
        if (this.address == null || this.address.trim().isEmpty()) {
            this.address = "默认地址";
        }

        // 如果年龄为空，设置为默认值18
        if (this.age == null) {
            this.age = 18;
        }
    }
}