package tech.chhsich.backend.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.pojo.ResponseMessage;

@RestController
@RequestMapping("/admin/menu")
@Validated
public class MenuController {

    // 菜单管理功能 - 待实现
    @GetMapping("/categories")
    public ResponseMessage getAllCategories() {
        return ResponseMessage.success("菜单类别管理功能待实现");
    }

    @GetMapping("/items")
    public ResponseMessage getAllMenus() {
        return ResponseMessage.success("菜品管理功能待实现");
    }
}