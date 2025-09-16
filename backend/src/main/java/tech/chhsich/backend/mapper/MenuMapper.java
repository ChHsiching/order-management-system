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
     * Retrieve menus that belong to a specific category and have the given product lock value.
     *
     * @param categoryId the category id to filter by (maps to the `cateid` column)
     * @param productLock the product lock value to filter by (maps to the `productlock` column)
     * @return a list of Menu objects matching the specified category and product lock (empty if none)
     */
    @Select("SELECT * FROM menu WHERE cateid = #{categoryId} AND productlock = #{productLock}")
    List<Menu> findByCategoryIdAndProductLock(@Param("categoryId") Long categoryId, @Param("productLock") Integer productLock);

    /**
     * Retrieve menus filtered by the `newstuijian` flag and `productlock` value.
     *
     * @param newStuijian  value of the `newstuijian` column to match
     * @param productLock  value of the `productlock` column to match
     * @return list of Menu records matching both criteria
     */
    @Select("SELECT * FROM menu WHERE newstuijian = #{newStuijian} AND productlock = #{productLock}")
    List<Menu> findByNewStuijianAndProductLock(@Param("newStuijian") Integer newStuijian, @Param("productLock") Integer productLock);

    /**
     * Retrieves all Menu records filtered by the product lock flag.
     *
     * @param productLock integer flag stored in the `productlock` column (e.g., 0 or 1) used to select matching menus
     * @return a list of Menu entities whose `productlock` value equals the given flag; empty if none match
     */
    @Select("SELECT * FROM menu WHERE productlock = #{productLock}")
    List<Menu> findByProductLock(Integer productLock);

    /**
     * Retrieves all Menu records belonging to the given category.
     *
     * @param categoryId the category id (menu.cateid) to filter by
     * @return a list of Menu objects for the specified category; empty if none found
     */
    @Select("SELECT * FROM menu WHERE cateid = #{categoryId}")
    List<Menu> findByCategoryId(Long categoryId);

    /**
     * Increment the sales count (`xiaoliang`) for a menu row.
     *
     * Adds `quantity` to the `xiaoliang` column of the menu record identified by `menuId`.
     * `quantity` may be negative to decrement the sales count.
     *
     * @param menuId the id of the menu record to update
     * @param quantity the amount to add to `xiaoliang` (negative to subtract)
     * @return the number of rows affected (typically 0 or 1)
     */
    @Update("UPDATE menu SET xiaoliang = xiaoliang + #{quantity} WHERE id = #{menuId}")
    int updateSales(@Param("menuId") Long menuId, @Param("quantity") Integer quantity);
}
