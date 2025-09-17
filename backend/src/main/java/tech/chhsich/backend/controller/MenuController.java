package tech.chhsich.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.entity.Ltype;
import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.entity.ResponseMessage;
import tech.chhsich.backend.service.CategoryService;
import tech.chhsich.backend.service.MenuService;

import java.util.List;

@RestController
@RequestMapping("/admin/menu")
@Validated
@Tag(name = "后台菜单管理", description = "后台菜单类别和菜品管理接口")
public class MenuController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MenuService menuService;

    // 菜单类别管理
    @GetMapping("/categories")
    @Operation(summary = "获取所有菜单类别", description = "获取系统中所有的菜单类别信息")
    public ResponseMessage getAllCategories() {
        try {
            List<Ltype> categories = categoryService.getAllCategories();
            return ResponseMessage.success(categories);
        } catch (Exception e) {
            return ResponseMessage.error("获取菜单类别失败: " + e.getMessage());
        }
    }

    @PostMapping("/categories")
    @Operation(summary = "添加菜单类别", description = "添加新的菜单类别")
    public ResponseMessage addCategory(@RequestBody Ltype category) {
        try {
            boolean success = categoryService.addCategory(category);
            if (success) {
                return ResponseMessage.success("菜单类别添加成功");
            } else {
                return ResponseMessage.error("菜单类别添加失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("添加菜单类别失败: " + e.getMessage());
        }
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "更新菜单类别", description = "更新指定的菜单类别信息")
    public ResponseMessage updateCategory(
            @PathVariable @Parameter(description = "类别ID") Long id,
            @RequestBody Ltype category) {
        try {
            category.setId(id);
            boolean success = categoryService.updateCategory(category);
            if (success) {
                return ResponseMessage.success("菜单类别更新成功");
            } else {
                return ResponseMessage.error("菜单类别更新失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("更新菜单类别失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "删除菜单类别", description = "删除指定的菜单类别（逻辑删除）")
    public ResponseMessage deleteCategory(
            @PathVariable @Parameter(description = "类别ID") Long id) {
        try {
            boolean success = categoryService.deleteCategory(id);
            if (success) {
                return ResponseMessage.success("菜单类别删除成功");
            } else {
                return ResponseMessage.error("菜单类别删除失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("删除菜单类别失败: " + e.getMessage());
        }
    }

    // 菜品管理
    @GetMapping("/items")
    @Operation(summary = "获取所有菜品", description = "获取系统中所有的菜品信息")
    public ResponseMessage getAllMenus() {
        try {
            List<Menu> menus = menuService.getAllAvailableMenus();
            return ResponseMessage.success(menus);
        } catch (Exception e) {
            return ResponseMessage.error("获取菜品列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "获取菜品详情", description = "根据ID获取指定菜品的详细信息")
    public ResponseMessage getMenuById(
            @PathVariable @Parameter(description = "菜品ID") Long id) {
        try {
            Menu menu = menuService.getMenuById(id);
            if (menu != null) {
                return ResponseMessage.success(menu);
            } else {
                return ResponseMessage.error("菜品不存在");
            }
        } catch (Exception e) {
            return ResponseMessage.error("获取菜品详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "获取指定类别的菜品", description = "根据类别ID获取该类别下的所有菜品")
    public ResponseMessage getMenusByCategory(
            @PathVariable @Parameter(description = "类别ID") Long categoryId) {
        try {
            List<Menu> menus = menuService.getMenusByCategory(categoryId);
            return ResponseMessage.success(menus);
        } catch (Exception e) {
            return ResponseMessage.error("获取类别菜品失败: " + e.getMessage());
        }
    }

    @GetMapping("/recommended")
    @Operation(summary = "获取推荐菜品", description = "获取系统中标记为推荐的菜品")
    public ResponseMessage getRecommendedMenus() {
        try {
            List<Menu> menus = menuService.getRecommendedMenus();
            return ResponseMessage.success(menus);
        } catch (Exception e) {
            return ResponseMessage.error("获取推荐菜品失败: " + e.getMessage());
        }
    }

    @PostMapping("/items")
    @Operation(summary = "添加菜品", description = "添加新的菜品信息")
    public ResponseMessage addMenu(@RequestBody Menu menu) {
        try {
            boolean success = menuService.addMenu(menu);
            if (success) {
                return ResponseMessage.success("菜品添加成功");
            } else {
                return ResponseMessage.error("菜品添加失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("添加菜品失败: " + e.getMessage());
        }
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "更新菜品", description = "更新指定菜品的信息")
    public ResponseMessage updateMenu(
            @PathVariable @Parameter(description = "菜品ID") Long id,
            @RequestBody Menu menu) {
        try {
            menu.setId(id);
            boolean success = menuService.updateMenu(menu);
            if (success) {
                return ResponseMessage.success("菜品更新成功");
            } else {
                return ResponseMessage.error("菜品更新失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("更新菜品失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "删除菜品", description = "删除指定菜品（逻辑删除，下架处理）")
    public ResponseMessage deleteMenu(
            @PathVariable @Parameter(description = "菜品ID") Long id) {
        try {
            boolean success = menuService.deleteMenu(id);
            if (success) {
                return ResponseMessage.success("菜品删除成功");
            } else {
                return ResponseMessage.error("菜品删除失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("删除菜品失败: " + e.getMessage());
        }
    }

    @PutMapping("/items/{id}/recommend")
    @Operation(summary = "设置推荐状态", description = "设置或取消菜品的推荐状态")
    public ResponseMessage setRecommendStatus(
            @PathVariable @Parameter(description = "菜品ID") Long id,
            @RequestParam @Parameter(description = "推荐状态：1推荐，0取消推荐") Integer recommend) {
        try {
            boolean success = menuService.setRecommendStatus(id, recommend);
            if (success) {
                return ResponseMessage.success("推荐状态设置成功");
            } else {
                return ResponseMessage.error("推荐状态设置失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("设置推荐状态失败: " + e.getMessage());
        }
    }

    @PutMapping("/items/{id}/status")
    @Operation(summary = "设置菜品状态", description = "设置菜品的上下架状态")
    public ResponseMessage setMenuStatus(
            @PathVariable @Parameter(description = "菜品ID") Long id,
            @RequestParam @Parameter(description = "状态：0上架，1下架") Integer status) {
        try {
            boolean success = menuService.setMenuStatus(id, status);
            if (success) {
                return ResponseMessage.success("菜品状态设置成功");
            } else {
                return ResponseMessage.error("菜品状态设置失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("设置菜品状态失败: " + e.getMessage());
        }
    }
}