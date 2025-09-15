package tech.chhsich.backend.pojo;

import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="ltypes", uniqueConstraints = @UniqueConstraint(columnNames = "catename"))
public class Ltypes {//分类信息实体类
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    @Column(name = "catelock", nullable = false, length = 255)
    private Integer Catelock;//0未删除，1已删除

    @Column(name = "catename", nullable = false, length = 255)
    private String Catename;

    @Column(name = "address", nullable = false, length = 255)
    private String Address;

    @Column(name = "productname", nullable = false, length = 255)
    private String ProductName;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Integer getCatelock() {
        return Catelock;
    }

    public void setCatelock(Integer catelock) {
        Catelock = catelock;
    }

    public String getCatename() {
        return Catename;
    }

    public void setCatename(String catename) {
        Catename = catename;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }
}
