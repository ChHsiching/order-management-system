package tech.chhsich.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ltypes")
public class Ltype {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer catelock;
    private String catename;
    private String address;
    private String productname;
}