package tech.chhsich.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.pojo.Administrators;
import tech.chhsich.backend.pojo.ResponseMessage;
import tech.chhsich.backend.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {

    @Autowired
    private AdminService adminService;

//    管理员登录接口
    @PostMapping("/login")
    public ResponseMessage login(
            @RequestParam String username,
            @RequestParam String password) {
        return adminService.login(username, password);
    }

//管理员密码修改接口
    @PutMapping("/update")
    public ResponseMessage updatePassword(
            HttpServletRequest request,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        String username = (String) request.getAttribute("username");
        return adminService.updatePassword(username, oldPassword, newPassword);
    }

//     获取所有会员信息接口
    @GetMapping("/members")
    public ResponseMessage getAllMembers() {
        List<Administrators> members = adminService.getAllMembers();
        return ResponseMessage.success(members);
    }

//     根据用户名查询特定会员信息接口
//     获取指定用户名的会员详细信息
    @GetMapping("/members/{username}")
    public ResponseMessage getMemberByUsername(@PathVariable String username) {
        Administrators member = adminService.getMemberByUsername(username);
        if (member != null) {
            return ResponseMessage.success(member);
        } else {
            return ResponseMessage.error("会员不存在");
        }
    }

//     删除会员账户接口
//     根据用户名删除指定的会员账户
    @DeleteMapping("/members/{username}")
    public ResponseMessage deleteMember(@PathVariable String username) {
        return adminService.deleteMember(username);
    }
}
