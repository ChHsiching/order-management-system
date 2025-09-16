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

    /**
     * Finds all Menu records that match the given category ID and product lock status.
     *
     * @param categoryId the category ID to filter menus by
     * @param productLock the product lock value to filter menus by
     * @return a list of Menu entities matching the specified category and product lock
     */
    @Select("SELECT * FROM menu WHERE cateid = #{categoryId} AND productlock = #{productLock}")
    List<Menu> findByCategoryIdAndProductLock(@Param("categoryId") Long categoryId, @Param("productLock") Integer productLock);

    /**
     * Retrieves menus filtered by the `newstuijian` flag and product lock status.
     *
     * @param newStuijian  value of the `newstuijian` column to match
     * @param productLock  value of the `productlock` column to match
     * @return a list of Menu records matching both criteria (empty if none)
     */
    @Select("SELECT * FROM menu WHERE newstuijian = #{newStuijian} AND productlock = #{productLock}")
    List<Menu> findByNewStuijianAndProductLock(@Param("newStuijian") Integer newStuijian, @Param("productLock") Integer productLock);

    /**
     * Retrieves all Menu rows where the `productlock` column matches the given value.
     *
     * @param productLock the value to match against the `productlock` column
     * @return a list of Menu entries with the specified productLock; empty list if none match
     */
    @Select("SELECT * FROM menu WHERE productlock = #{productLock}")
    List<Menu> findByProductLock(Integer productLock);

    /**
     * Retrieves all Menu records with the given category ID.
     *
     * @param categoryId the category ID to match (maps to the `menu.cateid` column)
     * @return a list of matching Menu entities; empty if no rows match
     */
    @Select("SELECT * FROM menu WHERE cateid = #{categoryId}")
    List<Menu> findByCategoryId(Long categoryId);

    /**
     * Increment the sales count ("xiaoliang") of a menu row by a given quantity.
     *
     * @param menuId   the id of the menu row to update
     * @param quantity the amount to add to the current sales count
     * @return the number of rows affected (typically 0 if no row with the id exists, or 1 on success)
     */
    @Update("UPDATE menu SET xiaoliang = xiaoliang + #{quantity} WHERE id = #{menuId}")
    int updateSales(@Param("menuId") Long menuId, @Param("quantity") Integer quantity);
}
