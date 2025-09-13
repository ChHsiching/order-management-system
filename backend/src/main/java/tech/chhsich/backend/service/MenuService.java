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
    private MenuMapper menuMapper; // 只保留MyBatis的mapper依赖

    public List<Menu> getMenusByCategory(Long categoryId) {
        return menuMapper.findByCategoryIdAndProductLock(categoryId, PRODUCT_STATUS_ACTIVE);
    }

    public List<Menu> getRecommendedMenus() {
        return menuMapper.findByNewStuijianAndProductLock(PRODUCT_STATUS_RECOMMENDED, PRODUCT_STATUS_ACTIVE);
    }

    public List<Menu> getAllAvailableMenus() {
        return menuMapper.findByProductLock(PRODUCT_STATUS_ACTIVE);
    }
    // 修改为使用mapper获取菜单，移除JPA的repository依赖
    public Menu getMenuById(Long id) {
        return menuMapper.selectById(id); // 使用MyBatis-Plus的BaseMapper方法
    }

    // 新增方法：更新销量
    public boolean updateSales(Long menuId, Integer quantity) {
        return menuMapper.updateSales(menuId, quantity) > 0;
    }
}
