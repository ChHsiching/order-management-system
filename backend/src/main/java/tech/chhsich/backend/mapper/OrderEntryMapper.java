package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.chhsich.backend.entity.OrderEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import java.util.List;

@Mapper
public interface OrderEntryMapper extends BaseMapper<OrderEntry> {

    /**
     * Retrieves all OrderEntry records that have the specified order ID.
     *
     * @param orderid the order identifier to match in the_order_entry table
     * @return a list of matching OrderEntry objects; an empty list if no matches
     */
    @Select("SELECT * FROM the_order_entry WHERE orderid = #{orderid}")
    List<OrderEntry> findByOrderid(String orderid);

    /**
     * Delete all OrderEntry rows with the given order ID.
     *
     * @param orderid the order identifier whose entries should be removed
     */
    @Delete("DELETE FROM the_order_entry WHERE orderid = #{orderid}")
    void deleteByOrderid(String orderid);
}