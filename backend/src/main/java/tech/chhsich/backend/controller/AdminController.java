package tech.chhsich.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.entity.ResponseMessage;
import tech.chhsich.backend.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {

    private final AdminService adminService;

    /**
     * Creates an AdminController configured with the provided AdminService.
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

/**
     * Authenticates an administrator using the provided credentials.
     *
     * @param username the administrator's username
     * @param password the administrator's password
     * @return a ResponseMessage representing the authentication result (success or failure); on success the message may include authentication data
     */
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

/**
     * Retrieves all administrator members.
     *
     * Returns a success ResponseMessage whose payload is a List<Administrator> containing all members.
     */
    @GetMapping("/members")
    public ResponseMessage getAllMembers() {
        List<Administrator> members = adminService.getAllMembers();
        return ResponseMessage.success(members);
    }

//     根据用户名查询特定会员信息接口
/**
     * Retrieves detailed information for the administrator with the given username.
     *
     * Returns a success ResponseMessage containing the Administrator when found;
     * otherwise returns an error ResponseMessage with message "会员不存在".
     *
     * @param username the username of the administrator to look up
     * @return a ResponseMessage containing the Administrator on success or an error message if not found
     */
    @GetMapping("/members/{username}")
    public ResponseMessage getMemberByUsername(@PathVariable String username) {
        Administrator member = adminService.getMemberByUsername(username);
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
