package tech.chhsich.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.entity.ResponseMessage;
import tech.chhsich.backend.service.MenuService;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 前台菜单管理控制器
 *
 * 提供前台用户公开访问的菜单查询接口。
 * 基于项目设计文档第6.3节普通用户模块的系统主页面实现，
 * 为用户提供菜单浏览、搜索和分类查看功能。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@RestController
@RequestMapping("/api/menu")
@Validated
@Tag(name = "前台菜单管理", description = "前台用户菜单浏览和查询接口")
public class FrontendMenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取所有可用的菜品列表
     *
     * 为前台用户提供所有上架菜品的浏览功能。
     * 基于项目设计文档第6.3.1节系统主页面实现，
     * 支持普通用户的菜单浏览需求。
     *
     * @return ResponseMessage 包含可用菜品列表的响应对象
     */
    @GetMapping
    @Operation(summary = "获取所有可用菜品", description = "获取系统中所有上架的可供用户浏览的菜品列表")
    public ResponseMessage getAllAvailableMenus() {
        try {
            List<Menu> menus = menuService.getAllAvailableMenus();
            return ResponseMessage.success(menus);
        } catch (Exception e) {
            return ResponseMessage.error("获取菜品列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取菜品详情
     *
     * 提供单个菜品的详细信息查看功能。
     * 基于项目设计文档第6.3.4节用户点餐模块，
     * 支持用户点击查看菜品详细信息。
     *
     * @param id 菜品ID，用于标识特定菜品
     * @return ResponseMessage 包含菜品详情的响应对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取菜品详情", description = "根据菜品ID获取菜品的详细信息")
    public ResponseMessage getMenuById(
            @PathVariable @Parameter(description = "菜品ID") Long id) {
        try {
            Menu menu = menuService.getMenuById(id);
            if (menu != null && menu.getProductLock() == 0) {
                return ResponseMessage.success(menu);
            } else if (menu == null) {
                return ResponseMessage.error("菜品不存在");
            } else {
                return ResponseMessage.error("菜品已下架");
            }
        } catch (Exception e) {
            return ResponseMessage.error("获取菜品详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类ID获取菜品列表
     *
     * 提供按分类浏览菜品的功ꢀ能。
     * 基于项目设计文档第6.3.1节系统主页面实现，
     * 支持用户按类别查看菜品。
     *
     * @param categoryId 分类ID，用于筛选特定类别的菜品
     * @return ResponseMessage 包含分类菜品列表的响应对象
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "获取指定分类的菜品", description = "根据分类ID获取该分类下的所有上架菜品")
    public ResponseMessage getMenusByCategory(
            @PathVariable @Parameter(description = "分类ID") Long categoryId) {
        try {
            List<Menu> menus = menuService.getMenusByCategory(categoryId);
            return ResponseMessage.success(menus);
        } catch (Exception e) {
            return ResponseMessage.error("获取分类菜品失败: " + e.getMessage());
        }
    }

    /**
     * 获取推荐菜品列表
     *
     * 提供推荐菜品的展示功能。
     * 基于项目设计文档第5.4节菜单信息表设计中的newstuijian字段，
     * 为用户提供推荐菜品浏览。
     *
     * @return ResponseMessage 包含推荐菜品列表的响应对象
     */
    @GetMapping("/recommended")
    @Operation(summary = "获取推荐菜品", description = "获取系统中标记为推荐的菜品列表")
    public ResponseMessage getRecommendedMenus() {
        try {
            List<Menu> menus = menuService.getRecommendedMenus();
            System.out.println("DEBUG: 获取到 " + menus.size() + " 个推荐菜品");

            // 手动转换为包含价格字段的Map列表
            List<Map<String, Object>> result = new ArrayList<>();
            for (Menu menu : menus) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", menu.getId());
                item.put("createTime", menu.getCreateTime());
                item.put("imgPath", menu.getImgPath());
                item.put("info", menu.getInfo());
                item.put("name", menu.getName());
                item.put("isRecommend", menu.getIsRecommend());
                item.put("originalPrice", menu.getOriginalPrice());
                item.put("hotPrice", menu.getHotPrice());
                item.put("productLock", menu.getProductLock());
                item.put("sales", menu.getSales());
                item.put("categoryId", menu.getCategoryId());
                result.add(item);

                System.out.println("DEBUG: 菜品 " + menu.getName() + " 价格字段: originalPrice=" + menu.getOriginalPrice() + ", hotPrice=" + menu.getHotPrice());
            }

            return ResponseMessage.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseMessage.error("获取推荐菜品失败: " + e.getMessage());
        }
    }

    /**
     * 搜索菜品
     *
     * 提供菜品名称搜索功能。
     * 基于项目设计文档第6.3.1节系统主页面实现，
     * 支持用户通过关键词搜索菜品。
     *
     * @param keyword 搜索关键词，进行URL编码处理
     * @return ResponseMessage 包含搜索结果菜品列表的响应对象
     */
    @GetMapping("/search")
    @Operation(summary = "搜索菜品", description = "根据菜品名称关键词搜索菜品，支持URL编码")
    public ResponseMessage searchMenus(
            @RequestParam @Parameter(description = "搜索关键词") String keyword) {
        try {
            // 处理URL编码和空值
            if (keyword == null || keyword.trim().isEmpty()) {
                List<Menu> menus = menuService.getAllAvailableMenus();
                return ResponseMessage.success(menus);
            }

            // URL解码处理中文编码问题
            String decodedKeyword = java.net.URLDecoder.decode(keyword, "UTF-8");
            List<Menu> menus = menuService.searchMenus(decodedKeyword);
            return ResponseMessage.success(menus);
        } catch (Exception e) {
            return ResponseMessage.error("搜索菜品失败: " + e.getMessage());
        }
    }

    /**
     * 获取热销菜品列表
     *
     * 提供热销菜品的展示功能。
     * 基于项目设计文档第5.4节菜单信息表设计中的xiaoliang字段，
     * 为用户展示销量高的热门菜品。
     *
     * @param limit 返回数量限制，默认返回前10个热销菜品
     * @return ResponseMessage 包含热销菜品列表的响应对象
     */
    @GetMapping("/hot-sales")
    @Operation(summary = "获取热销菜品", description = "获取销量最高的菜品列表")
    public ResponseMessage getHotSalesMenus(
            @RequestParam(defaultValue = "10") @Parameter(description = "返回数量限制") Integer limit) {
        try {
            List<Menu> menus = menuService.getHotSalesMenus(limit);
            return ResponseMessage.success(menus);
        } catch (Exception e) {
            return ResponseMessage.error("获取热销菜品失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有菜品分类
     *
     * 提供分类查询功能，为前台用户展示所有可用的菜品分类。
     * 基于项目设计文档第6.3.1节系统主页面实现，
     * 支持用户按分类浏览菜品。
     *
     * @return ResponseMessage 包含分类列表的响应对象
     */
    @GetMapping("/categories")
    @Operation(summary = "获取所有菜品分类", description = "获取系统中所有的菜品分类信息")
    public ResponseMessage getAllCategories() {
        try {
            // 需要注入CategoryService
            List<tech.chhsich.backend.entity.Ltype> categories = menuService.getAllCategories();
            return ResponseMessage.success(categories);
        } catch (Exception e) {
            return ResponseMessage.error("获取分类失败: " + e.getMessage());
        }
    }
}