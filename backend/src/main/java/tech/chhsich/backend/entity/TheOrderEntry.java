package tech.chhsich.backend.pojo;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "the_order_entry")
public class TheOrderEntry {//订单条目实体类
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    @Column(name = "price", nullable = false)
    private Double Price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productid", nullable = false)
    private Menu menu;

    @Column(name = "productname", nullable = false, length = 255)
    private String ProductName;

    @Column(name = "productnum")
    private Integer ProductNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderid", nullable = false)
    private Cginfo order;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Double getPrice() {
        return Price;
    }

    public void setPrice(Double price) {
        Price = price;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public Integer getProductNum() {
        return ProductNum;
    }

    public void setProductNum(Integer productNum) {
        ProductNum = productNum;
    }

    public Cginfo getOrder() {
        return order;
    }

    public void setOrder(Cginfo order) {
        this.order = order;
    }
}
