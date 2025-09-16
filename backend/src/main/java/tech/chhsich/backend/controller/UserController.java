package tech.chhsich.backend.controller;

import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /**
     * Registers a new user.
     *
     * @param user Administrator object containing registration details
     * @return ResponseEntity with HTTP 200 and the created Administrator on success, or HTTP 400 with an error message if registration fails (e.g., username/email/phone already exists)
     */
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

    /**
     * Authenticate a user with the given username and password.
     *
     * Attempts to log in and returns the authenticated Administrator on success or
     * a 400 response with an error message if authentication fails.
     *
     * @param username the user's username
     * @param password the user's password
     * @return ResponseEntity containing an Administrator (200) on success or an error message (400) on failure
     */
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

    /**
     * Retrieves public user details for the given username.
     *
     * Only the user themself or a caller with ROLE_ADMIN may access this resource.
     * On success returns HTTP 200 with the Administrator object; sensitive fields
     * (password and qq) are cleared. Returns HTTP 403 if the caller is not authorized,
     * and HTTP 404 if no user with the given username exists.
     *
     * @param username the username to look up
     * @return a ResponseEntity:
     *         - 200 with the Administrator (sensitive fields nulled) when found and authorized;
     *         - 403 with an error message when the caller is not allowed to view other users;
     *         - 404 when the user does not exist.
     */
    @Operation(summary = "获取用户信息", description = "根据用户名获取用户详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "用户不存在")
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        // 1. 验证当前用户身份
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(username) && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权访问其他用户信息");
        }

        // 2. 获取用户信息
        Administrator user = userService.getUserByUsername(username);
        if (user != null) {
            // 3. 过滤敏感信息
            user.setPassword(null);
            user.setQq(null); // QQ号也可能属于敏感信息
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}