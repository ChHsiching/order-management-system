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
     * Retrieves all OrderInfo records from the `cg_info` table for the given username.
     *
     * @param username the username to filter orders by
     * @return a list of OrderInfo matching the specified username; empty if none found
     */
    @Select("SELECT * FROM cg_info WHERE username = #{username}")
    List<OrderInfo> findByUsername(String username);

    /**
     * Retrieves the OrderInfo record with the given order ID from the cg_info table.
     *
     * @param orderid the order identifier to search for
     * @return the matching OrderInfo, or null if none is found
     */
    @Select("SELECT * FROM cg_info WHERE orderid = #{orderid}")
    OrderInfo findByOrderid(String orderid);

    /**
     * Retrieves all OrderInfo records for a given username with the specified status.
     *
     * @param username the user's username to filter orders by
     * @param status   the order status code to filter by
     * @return a list of OrderInfo matching the username and status (empty if none)
     */
    @Select("SELECT * FROM cg_info WHERE username = #{username} AND status = #{status}")
    List<OrderInfo> findByUsernameAndStatus(@Param("username") String username, @Param("status") Integer status);
    /**
     * Update the status of the order identified by the given order ID.
     *
     * @param orderid the order ID whose record's status will be updated
     * @param status  the new status value to set
     * @return the number of rows affected (should be 0 or 1)
     */
    @Update("UPDATE cg_info SET status = #{status} WHERE orderid = #{orderid}")
    int updateStatusByOrderid(@Param("orderid") String orderid, @Param("status") Integer status);

    /**
     * Retrieves all OrderInfo records with the given status.
     *
     * @param status numeric status code to filter orders by
     * @return list of OrderInfo matching the specified status; empty list if none found
     */
    @Select("SELECT * FROM cg_info WHERE status = #{status}")
    List<OrderInfo> findByStatus(Integer status);
}