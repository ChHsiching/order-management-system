package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.chhsich.backend.entity.Administrator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdministratorMapper extends BaseMapper<Administrator> {

    /**
     * Retrieves an Administrator matching the given username and password.
     *
     * @param username the administrator's username
     * @param password the administrator's password
     * @return the matching Administrator, or {@code null} if no match is found
     */
    @Select("SELECT * FROM administrators WHERE username = #{username} AND password = #{password}")
    Administrator findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    /**
     * Checks whether an administrator with the given username exists.
     *
     * @param username the username to check for existence
     * @return true if at least one administrator record has the given username; false otherwise
     */
    @Select("SELECT COUNT(*) FROM administrators WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    /**
     * Returns whether an administrator record exists with the given email.
     *
     * @param email the email address to check for existence
     * @return true if at least one administrator has the specified email; false otherwise
     */
    @Select("SELECT COUNT(*) FROM administrators WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Checks whether an administrator exists with the specified phone number.
     *
     * @param phone the phone number to check for an existing administrator
     * @return true if at least one administrator record has the given phone number; false otherwise
     */
    @Select("SELECT COUNT(*) FROM administrators WHERE phone = #{phone}")
    boolean existsByPhone(@Param("phone") String phone);

    /**
     * Retrieves an Administrator by its username.
     *
     * @param username the administrator's username to look up
     * @return the matching Administrator, or {@code null} if no match is found
     */
    @Select("SELECT * FROM administrators WHERE username = #{username}")
    Administrator findByUsername(@Param("username") String username);
}