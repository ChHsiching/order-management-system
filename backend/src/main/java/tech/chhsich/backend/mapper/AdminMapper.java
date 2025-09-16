package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import tech.chhsich.backend.entity.Administrator;

import java.util.List;

@Mapper
public interface AdminMapper extends BaseMapper<Administrator> {

    @Select("SELECT COUNT(username) FROM administrators WHERE username=#{username} AND password=#{password}")
    int login1(String username, String password);

    @Select("SELECT * FROM administrators WHERE username=#{username} AND password=#{password}")
    Administrator getAdminByUsernameAndPassword(String username, String password);

    @Update("UPDATE administrators SET password=#{newPassword} WHERE username=#{username} AND password=#{oldPassword}")
    int updatePassword(String username, String oldPassword, String newPassword);

    @Select("SELECT * FROM administrators WHERE role=0")
    List<Administrator> getAllMembers();

    @Delete("DELETE FROM administrators WHERE username=#{username} AND role=0")
    int deleteMember(String username);

    @Select("SELECT * FROM administrators WHERE username=#{username}")
    Administrator getMemberByUsername(String username);

    // Additional methods for UserService compatibility
    @Select("SELECT * FROM administrators WHERE username = #{username}")
    Administrator findByUsername(String username);

    @Select("SELECT COUNT(*) FROM administrators WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Select("SELECT COUNT(*) FROM administrators WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Select("SELECT COUNT(*) FROM administrators WHERE phone = #{phone}")
    boolean existsByPhone(String phone);
}
