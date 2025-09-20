package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tech.chhsich.backend.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * Retrieves all OrderInfo records for the given username from the cg_info table.
     *
     * @param username the username to filter orders by
     * @return a list of matching OrderInfo entities; empty if none found
     */
    @Select("SELECT * FROM cg_info WHERE username = #{username}")
    List<OrderInfo> findByUsername(String username);

    /**
     * Retrieves the OrderInfo record with the given order ID.
     *
     * @param orderid the order's unique identifier
     * @return the matching OrderInfo, or {@code null} if no record exists
     */
    @Select("SELECT * FROM cg_info WHERE orderid = #{orderid}")
    OrderInfo findByOrderid(String orderid);

    /**
     * Returns all OrderInfo records for the given username with the specified status.
     *
     * @param username the username to filter by
     * @param status numeric status code to filter by
     * @return a list of matching OrderInfo records; empty if none found
     */
    @Select("SELECT * FROM cg_info WHERE username = #{username} AND status = #{status}")
    List<OrderInfo> findByUsernameAndStatus(@Param("username") String username, @Param("status") Integer status);
    /**
     * Update the status of an OrderInfo record identified by its order ID.
     *
     * @param orderid the order identifier whose status will be changed
     * @param status  the new status value to set
     * @return the number of rows affected (should be 0 or 1)
     */
    @Update("UPDATE cg_info SET status = #{status} WHERE orderid = #{orderid}")
    int updateStatusByOrderid(@Param("orderid") String orderid, @Param("status") Integer status);

    /**
     * Retrieves all OrderInfo records with the given status from the cg_info table.
     *
     * @param status numeric status code of the orders to fetch
     * @return a list of OrderInfo matching the provided status; empty if none found
     */
    @Select("SELECT * FROM cg_info WHERE status = #{status}")
    List<OrderInfo> findByStatus(Integer status);
}