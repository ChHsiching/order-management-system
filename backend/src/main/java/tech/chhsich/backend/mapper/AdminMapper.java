package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import tech.chhsich.backend.entity.Administrator;

import java.util.List;

@Mapper
public interface AdminMapper extends BaseMapper<Administrator> {

    /**
     * Counts administrators matching the given username and password.
     *
     * @param username the administrator's username to authenticate
     * @param password the administrator's password to authenticate
     * @return the number of administrator records matching the credentials (0 if none)
     */
    @Select("SELECT COUNT(username) FROM administrators WHERE username=#{username} AND password=#{password}")
    int login1(String username, String password);

    /**
     * Retrieves an Administrator matching the given username and password.
     *
     * @param username the administrator's username
     * @param password the administrator's password
     * @return the matching Administrator, or {@code null} if none is found
     */
    @Select("SELECT * FROM administrators WHERE username=#{username} AND password=#{password}")
    Administrator getAdminByUsernameAndPassword(String username, String password);

    /**
     * Update an administrator's password if the provided current password matches.
     *
     * Updates the password for the administrator with the given username when the existing
     * password equals {@code oldPassword}.
     *
     * @param username    the administrator's username
     * @param oldPassword the current password to validate before updating
     * @param newPassword the new password to set
     * @return the number of rows affected (1 if the password was changed, 0 if no matching user or password)
     */
    @Update("UPDATE administrators SET password=#{newPassword} WHERE username=#{username} AND password=#{oldPassword}")
    int updatePassword(String username, String oldPassword, String newPassword);

    /**
     * Retrieves all administrators with the "member" role.
     *
     * Returns a list of Administrator entities whose role indicates a member (role = 0).
     * If no members exist, an empty list is returned.
     *
     * @return a list of member Administrators (never null)
     */
    @Select("SELECT * FROM administrators WHERE role=0")
    List<Administrator> getAllMembers();

    /**
     * Delete a member account (administrators with role = 0) by username.
     *
     * @param username the member's username to delete
     * @return the number of rows affected (0 if no matching member, 1 if deleted)
     */
    @Delete("DELETE FROM administrators WHERE username=#{username} AND role=0")
    int deleteMember(String username);

    /**
     * Retrieves an Administrator by username.
     *
     * @param username the username to search for
     * @return the matching Administrator, or null if none is found
     */
    @Select("SELECT * FROM administrators WHERE username=#{username}")
    Administrator getMemberByUsername(String username);

    /**
     * Retrieves an Administrator by username.
     *
     * @param username the username to look up
     * @return the Administrator with the given username, or null if none found
     */
    @Select("SELECT * FROM administrators WHERE username = #{username}")
    Administrator findByUsername(String username);

    /**
     * Returns whether an administrator with the given username exists.
     *
     * @param username the username to check for existence
     * @return true if an administrator with the specified username exists, false otherwise
     */
    @Select("SELECT COUNT(*) FROM administrators WHERE username = #{username}")
    boolean existsByUsername(String username);

    /**
     * Checks whether an administrator with the given email exists.
     *
     * @param email the email address to check for existence
     * @return true if at least one administrator record with the email exists, false otherwise
     */
    @Select("SELECT COUNT(*) FROM administrators WHERE email = #{email}")
    boolean existsByEmail(String email);

    /**
     * Checks whether an administrator exists with the given phone number.
     *
     * @param phone the phone number to check
     * @return true if at least one administrator has the specified phone number, false otherwise
     */
    @Select("SELECT COUNT(*) FROM administrators WHERE phone = #{phone}")
    boolean existsByPhone(String phone);
}
