package tech.chhsich.backend.controller;

import tech.chhsich.backend.entity.Ltype;
import tech.chhsich.backend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "分类管理", description = "菜品分类查询接口")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "获取所有分类", description = "获取所有可用的菜品分类")
    @GetMapping
    public ResponseEntity<List<Ltype>> getAllCategories() {
        List<Ltype> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "根据ID获取分类", description = "根据分类ID获取详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "分类不存在")
    @GetMapping("/{id}")
    public ResponseEntity<Ltype> getCategoryById(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id) {
        Ltype category = categoryService.getCategoryById(id);
        if (category != null) {
            return ResponseEntity.ok(category);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "根据名称获取分类", description = "根据分类名称获取详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "分类不存在")
    @GetMapping("/name/{catename}")
    public ResponseEntity<Ltype> getCategoryByName(
            @Parameter(description = "分类名称", required = true) @PathVariable String catename) {
        Ltype category = categoryService.getCategoryByName(catename);
        if (category != null) {
            return ResponseEntity.ok(category);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}