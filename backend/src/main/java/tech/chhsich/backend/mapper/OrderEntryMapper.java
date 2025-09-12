package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.chhsich.backend.entity.OrderEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import java.util.List;

@Mapper
public interface OrderEntryMapper extends BaseMapper<OrderEntry> {

    @Select("SELECT * FROM the_order_entry WHERE orderid = #{orderid}")
    List<OrderEntry> findByOrderid(String orderid);

    @Delete("DELETE FROM the_order_entry WHERE orderid = #{orderid}")
    void deleteByOrderid(String orderid);
}