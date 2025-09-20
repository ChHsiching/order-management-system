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
        System.out.println("[UserService] 尝试登录: " + username);
        Administrator user = adminMapper.findByUsername(username);
        if (user != null) {
            System.out.println("[UserService] 找到用户: " + user.getUsername());
            System.out.println("[UserService] 数据库密码哈希: " + user.getPassword());
            System.out.println("[UserService] 输入密码: " + password);
            boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
            System.out.println("[UserService] 密码匹配结果: " + passwordMatch);

            if (passwordMatch) {
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
        } else {
            System.out.println("[UserService] 未找到用户: " + username);
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

    /**
     * Updates user profile information.
     *
     * @param username the username of the user to update
     * @param email the new email address (optional)
     * @param phone the new phone number (optional)
     * @return the updated Administrator object
     */
    public Administrator updateUserProfile(String username, String email, String phone) {
        // 1. 查找用户
        Administrator user = adminMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 更新邮箱和手机号（如果提供了新值）
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone);
        }

        // 3. 更新时间
        user.setCreateTime(LocalDateTime.now());

        // 4. 保存更新
        int result = adminMapper.updateUser(user);
        if (result > 0) {
            return user;
        } else {
            throw new RuntimeException("更新失败");
        }
    }

    /**
     * Verify user password.
     *
     * @param username the username of the user to verify
     * @param password the password to verify
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(String username, String password) {
        Administrator user = adminMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * Change user password.
     *
     * @param username the username of the user to update
     * @param newPassword the new password
     */
    public void changePassword(String username, String newPassword) {
        // 1. 查找用户
        Administrator user = adminMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 编码新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        // 3. 更新时间
        user.setCreateTime(LocalDateTime.now());

        // 4. 保存更新
        int result = adminMapper.updateUser(user);
        if (result == 0) {
            throw new RuntimeException("密码修改失败");
        }
    }
}