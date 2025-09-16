package tech.chhsich.backend.controller;

import tech.chhsich.backend.entity.Ltype;
import tech.chhsich.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CategoryService categoryService;

    /**
     * Retrieves all available dish categories.
     *
     * @return a ResponseEntity containing a list of Ltype category objects with HTTP 200 status;
     *         the list may be empty if no categories exist
     */
    @Operation(summary = "获取所有分类", description = "获取所有可用的菜品分类")
    @GetMapping
    public ResponseEntity<List<Ltype>> getAllCategories() {
        List<Ltype> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Retrieves a category by its ID.
     *
     * Returns 200 OK with the Ltype when found, or 404 Not Found if no category exists for the given ID.
     *
     * @param id the category ID
     * @return a ResponseEntity containing the category when found or a 404 response otherwise
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
     * Returns the matching Ltype wrapped in a ResponseEntity with HTTP 200 if found,
     * otherwise responds with HTTP 404 Not Found.
     *
     * @param catename the category name to look up
     * @return ResponseEntity containing the found Ltype with status 200, or 404 if not found
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