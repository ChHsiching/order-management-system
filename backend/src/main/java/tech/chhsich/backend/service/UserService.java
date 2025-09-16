// UserService.java
package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.mapper.AdministratorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private AdministratorMapper administratorMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new administrator account.
     *
     * Validates that the username, email, and phone are unique, encodes the provided password,
     * sets the role to 0 and creation timestamp to now, persists the administrator, and returns it.
     *
     * @param user an Administrator instance containing at least username, email, phone and a plaintext password;
     *             on successful return the instance will have its password replaced with the encoded value,
     *             role set to 0, and createTime set to the registration time
     * @return the persisted Administrator with encoded password, role = 0, and createTime set
     * @throws RuntimeException if the username, email, or phone already exists (messages: "用户名已存在", "邮箱已存在", "手机号已存在")
     */
    public Administrator registerUser(Administrator user) {
        if (administratorMapper.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (administratorMapper.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        if (administratorMapper.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        user.setRole(0);
        user.setCreateTime(LocalDateTime.now());
        administratorMapper.insert(user);
        return user;
    }

    /**
     * Authenticate an administrator by username and plaintext password.
     *
     * Returns the matching Administrator if the username exists and the provided
     * plaintext password matches the stored (encoded) password.
     *
     * @param username the administrator's username
     * @param password the plaintext password to verify
     * @return the authenticated Administrator
     * @throws RuntimeException if the username does not exist or the password is incorrect (message: "用户名或密码错误")
     */
    public Administrator login(String username, String password) {
        Administrator user = administratorMapper.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        throw new RuntimeException("用户名或密码错误");
    }

    /**
     * Retrieve an Administrator by their username.
     *
     * @param username the administrator's username (login name)
     * @return the matching Administrator, or {@code null} if no user with the given username exists
     */
    public Administrator getUserByUsername(String username) {
        return administratorMapper.findByUsername(username);
    }
}