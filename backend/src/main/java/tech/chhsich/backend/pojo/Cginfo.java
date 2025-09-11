package tech.chhsich.backend.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.extension.PreInterruptCallback;

import java.time.LocalDateTime;

@Data
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
    private Long id;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "createtime", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "orderid", nullable = false, length = 255)
    private String orderId;

    @Column(name = "phone", nullable = false, length = 255)
    private String phone;

    @Column(name = "status")
    private Integer status;//0待受理，1已受理

    @Column(name = "totalprice", nullable = false)
    private Double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private Administrators user;
}
