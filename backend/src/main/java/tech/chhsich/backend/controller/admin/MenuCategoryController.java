package tech.chhsich.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.entity.Ltype;
import tech.chhsich.backend.entity.ResponseMessage;
import tech.chhsich.backend.service.CategoryService;

import java.util.List;
import java.util.Map;

/**
 * 菜单类别管理控制器
 * 提供菜单类别的增删改查功能
 */
@RestController
@RequestMapping("/api/admin/menuCategory")
@Tag(name = "菜单类别管理", description = "菜单类别的增删改查接口")
public class MenuCategoryController {

    private final CategoryService categoryService;

    public MenuCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取所有菜单类别列表
     *
     * @return 所有菜单类别列表
     */
    @Operation(summary = "获取所有类别", description = "获取系统中所有菜单类别，包括已删除的")
    @GetMapping("/getList")
    public ResponseEntity<ResponseMessage> getCategoryList() {
        try {
            List<Ltype> categories = categoryService.getAllCategoriesIncludingDeleted();
            return ResponseEntity.ok(ResponseMessage.success("获取类别列表成功", categories));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("获取类别列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有激活的菜单类别
     *
     * @return 激活的菜单类别列表
     */
    @Operation(summary = "获取激活类别", description = "获取所有未删除的菜单类别")
    @GetMapping("/active")
    public ResponseEntity<ResponseMessage> getActiveCategories() {
        try {
            List<Ltype> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(ResponseMessage.success("获取激活类别成功", categories));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("获取激活类别失败：" + e.getMessage()));
        }
    }

    /**
     * 根据ID获取菜单类别详情
     *
     * @param id 类别ID
     * @return 类别详情
     */
    @Operation(summary = "获取类别详情", description = "根据ID获取菜单类别的详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage> getCategoryById(
            @Parameter(description = "类别ID", required = true)
            @PathVariable Long id) {
        try {
            Ltype category = categoryService.getCategoryById(id);
            if (category != null) {
                return ResponseEntity.ok(ResponseMessage.success("获取类别成功", category));
            } else {
                return ResponseEntity.ok(ResponseMessage.error("类别不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("获取类别失败：" + e.getMessage()));
        }
    }

    /**
     * 添加新的菜单类别
     *
     * @param category 类别信息
     * @return 添加结果
     */
    @Operation(summary = "添加类别", description = "添加新的菜单类别")
    @PostMapping("/add")
    public ResponseEntity<ResponseMessage> addCategory(
            @Parameter(description = "类别信息", required = true)
            @RequestBody Ltype category) {
        try {
            // 验证必要字段
            if (category.getCateName() == null || category.getCateName().trim().isEmpty()) {
                return ResponseEntity.ok(ResponseMessage.error("类别名称不能为空"));
            }

            boolean success = categoryService.addCategory(category);
            if (success) {
                return ResponseEntity.ok(ResponseMessage.success("添加类别成功", category));
            } else {
                return ResponseEntity.ok(ResponseMessage.error("添加类别失败"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResponseMessage.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("添加类别失败：" + e.getMessage()));
        }
    }

    /**
     * 更新菜单类别信息
     *
     * @param id 类别ID
     * @param category 更新的类别信息
     * @return 更新结果
     */
    @Operation(summary = "更新类别", description = "更新现有菜单类别信息")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseMessage> updateCategory(
            @Parameter(description = "类别ID", required = true)
            @PathVariable Long id,

            @Parameter(description = "更新的类别信息", required = true)
            @RequestBody Ltype category) {
        try {
            // 设置ID
            category.setId(id);

            // 验证必要字段
            if (category.getCateName() == null || category.getCateName().trim().isEmpty()) {
                return ResponseEntity.ok(ResponseMessage.error("类别名称不能为空"));
            }

            boolean success = categoryService.updateCategory(category);
            if (success) {
                return ResponseEntity.ok(ResponseMessage.success("更新类别成功", category));
            } else {
                return ResponseEntity.ok(ResponseMessage.error("更新类别失败，类别不存在"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResponseMessage.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("更新类别失败：" + e.getMessage()));
        }
    }

    /**
     * 删除菜单类别（按类别名删除）
     *
     * @param categoryName 类别名称
     * @return 删除结果
     */
    @Operation(summary = "删除类别", description = "根据类别名删除菜单类别")
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseMessage> deleteCategoryByName(
            @Parameter(description = "类别名称", required = true)
            @RequestParam String categoryName) {
        try {
            Ltype category = categoryService.getCategoryByName(categoryName);
            if (category == null) {
                return ResponseEntity.ok(ResponseMessage.error("类别不存在"));
            }

            boolean success = categoryService.deleteCategory(category.getId());
            if (success) {
                return ResponseEntity.ok(ResponseMessage.success("删除类别成功"));
            } else {
                return ResponseEntity.ok(ResponseMessage.error("删除类别失败"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResponseMessage.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("删除类别失败：" + e.getMessage()));
        }
    }

    /**
     * 根据ID删除菜单类别
     *
     * @param id 类别ID
     * @return 删除结果
     */
    @Operation(summary = "根据ID删除类别", description = "根据ID删除菜单类别")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> deleteCategoryById(
            @Parameter(description = "类别ID", required = true)
            @PathVariable Long id) {
        try {
            boolean success = categoryService.deleteCategory(id);
            if (success) {
                return ResponseEntity.ok(ResponseMessage.success("删除类别成功"));
            } else {
                return ResponseEntity.ok(ResponseMessage.error("删除类别失败，类别不存在"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResponseMessage.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("删除类别失败：" + e.getMessage()));
        }
    }

    /**
     * 恢复已删除的类别
     *
     * @param id 类别ID
     * @return 恢复结果
     */
    @Operation(summary = "恢复类别", description = "恢复已删除的菜单类别")
    @PostMapping("/{id}/restore")
    public ResponseEntity<ResponseMessage> restoreCategory(
            @Parameter(description = "类别ID", required = true)
            @PathVariable Long id) {
        try {
            boolean success = categoryService.restoreCategory(id);
            if (success) {
                return ResponseEntity.ok(ResponseMessage.success("恢复类别成功"));
            } else {
                return ResponseEntity.ok(ResponseMessage.error("恢复类别失败，类别不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("恢复类别失败：" + e.getMessage()));
        }
    }

    /**
     * 获取类别统计信息
     *
     * @return 统计信息
     */
    @Operation(summary = "获取类别统计", description = "获取类别相关的统计信息")
    @GetMapping("/statistics")
    public ResponseEntity<ResponseMessage> getCategoryStatistics() {
        try {
            long totalCount = categoryService.getCategoryCount();
            List<Ltype> allCategories = categoryService.getAllCategoriesIncludingDeleted();
            long deletedCount = allCategories.stream()
                    .filter(category -> category.getCateLock() == 1)
                    .count();

            Map<String, Object> statistics = Map.of(
                    "totalCount", totalCount,
                    "deletedCount", deletedCount,
                    "activeCount", totalCount - deletedCount
            );

            return ResponseEntity.ok(ResponseMessage.success("获取统计信息成功", statistics));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("获取统计信息失败：" + e.getMessage()));
        }
    }
}