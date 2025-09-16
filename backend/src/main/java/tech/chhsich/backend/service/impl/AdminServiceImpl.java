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

    /**
     * Constructs an AdminServiceImpl with the provided AdminMapper dependency.
     *
     * The mapper is retained for data-store interactions performed by this service.
     */
    public AdminServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }
    
    /**
     * Authenticate an administrator by username and password.
     *
     * <p>Validates that both parameters are non-empty, compares the MD5-hashed password
     * against the data store, and on success returns a ResponseMessage containing a JWT
     * `token` and a safe user object (`username`, `email`, `phone`, `role`, `createTime`).
     * On failure returns an error ResponseMessage with an appropriate message.
     *
     * @param username the administrator's username (must be non-empty)
     * @param password the plaintext password to authenticate (must be non-empty)
     * @return a success ResponseMessage with a data map containing keys
     *         "token" (String) and "user" (Map of non-sensitive user fields) if authentication
     *         succeeds; otherwise an error ResponseMessage with a human-readable message
     */
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

            // 创建安全的用户信息对象（不包含敏感信息）
            Map<String, Object> safeUserInfo = new HashMap<>();
            safeUserInfo.put("username", admin.getUsername());
            safeUserInfo.put("email", admin.getEmail());
            safeUserInfo.put("phone", admin.getPhone());
            safeUserInfo.put("role", admin.getRole());
            safeUserInfo.put("createTime", admin.getCreateTime());

            // 返回成功响应，包含token和安全的用户信息
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", safeUserInfo);
            return ResponseMessage.success(data);
        } else {
            return ResponseMessage.error("用户名或密码错误");
        }
    }
    
    /**
     * Update an administrator's password if the provided current password matches.
     *
     * The method validates inputs (non-empty), hashes the supplied plaintext
     * passwords with MD5, and delegates the update to the AdminMapper. If the
     * update affects at least one row the operation is considered successful.
     *
     * @param username     the administrator's username (non-empty)
     * @param oldPassword  the current plaintext password (non-empty; will be MD5-hashed)
     * @param newPassword  the new plaintext password (non-empty; will be MD5-hashed)
     * @return a ResponseMessage indicating success ("密码修改成功") or an error:
     *         "参数不能为空" when any parameter is empty, or
     *         "原密码错误或用户不存在" when the current password does not match or the user is missing
     */
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
    
    /**
     * Returns all administrator accounts.
     *
     * @return a list of Administrator objects representing every administrator; returns an empty list if none exist
     */
    @Override
    public List<Administrator> getAllMembers() {
        return adminMapper.getAllMembers();
    }

    /**
     * Delete a member account identified by username.
     *
     * Validates that the provided username is non-empty, attempts to remove the member via the mapper,
     * and returns a ResponseMessage indicating success ("会员删除成功") when a row was deleted or an
     * error ("会员不存在或删除失败" or "用户名不能为空") otherwise.
     *
     * @param username the username of the member to delete
     * @return a ResponseMessage describing the result of the delete operation
     */
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

    /**
     * Retrieves an administrator by username.
     *
     * @param username the administrator's username
     * @return the Administrator with the given username, or {@code null} if none exists
     */
    @Override
    public Administrator getMemberByUsername(String username) {
        return adminMapper.getMemberByUsername(username);
    }
}
