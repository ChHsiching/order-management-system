package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.mapper.MenuMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MenuService {
    // 定义状态常量
    private static final Integer PRODUCT_STATUS_ACTIVE = 0;
    private static final Integer PRODUCT_STATUS_RECOMMENDED = 1;

    private final MenuMapper menuMapper; /**
     * Creates a MenuService backed by the given MyBatis mapper.
     *
     * Uses constructor injection to initialize the service's MenuMapper dependency.
     */

    public MenuService(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * Retrieve active menus belonging to the given category.
     *
     * @param categoryId the ID of the category to filter menus by
     * @return a list of menus in the specified category that are marked active
     */
    public List<Menu> getMenusByCategory(Long categoryId) {
        return menuMapper.findByCategoryIdAndProductLock(categoryId, PRODUCT_STATUS_ACTIVE);
    }

    /**
     * Retrieves menus that are marked as recommended and currently active.
     *
     * <p>Results are filtered by the service's recommendation and active-status constants.
     *
     * @return a list of recommended, active Menu objects; an empty list if none are found
     */
    public List<Menu> getRecommendedMenus() {
        return menuMapper.findByNewStuijianAndProductLock(PRODUCT_STATUS_RECOMMENDED, PRODUCT_STATUS_ACTIVE);
    }

    /**
     * Retrieves all menus that are currently available (active).
     *
     * This returns a list of Menu entities whose product lock/status equals {@code PRODUCT_STATUS_ACTIVE}.
     *
     * @return a list of active Menu objects; never null (may be empty)
     */
    public List<Menu> getAllAvailableMenus() {
        return menuMapper.findByProductLock(PRODUCT_STATUS_ACTIVE);
    }
    /**
     * Retrieve a Menu by its primary key.
     *
     * @param id the Menu's id
     * @return the Menu with the given id, or null if not found
     */
    public Menu getMenuById(Long id) {
        return menuMapper.selectById(id); // 使用MyBatis-Plus的BaseMapper方法
    }

    /**
     * Incrementally updates the sales count for the menu identified by {@code menuId}.
     *
     * @param menuId   the ID of the menu whose sales to update
     * @param quantity the amount to add to the menu's sales total
     * @return {@code true} if the update affected at least one row (update succeeded), {@code false} otherwise
     */
    public boolean updateSales(Long menuId, Integer quantity) {
        return menuMapper.updateSales(menuId, quantity) > 0;
    }
}
