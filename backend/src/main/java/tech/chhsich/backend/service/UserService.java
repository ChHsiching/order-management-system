package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.mapper.AdministratorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private AdministratorMapper administratorMapper;

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

        user.setRole(0);
        user.setCreateTime(LocalDateTime.now());
        administratorMapper.insert(user);
        return user;
    }

    public Administrator login(String username, String password) {
        Administrator user = administratorMapper.findByUsernameAndPassword(username, password);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }

    public Administrator getUserByUsername(String username) {
        return administratorMapper.selectById(username);
    }
}