package tech.chhsich.backend.controller;

import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    private final UserService userService;

    /**
     * Create a UserController backed by the provided UserService.
     *
     * The injected service is used to perform user registration, login, and retrieval operations.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user.
     *
     * Attempts to create a new Administrator from the provided request body. On success returns
     * HTTP 200 with the created Administrator. If registration fails (for example, username, email,
     * or phone number already exists) returns HTTP 400 with an explanatory message.
     *
     * @param user the Administrator payload from the request body containing registration data
     * @return a ResponseEntity containing the created Administrator on success or an error message with status 400
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
     * Attempts to log in and returns the authenticated Administrator on success,
     * or a 400 Bad Request with an error message on authentication failure.
     *
     * @param username the user's username
     * @param password the user's password
     * @return a ResponseEntity containing the authenticated Administrator (200) or an error message (400)
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
     * Returns the details of the currently authenticated user.
     *
     * <p>Finds the username from the security context, looks up the corresponding
     * Administrator, clears sensitive fields (password and qq) and returns the
     * sanitized user object. If no user is found, responds with HTTP 404.</p>
     *
     * @return ResponseEntity with HTTP 200 and the sanitized Administrator when found,
     *         or HTTP 404 when the current user does not exist
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "用户不存在")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserInfo() {
        // 1. 获取当前认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // 2. 获取用户信息
        Administrator user = userService.getUserByUsername(currentUsername);
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