package tech.chhsich.backend.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cg_info",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "phone"),
                @UniqueConstraint(columnNames = "orderid")
        })
public class Cginfo {//订单信息实体类
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    @Column(name = "address", nullable = false, length = 255)
    private String Address;

    @Column(name = "createtime", nullable = false)
    private LocalDateTime CreateTime;

    @Column(name = "orderid", nullable = false, length = 255)
    private String OrderId;

    @Column(name = "phone", nullable = false, length = 255)
    private String Phone;

    @Column(name = "status")
    private Integer Status;//0待受理，1已受理

    @Column(name = "totalprice", nullable = false)
    private Double TotalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private Administrators user;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public LocalDateTime getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        CreateTime = createTime;
    }

    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String orderId) {
        OrderId = orderId;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public Integer getStatus() {
        return Status;
    }

    public void setStatus(Integer status) {
        Status = status;
    }

    public Double getTotalPrice() {
        return TotalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        TotalPrice = totalPrice;
    }

    public Administrators getUser() {
        return user;
    }

    public void setUser(Administrators user) {
        this.user = user;
    }
}
