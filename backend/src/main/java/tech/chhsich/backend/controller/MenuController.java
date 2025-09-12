package tech.chhsich.backend.controller;

import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "菜品管理", description = "菜品查询和推荐接口")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Operation(summary = "获取所有可用菜品", description = "获取所有未下架的菜品列表")
    @GetMapping
    public ResponseEntity<List<Menu>> getAllMenus() {
        List<Menu> menus = menuService.getAllAvailableMenus();
        return ResponseEntity.ok(menus);
    }

    @Operation(summary = "按分类获取菜品", description = "根据分类ID获取该分类下的所有菜品")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Menu>> getMenusByCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId) {
        List<Menu> menus = menuService.getMenusByCategory(categoryId);
        return ResponseEntity.ok(menus);
    }

    @Operation(summary = "获取推荐菜品", description = "获取推荐的菜品列表")
    @GetMapping("/recommended")
    public ResponseEntity<List<Menu>> getRecommendedMenus() {
        List<Menu> menus = menuService.getRecommendedMenus();
        return ResponseEntity.ok(menus);
    }

    @Operation(summary = "根据ID获取菜品详情", description = "根据菜品ID获取详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "菜品不存在")
    @GetMapping("/{id}")
    public ResponseEntity<Menu> getMenuById(
            @Parameter(description = "菜品ID", required = true) @PathVariable Long id) {
        Menu menu = menuService.getMenuById(id);
        if (menu != null) {
            return ResponseEntity.ok(menu);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}