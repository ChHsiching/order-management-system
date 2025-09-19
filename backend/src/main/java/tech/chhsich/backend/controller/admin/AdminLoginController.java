package tech.chhsich.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.entity.ResponseMessage;
import tech.chhsich.backend.mapper.AdminMapper;
import tech.chhsich.backend.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员登录控制器
 * 提供管理员登录认证功能，只有role=1的管理员才能登录后台系统
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员认证", description = "管理员登录认证相关接口")
public class AdminLoginController {

    private final AdminMapper adminMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AdminLoginController(AdminMapper adminMapper, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.adminMapper = adminMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 管理员登录接口
     * 只有role=1的管理员才能登录成功
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果和JWT令牌
     */
    @Operation(summary = "管理员登录", description = "管理员登录认证，只有role=1的管理员才能登录")
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage> login(
            @Parameter(description = "用户名", required = true)
            @RequestParam String username,

            @Parameter(description = "密码", required = true)
            @RequestParam String password) {

        try {
            // 1. 验证用户是否存在
            Administrator admin = adminMapper.getMemberByUsername(username);
            if (admin == null) {
                return ResponseEntity.ok(ResponseMessage.error("用户名或密码错误"));
            }

            // 2. 验证用户角色是否为管理员 (role=1)
            if (admin.getRole() != 1) {
                return ResponseEntity.ok(ResponseMessage.error("无管理员权限"));
            }

            // 3. Spring Security认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5. 生成JWT令牌（包含角色信息）
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", admin.getRole());
            claims.put("email", admin.getEmail());
            String token = jwtUtil.generateToken(username, claims);

            // 6. 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", admin.getUsername());
            data.put("role", admin.getRole());
            data.put("email", admin.getEmail());

            return ResponseEntity.ok(ResponseMessage.success("登录成功", data));

        } catch (Exception e) {
            return ResponseEntity.ok(ResponseMessage.error("登录失败：" + e.getMessage()));
        }
    }

    /**
     * 获取当前登录管理员信息
     *
     * @return 当前管理员信息
     */
    @Operation(summary = "获取当前管理员信息", description = "获取当前登录管理员的详细信息")
    @GetMapping("/info")
    public ResponseEntity<ResponseMessage> getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Administrator admin = adminMapper.getMemberByUsername(username);

            if (admin != null && admin.getRole() == 1) {
                Map<String, Object> data = new HashMap<>();
                data.put("username", admin.getUsername());
                data.put("email", admin.getEmail());
                data.put("phone", admin.getPhone());
                data.put("role", admin.getRole());
                data.put("createTime", admin.getCreateTime());

                return ResponseEntity.ok(ResponseMessage.success("获取成功", data));
            }
        }

        return ResponseEntity.ok(ResponseMessage.error("未登录或无管理员权限"));
    }

    /**
     * 管理员登出
     *
     * @return 登出结果
     */
    @Operation(summary = "管理员登出", description = "管理员登出系统")
    @PostMapping("/logout")
    public ResponseEntity<ResponseMessage> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ResponseMessage.success("登出成功"));
    }
}