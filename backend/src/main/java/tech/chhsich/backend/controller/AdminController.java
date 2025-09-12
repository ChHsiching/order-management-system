package tech.chhsich.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.chhsich.backend.pojo.Administrators;
import tech.chhsich.backend.pojo.ResponseMessage;
import tech.chhsich.backend.service.AdminService;

@RestController
@RequestMapping("/admin")
@Validated //表明当前类开启验证
public class AdminController {
    @Autowired
    private AdminService adminService;

    //管理员登录验证
    @PostMapping("/login")
    public ResponseMessage login(String username, String password) {
        return adminService.login(username, password);
    }
}
