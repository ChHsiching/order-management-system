package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MenuService {
    // 定义状态常量
    private static final Integer PRODUCT_STATUS_ACTIVE = 0;
    private static final Integer PRODUCT_STATUS_DELETED = 1;
    private static final Integer PRODUCT_STATUS_RECOMMENDED = 1;

    @Autowired
    private MenuMapper menuMapper; /**
     * Returns all active Menu items in the given category.
     *
     * Filters menus by the provided category ID and only includes items with the active product status.
     *
     * @param categoryId the ID of the category to retrieve menus for
     * @return a list of active Menu objects for the category (empty if none found)
     */

    public List<Menu> getMenusByCategory(Long categoryId) {
        return menuMapper.findByCategoryIdAndProductLock(categoryId, PRODUCT_STATUS_ACTIVE);
    }

    /**
     * Retrieves menus marked as recommended and currently active.
     *
     * <p>Returns a list of Menu entities that are flagged as "recommended" and have an active product status.</p>
     *
     * @return a list of recommended, active Menu objects (empty if none found)
     */
    public List<Menu> getRecommendedMenus() {
        return menuMapper.findByNewStuijianAndProductLock(PRODUCT_STATUS_RECOMMENDED, PRODUCT_STATUS_ACTIVE);
    }

    /**
     * Returns all menus that are marked as active (product lock == PRODUCT_STATUS_ACTIVE).
     *
     * @return a list of Menu objects whose product lock indicates active availability; never null
     */
    public List<Menu> getAllAvailableMenus() {
        return menuMapper.findByProductLock(PRODUCT_STATUS_ACTIVE);
    }
    /**
     * Retrieve a Menu by its primary key.
     *
     * @param id the Menu primary key (may be null)
     * @return the Menu with the given id, or {@code null} if no such record exists
     */
    public Menu getMenuById(Long id) {
        return menuMapper.selectById(id); // 使用MyBatis-Plus的BaseMapper方法
    }

    /**
     * Increment the sales count for a menu item in the database.
     *
     * Updates the stored sales value for the menu identified by {@code menuId} by the given {@code quantity}.
     *
     * @param menuId  the ID of the menu item to update
     * @param quantity the amount to add to the menu's sales (may be negative to decrement)
     * @return {@code true} if the update affected at least one row, {@code false} otherwise
     */
    public boolean updateSales(Long menuId, Integer quantity) {
        return menuMapper.updateSales(menuId, quantity) > 0;
    }
}
