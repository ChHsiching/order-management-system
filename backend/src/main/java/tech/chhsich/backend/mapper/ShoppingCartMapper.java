package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tech.chhsich.backend.entity.ShoppingCart;

import java.util.List;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {

    @Select("SELECT * FROM shopping_cart WHERE username = #{username}")
    List<ShoppingCart> findByUsername(@Param("username") String username);

    @Select("SELECT * FROM shopping_cart WHERE username = #{username} AND productid = #{productId}")
    ShoppingCart findByUsernameAndProductId(@Param("username") String username, @Param("productId") Long productId);

    @Update("UPDATE shopping_cart SET quantity = quantity + #{quantity}, updatetime = NOW() WHERE username = #{username} AND productid = #{productId}")
    int updateQuantity(@Param("username") String username, @Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Select("DELETE FROM shopping_cart WHERE username = #{username}")
    int deleteByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM shopping_cart WHERE username = #{username}")
    int countByUsername(@Param("username") String username);
}