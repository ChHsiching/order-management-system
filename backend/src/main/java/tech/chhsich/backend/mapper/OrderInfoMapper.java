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

    @Select("SELECT * FROM cg_info WHERE username = #{username}")
    List<OrderInfo> findByUsername(String username);

    @Select("SELECT * FROM cg_info WHERE orderid = #{orderid}")
    OrderInfo findByOrderid(String orderid);

    @Select("SELECT * FROM cg_info WHERE username = #{username} AND status = #{status}")
    List<OrderInfo> findByUsernameAndStatus(@Param("username") String username, @Param("status") Integer status);
    @Update("UPDATE cg_info SET status = #{status} WHERE orderid = #{orderid}")
    int updateStatusByOrderid(@Param("orderid") String orderid, @Param("status") Integer status);

    @Select("SELECT * FROM cg_info WHERE status = #{status}")
    List<OrderInfo> findByStatus(Integer status);
}