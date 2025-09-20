package tech.chhsich.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MenuDTO {
    private Long id;
    private LocalDateTime createTime;
    private String imgPath;
    private String info;
    private String name;
    private Integer isRecommend;
    private Double originalPrice;
    private Double hotPrice;
    private Integer productLock;
    private Integer sales;
    private Long categoryId;
}