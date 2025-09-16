// UserService.java
package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.mapper.AdminMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(AdminMapper adminMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Administrator registerUser(Administrator user) {
        if (adminMapper.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (adminMapper.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        if (adminMapper.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        user.setRole(0);
        user.setCreateTime(LocalDateTime.now());
        adminMapper.insert(user);
        return user;
    }

    public Administrator login(String username, String password) {
        Administrator user = adminMapper.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 创建安全的用户副本（不包含密码）
            Administrator safeUser = new Administrator();
            safeUser.setUsername(user.getUsername());
            safeUser.setEmail(user.getEmail());
            safeUser.setPhone(user.getPhone());
            safeUser.setRole(user.getRole());
            safeUser.setCreateTime(user.getCreateTime());
            safeUser.setQq(user.getQq());
            return safeUser;
        }
        throw new RuntimeException("用户名或密码错误");
    }

    public Administrator getUserByUsername(String username) {
        return adminMapper.findByUsername(username);
    }
}