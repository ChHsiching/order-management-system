package tech.chhsich.backend.controller;

import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户注册、登录和信息查询接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册", description = "注册新用户")
    @ApiResponse(responseCode = "200", description = "注册成功")
    @ApiResponse(responseCode = "400", description = "注册失败，用户名/邮箱/手机号已存在")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Administrator user) {
        try {
            Administrator newUser = userService.registerUser(user);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "用户登录", description = "用户登录验证")
    @ApiResponse(responseCode = "200", description = "登录成功")
    @ApiResponse(responseCode = "400", description = "登录失败，用户名或密码错误")
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password) {
        try {
            Administrator user = userService.login(username, password);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "获取用户信息", description = "根据用户名获取用户详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "用户不存在")
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserInfo(
            @Parameter(description = "用户名", required = true) @PathVariable String username) {
        Administrator user = userService.getUserByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}