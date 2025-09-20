package tech.chhsich.backend.controller;

import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.service.UserService;
import tech.chhsich.backend.dto.UserRegistrationRequest;
import tech.chhsich.backend.dto.LoginRequest;
import tech.chhsich.backend.dto.LoginResponse;
import tech.chhsich.backend.utils.JwtUtil;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户注册、登录和信息查询接口")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Create a UserController backed by the provided UserService.
     *
     * The injected service is used to perform user registration, login, and retrieval operations.
     */
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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
    public ResponseEntity<?> register(@Validated @RequestBody UserRegistrationRequest registrationRequest) {
        try {
            // 转换DTO为实体类
            Administrator user = new Administrator();
            user.setUsername(registrationRequest.getUsername());
            user.setPassword(registrationRequest.getPassword());
            user.setEmail(registrationRequest.getEmail());
            user.setPhone(registrationRequest.getPhone());
            user.setQq(registrationRequest.getQq());

            Administrator newUser = userService.registerUser(user);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Authenticate a user with the given username and password (JSON format).
     *
     * Attempts to log in and returns JWT token and user info on success,
     * or an error message on authentication failure.
     *
     * @param loginRequest login request containing username and password
     * @return a LoginResponse containing JWT token and user info
     */
    @Operation(summary = "用户登录(JSON格式)", description = "用户登录验证，返回JWT token")
    @ApiResponse(responseCode = "200", description = "登录成功")
    @ApiResponse(responseCode = "400", description = "登录失败，用户名或密码错误")
    @PostMapping("/login")
    public ResponseEntity<?> loginJson(@Validated @RequestBody LoginRequest loginRequest) {
        System.out.println("[Login API] 收到登录请求: " + loginRequest.getUsername());
        try {
            Administrator user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            System.out.println("[Login API] 用户验证成功: " + user.getUsername());

            // 创建用户信息claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole());
            claims.put("email", user.getEmail());

            // 生成JWT token
            String token = jwtUtil.generateToken(user.getUsername(), claims);

            // 清理敏感信息
            user.setPassword(null);
            user.setQq(null);

            LoginResponse response = LoginResponse.success(token, user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("[Login API] 用户验证失败: " + e.getMessage());
            LoginResponse response = LoginResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Authenticate a user with the given username and password (form format).
     *
     * Attempts to log in and returns the authenticated Administrator on success,
     * or a 400 Bad Request with an error message on authentication failure.
     *
     * @param username the user's username
     * @param password the user's password
     * @return a ResponseEntity containing the authenticated Administrator (200) or an error message (400)
     */
    @Operation(summary = "用户登录(Form格式)", description = "用户登录验证")
    @ApiResponse(responseCode = "200", description = "登录成功")
    @ApiResponse(responseCode = "400", description = "登录失败，用户名或密码错误")
    @PostMapping("/login-form")
    public ResponseEntity<?> loginForm(
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

    /**
     * Update current user profile information.
     *
     * <p>Updates the profile information for the currently authenticated user.
     * Only email and phone can be updated, username cannot be changed.</p>
     *
     * @param updateRequest the profile update request containing email and phone
     * @return ResponseEntity with success message or error
     */
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "400", description = "更新失败")
    @PutMapping("/update")
    public ResponseEntity<?> updateCurrentUserProfile(@Validated @RequestBody UserProfileUpdateRequest updateRequest) {
        try {
            // 1. 获取当前认证用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();

            // 2. 更新用户信息
            Administrator updatedUser = userService.updateUserProfile(currentUsername, updateRequest.getEmail(), updateRequest.getPhone());

            // 3. 过滤敏感信息
            updatedUser.setPassword(null);
            updatedUser.setQq(null);

            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Change user password.
     *
     * <p>Changes the password for the currently authenticated user.
     * Requires old password for verification and new password with confirmation.</p>
     *
     * @param changePasswordRequest the password change request
     * @return ResponseEntity with success message or error
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @ApiResponse(responseCode = "200", description = "修改成功")
    @ApiResponse(responseCode = "400", description = "修改失败")
    @ApiResponse(responseCode = "403", description = "原密码错误")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Validated @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            // 1. 获取当前认证用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();

            // 2. 验证旧密码
            if (!userService.verifyPassword(currentUsername, changePasswordRequest.getOldPassword())) {
                return ResponseEntity.status(403).body("原密码错误");
            }

            // 3. 修改密码
            userService.changePassword(currentUsername, changePasswordRequest.getNewPassword());

            // 4. 返回成功消息
            Map<String, String> response = new HashMap<>();
            response.put("message", "密码修改成功");
            response.put("code", "200");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 用户信息更新请求DTO
    @Data
    public static class UserProfileUpdateRequest {
        @Email(message = "邮箱格式不正确")
        private String email;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
    }

    // 修改密码请求DTO
    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度应在6-20位之间")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度应在6-20位之间")
        private String newPassword;
    }
}