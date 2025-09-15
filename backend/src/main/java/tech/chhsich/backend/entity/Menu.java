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
@Table(name="menu")
public class Menu {//菜单/菜品实体类
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    @Column(name = "createtime",nullable = false)
    private LocalDateTime CreateTime;

    @Column(name = "imgpath",nullable = false,length = 255)
    private String ImgPath;

    @Column(name = "info5", nullable = false, length = 255)
    private String Info;

    @Column(name = "name", length = 255)
    private String Name;

    @Column(name = "newstuijian")
    private Integer IsRecommend;//0不推荐，1推荐

    @Column(name = "price1", nullable = false)
    private Double OriginalPrice;

    @Column(name = "price2", nullable = false)
    private Double HotPrice;

    @Column(name = "productlock")
    private Integer ProductLock;//0未下架，1已下架

    @Column(name = "xiaoliang")
    private Integer Sales;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cateid", nullable = false)
    private Ltypes Category;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public LocalDateTime getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        CreateTime = createTime;
    }

    public String getImgPath() {
        return ImgPath;
    }

    public void setImgPath(String imgPath) {
        ImgPath = imgPath;
    }

    public String getInfo() {
        return Info;
    }

    public void setInfo(String info) {
        Info = info;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Integer getIsRecommend() {
        return IsRecommend;
    }

    public void setIsRecommend(Integer isRecommend) {
        IsRecommend = isRecommend;
    }

    public Double getOriginalPrice() {
        return OriginalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        OriginalPrice = originalPrice;
    }

    public Double getHotPrice() {
        return HotPrice;
    }

    public void setHotPrice(Double hotPrice) {
        HotPrice = hotPrice;
    }

    public Integer getProductLock() {
        return ProductLock;
    }

    public void setProductLock(Integer productLock) {
        ProductLock = productLock;
    }

    public Integer getSales() {
        return Sales;
    }

    public void setSales(Integer sales) {
        Sales = sales;
    }

    public Ltypes getCategory() {
        return Category;
    }

    public void setCategory(Ltypes category) {
        Category = category;
    }
}
