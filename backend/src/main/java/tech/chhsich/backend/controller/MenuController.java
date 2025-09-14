package tech.chhsich.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.pojo.Ltypes;
import tech.chhsich.backend.pojo.Menu;
import tech.chhsich.backend.pojo.ResponseMessage;
import tech.chhsich.backend.service.MenuCategoryService;
import tech.chhsich.backend.service.MenuService;

import java.util.List;

@RestController
@RequestMapping("/menu")
@Validated
public class MenuController {

    @Autowired
    private MenuCategoryService categoryService;
    @Autowired
    private MenuService menuService;

//    获取所有菜单类别接口
    @GetMapping("/categories")
    public ResponseMessage getAllCategories() {
        List<Ltypes> categories = categoryService.getAllCategories();
        return ResponseMessage.success(categories);
    }

//     根据ID获取特定菜单类别接口

    @GetMapping("/categories/{id}")
    public ResponseMessage getCategoryById(@PathVariable Long id) {
        Ltypes category = categoryService.getCategoryById(id);
        if (category != null) {
            return ResponseMessage.success(category);
        } else {
            return ResponseMessage.error("菜单类别不存在");
        }
    }

//     新增菜单类别接口
    @PostMapping("/categories")
    public ResponseMessage addCategory(
            @RequestParam String catename,
            @RequestParam String address,
            @RequestParam String productName) {
        Ltypes category = new Ltypes();
        category.setCatename(catename);
        category.setAddress(address);
        category.setProductName(productName);
        category.setCatelock(0); // 默认未删除
        return categoryService.addCategory(category);
    }

//    更新菜单类别接口
    @PutMapping("/categories/{id}")
    public ResponseMessage updateCategory(
            @PathVariable Long id,
            @RequestParam String catename,
            @RequestParam String address,
            @RequestParam String productName,
            @RequestParam(defaultValue = "0") Integer catelock) {
        Ltypes category = new Ltypes();
        category.setId(id);
        category.setCatename(catename);
        category.setAddress(address);
        category.setProductName(productName);
        category.setCatelock(catelock);
        return categoryService.updateCategory(category);
    }


//     删除菜单类别接口
//     删除指定ID的菜单类别
    @DeleteMapping("/categories/{id}")
    public ResponseMessage deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }


//     获取所有菜品信息接口
    @GetMapping("/items")
    public ResponseMessage getAllMenus() {
        List<Menu> menus = menuService.getAllMenus();
        return ResponseMessage.success(menus);
    }

//     根据类别获取菜品接口
    @GetMapping("/items/category/{categoryId}")
    public ResponseMessage getMenusByCategory(@PathVariable Long categoryId) {
        List<Menu> menus = menuService.getMenusByCategory(categoryId);
        return ResponseMessage.success(menus);
    }


//    根据ID获取特定菜品接口
    @GetMapping("/items/{id}")
    public ResponseMessage getMenuById(@PathVariable Long id) {
        Menu menu = menuService.getMenuById(id);
        if (menu != null) {
            return ResponseMessage.success(menu);
        } else {
            return ResponseMessage.error("菜品不存在");
        }
    }

//     获取推荐菜品接口
    @GetMapping("/items/recommended")
    public ResponseMessage getRecommendedMenus() {
        List<Menu> menus = menuService.getRecommendedMenus();
        return ResponseMessage.success(menus);
    }


//     新增菜品接口
    @PostMapping("/items")
    public ResponseMessage addMenu(
            @RequestParam String name,
            @RequestParam String imgPath,
            @RequestParam String info,
            @RequestParam Double originalPrice,
            @RequestParam Double hotPrice,
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") Integer isRecommend,
            @RequestParam(defaultValue = "0") Integer sales) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setImgPath(imgPath);
        menu.setInfo(info);
        menu.setOriginalPrice(originalPrice);
        menu.setHotPrice(hotPrice);
        menu.setIsRecommend(isRecommend);
        menu.setSales(sales);
        menu.setProductLock(0); // 默认未下架
        menu.setCreateTime(java.time.LocalDateTime.now());
        
        // 设置类别
        Ltypes category = new Ltypes();
        category.setId(categoryId);
        menu.setCategory(category);
        
        return menuService.addMenu(menu);
    }

//     更新菜品信息接口
//     修改指定ID的菜品详细信息
    @PutMapping("/items/{id}")
    public ResponseMessage updateMenu(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String imgPath,
            @RequestParam String info,
            @RequestParam Double originalPrice,
            @RequestParam Double hotPrice,
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") Integer isRecommend,
            @RequestParam(defaultValue = "0") Integer productLock,
            @RequestParam(defaultValue = "0") Integer sales) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setName(name);
        menu.setImgPath(imgPath);
        menu.setInfo(info);
        menu.setOriginalPrice(originalPrice);
        menu.setHotPrice(hotPrice);
        menu.setIsRecommend(isRecommend);
        menu.setProductLock(productLock);
        menu.setSales(sales);
        menu.setCreateTime(java.time.LocalDateTime.now());
        
        // 设置类别
        Ltypes category = new Ltypes();
        category.setId(categoryId);
        menu.setCategory(category);
        
        return menuService.updateMenu(menu);
    }

//    删除菜品接口
//    删除指定ID的菜品
    @DeleteMapping("/items/{id}")
    public ResponseMessage deleteMenu(@PathVariable Long id) {
        return menuService.deleteMenu(id);
    }
}
