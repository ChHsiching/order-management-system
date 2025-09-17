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

    /**
     * Creates a new UserService with required dependencies.
     *
     * The provided mapper and password encoder are used for user persistence and password handling.
     */
    public UserService(AdminMapper adminMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new Administrator after validating uniqueness and encoding the password.
     *
     * Validates that username, email, and phone are unique; if any check fails a RuntimeException
     * is thrown with message "用户名已存在", "邮箱已存在", or "手机号已存在" respectively.
     * On success the user's password is encoded, role is set to 0, createTime is set to now,
     * the user is inserted via the mapper, and the persisted Administrator is returned.
     *
     * @param user Administrator to register; its password field must contain the raw password.
     * @return the registered Administrator with an encoded password, role set to 0, and createTime populated.
     * @throws RuntimeException if username, email, or phone already exists (see messages above)
     */
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

    /**
     * Authenticate an administrator by username and password.
     *
     * If authentication succeeds returns a safe Administrator instance with sensitive fields
     * (password) omitted; otherwise throws a RuntimeException with message "用户名或密码错误".
     *
     * @param username the administrator's username
     * @param password the raw password to verify against the stored encoded password
     * @return an Administrator containing public data (username, email, phone, role, createTime, qq) but not the password
     * @throws RuntimeException if the username is not found or the password does not match
     */
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

    /**
     * Retrieves an Administrator by username.
     *
     * @param username the account username to look up
     * @return the matching Administrator, or {@code null} if no user exists with the given username
     */
    public Administrator getUserByUsername(String username) {
        return adminMapper.findByUsername(username);
    }
}