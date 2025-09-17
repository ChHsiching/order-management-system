package tech.chhsich.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.mapper.MenuMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MenuService {
    // 定义状态常量
    private static final Integer PRODUCT_STATUS_ACTIVE = 0;
    private static final Integer PRODUCT_STATUS_INACTIVE = 1;
    private static final Integer PRODUCT_STATUS_RECOMMENDED = 1;
    private static final Integer PRODUCT_STATUS_NOT_RECOMMENDED = 0;

    private final MenuMapper menuMapper;

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
        return menuMapper.selectById(id);
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

    /**
     * 添加新菜品
     */
    public boolean addMenu(Menu menu) {
        menu.setCreateTime(LocalDateTime.now());
        menu.setProductLock(PRODUCT_STATUS_ACTIVE);
        menu.setSales(0);
        return menuMapper.insert(menu) > 0;
    }

    /**
     * 更新菜品信息
     */
    public boolean updateMenu(Menu menu) {
        Menu existingMenu = menuMapper.selectById(menu.getId());
        if (existingMenu == null) {
            return false;
        }

        // 保持创建时间不变
        menu.setCreateTime(existingMenu.getCreateTime());
        return menuMapper.updateById(menu) > 0;
    }

    /**
     * 删除菜品（逻辑删除，下架处理）
     */
    public boolean deleteMenu(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            return false;
        }

        menu.setProductLock(PRODUCT_STATUS_INACTIVE);
        return menuMapper.updateById(menu) > 0;
    }

    /**
     * 设置推荐状态
     */
    public boolean setRecommendStatus(Long id, Integer recommend) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            return false;
        }

        menu.setIsRecommend(recommend);
        return menuMapper.updateById(menu) > 0;
    }

    /**
     * 设置菜品状态（上架/下架）
     */
    public boolean setMenuStatus(Long id, Integer status) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            return false;
        }

        menu.setProductLock(status);
        return menuMapper.updateById(menu) > 0;
    }

    /**
     * 根据类别ID获取所有菜品（包括已下架的）
     */
    public List<Menu> getAllMenusByCategory(Long categoryId) {
        return menuMapper.findByCategoryId(categoryId);
    }

    /**
     * 获取所有菜品（包括已下架的）
     */
    public List<Menu> getAllMenus() {
        return menuMapper.selectList(null);
    }

    /**
     * 检查菜品是否存在
     */
    public boolean menuExists(Long id) {
        return menuMapper.selectById(id) != null;
    }

    /**
     * 批量更新菜品类别
     */
    public boolean batchUpdateCategory(List<Long> menuIds, Long newCategoryId) {
        for (Long menuId : menuIds) {
            Menu menu = menuMapper.selectById(menuId);
            if (menu != null) {
                menu.setCategoryId(newCategoryId);
                if (menuMapper.updateById(menu) <= 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
