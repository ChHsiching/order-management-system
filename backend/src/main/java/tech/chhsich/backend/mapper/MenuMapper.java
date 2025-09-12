package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tech.chhsich.backend.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    // 保持参数名与方法调用一致
    @Select("SELECT * FROM menu WHERE cateid = #{categoryId} AND productlock = #{productLock}")
    List<Menu> findByCategoryIdAndProductLock(@Param("categoryId") Long categoryId, @Param("productLock") Integer productLock);

    // 修正方法参数名拼写，与SQL参数一致
    @Select("SELECT * FROM menu WHERE newstuijian = #{newStuijian} AND productlock = #{productLock}")
    List<Menu> findByNewStuijianAndProductLock(@Param("newStuijian") Integer newStuijian, @Param("productLock") Integer productLock);

    @Select("SELECT * FROM menu WHERE productlock = #{productLock}")
    List<Menu> findByProductLock(Integer productLock);

    @Select("SELECT * FROM menu WHERE cateid = #{categoryId}")
    List<Menu> findByCategoryId(Long categoryId);

    @Update("UPDATE menu SET xiaoliang = xiaoliang + #{quantity} WHERE id = #{menuId}")
    int updateSales(@Param("menuId") Long menuId, @Param("quantity") Integer quantity);
}
