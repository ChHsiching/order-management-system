package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MenuService {

    @Autowired
    private MenuMapper menuMapper; // 只保留MyBatis的mapper依赖

    // 修正方法名与mapper接口一致
    public List<Menu> getMenusByCategory(Long categoryId) {
        return menuMapper.findByCategoryIdAndProductLock(categoryId, 0); // 0表示未删除的商品
    }

    // 修正方法名与mapper接口一致
    public List<Menu> getRecommendedMenus() {
        return menuMapper.findByNewStuijianAndProductLock(1, 0); // 1表示推荐商品
    }

    public List<Menu> getAllAvailableMenus() {
        return menuMapper.findByProductLock(0); // 0表示未删除的商品
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
