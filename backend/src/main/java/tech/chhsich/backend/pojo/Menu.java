package tech.chhsich.backend.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="menu")
public class Menu {//菜单/菜品实体类
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "createtime",nullable = false)
    private LocalDateTime createTime;

    @Column(name = "imgpath",nullable = false,length = 255)
    private String imgPath;

    @Column(name = "info5", nullable = false, length = 255)
    private String info;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "newstuijian")
    private Integer isRecommend;//0不推荐，1推荐

    @Column(name = "price1", nullable = false)
    private Double originalPrice;

    @Column(name = "price2", nullable = false)
    private Double hotPrice;

    @Column(name = "productlock")
    private Integer productLock;//0未下架，1已下架

    @Column(name = "xiaoliang")
    private Integer sales;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cateid", nullable = false)
    private Ltypes category;
}
