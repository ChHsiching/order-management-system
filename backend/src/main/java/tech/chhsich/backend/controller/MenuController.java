package tech.chhsich.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.entity.ResponseMessage;

@RestController
@RequestMapping("/admin/menu")
@Validated
@Tag(name = "后台菜单管理", description = "后台菜单类别和菜品管理接口")
public class MenuController {

    // 菜单管理功能 - 待实现
    @GetMapping("/categories")
    @Operation(summary = "获取所有菜单类别", description = "获取系统中所有的菜单类别信息")
    public ResponseMessage getAllCategories() {
        return ResponseMessage.success("菜单类别管理功能待实现");
    }

    @GetMapping("/items")
    @Operation(summary = "获取所有菜品", description = "获取系统中所有的菜品信息")
    public ResponseMessage getAllMenus() {
        return ResponseMessage.success("菜品管理功能待实现");
    }
}