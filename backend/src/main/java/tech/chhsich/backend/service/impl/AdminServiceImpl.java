package tech.chhsich.backend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tech.chhsich.backend.mapper.AdminMapper;
import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.entity.ResponseMessage;
import tech.chhsich.backend.service.AdminService;
import tech.chhsich.backend.utils.JwUtil;
import tech.chhsich.backend.utils.Md5Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;

    public AdminServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }
    
    @Override
    public ResponseMessage login(String username, String password) {
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(password)) {
            return ResponseMessage.error("用户名或密码不能为空");
        }
        
        // 将密码进行MD5加密
        String md5Password = Md5Util.getMD5String(password);
        
        // 根据用户名和密码查询用户
        Administrator admin = adminMapper.getAdminByUsernameAndPassword(username, md5Password);

        if (admin != null) {
            // 登录成功，生成令牌(jwt)
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", admin.getUsername());
            claims.put("role", admin.getRole());
            String token = JwUtil.getToken(claims);

            // 返回成功响应，包含token
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", admin);
            return ResponseMessage.success(data);
        } else {
            return ResponseMessage.error("用户名或密码错误");
        }
    }
    
    @Override
    public ResponseMessage updatePassword(String username, String oldPassword, String newPassword) {
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(oldPassword) || !StringUtils.hasLength(newPassword)) {
            return ResponseMessage.error("参数不能为空");
        }
        
        // 将密码进行MD5加密
        String md5OldPassword = Md5Util.getMD5String(oldPassword);
        String md5NewPassword = Md5Util.getMD5String(newPassword);
        
        int result = adminMapper.updatePassword(username, md5OldPassword, md5NewPassword);
        
        if (result > 0) {
            return ResponseMessage.success("密码修改成功");
        } else {
            return ResponseMessage.error("原密码错误或用户不存在");
        }
    }
    
    @Override
    public List<Administrator> getAllMembers() {
        return adminMapper.getAllMembers();
    }

    @Override
    public ResponseMessage deleteMember(String username) {
        if (!StringUtils.hasLength(username)) {
            return ResponseMessage.error("用户名不能为空");
        }

        int result = adminMapper.deleteMember(username);

        if (result > 0) {
            return ResponseMessage.success("会员删除成功");
        } else {
            return ResponseMessage.error("会员不存在或删除失败");
        }
    }

    @Override
    public Administrator getMemberByUsername(String username) {
        return adminMapper.getMemberByUsername(username);
    }
}
