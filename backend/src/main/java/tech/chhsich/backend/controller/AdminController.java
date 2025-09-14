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

    @PostMapping("/login")
    public ResponseMessage login(
            @RequestParam String username,
            @RequestParam String password) {
        return adminService.login(username, password);
    }

    @PutMapping("/password")
    public ResponseMessage updatePassword(
            HttpServletRequest request,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        String username = (String) request.getAttribute("username");
        return adminService.updatePassword(username, oldPassword, newPassword);
    }

    @GetMapping("/members")
    public ResponseMessage getAllMembers() {
        List<Administrators> members = adminService.getAllMembers();
        return ResponseMessage.success(members);
    }

    @GetMapping("/members/{username}")
    public ResponseMessage getMemberByUsername(@PathVariable String username) {
        Administrators member = adminService.getMemberByUsername(username);
        if (member != null) {
            return ResponseMessage.success(member);
        } else {
            return ResponseMessage.error("会员不存在");
        }
    }

    @DeleteMapping("/members/{username}")
    public ResponseMessage deleteMember(@PathVariable String username) {
        return adminService.deleteMember(username);
    }
}
