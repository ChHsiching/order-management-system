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
     * Retrieves all OrderEntry records with the given order ID from the `the_order_entry` table.
     *
     * @param orderid the order identifier to filter entries by
     * @return a list of matching OrderEntry objects; empty if no matches are found
     */
    @Select("SELECT * FROM the_order_entry WHERE orderid = #{orderid}")
    List<OrderEntry> findByOrderid(String orderid);

    /**
     * Delete all OrderEntry rows matching the given order ID.
     *
     * @param orderid the order identifier to match against the `orderid` column
     */
    @Delete("DELETE FROM the_order_entry WHERE orderid = #{orderid}")
    void deleteByOrderid(String orderid);
}