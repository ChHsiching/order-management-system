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

    /**
     * Creates a CategoryController wired with the required CategoryService.
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Retrieve all available dish categories.
     *
     * Returns an HTTP 200 response containing a list of Ltype objects representing all categories.
     *
     * @return ResponseEntity<List<Ltype>> with the list of categories (HTTP 200)
     */
    @Operation(summary = "获取所有分类", description = "获取所有可用的菜品分类")
    @GetMapping
    public ResponseEntity<List<Ltype>> getAllCategories() {
        List<Ltype> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Retrieve a category by its ID.
     *
     * @param id the category's identifier
     * @return a ResponseEntity containing the found Ltype with HTTP 200, or an empty response with HTTP 404 if not found
     */
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

    /**
     * Retrieves a category by its name.
     *
     * Looks up a category with the given name and returns it with HTTP 200 if found;
     * returns HTTP 404 if no matching category exists.
     *
     * @param catename the category name to look up
     * @return ResponseEntity containing the found category (200) or an empty body with 404 if not found
     */
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