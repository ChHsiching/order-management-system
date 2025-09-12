package tech.chhsich.backend.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="ltypes", uniqueConstraints = @UniqueConstraint(columnNames = "catename"))
public class Ltypes {//分类信息实体类
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "catelock", nullable = false, length = 255)
    private Integer catelock;//0未删除，1已删除

    @Column(name = "catename", nullable = false, length = 255)
    private String catename;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "productname", nullable = false, length = 255)
    private String productName;
}
