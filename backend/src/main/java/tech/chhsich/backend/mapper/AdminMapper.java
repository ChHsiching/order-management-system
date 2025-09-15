package tech.chhsich.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.chhsich.backend.entity.Administrators;

import java.util.List;

@Mapper
public interface AdminMapper {
    
    @Select("SELECT COUNT(username) FROM administrators WHERE username=#{username} AND password=#{password}")
    int login1(String username, String password);
    
    @Select("SELECT * FROM administrators WHERE username=#{username} AND password=#{password}")
    Administrators getAdminByUsernameAndPassword(String username, String password);
    
    @Update("UPDATE administrators SET password=#{newPassword} WHERE username=#{username} AND password=#{oldPassword}")
    int updatePassword(String username, String oldPassword, String newPassword);
    
    @Select("SELECT * FROM administrators WHERE role=0")
    List<Administrators> getAllMembers();
    
    @Delete("DELETE FROM administrators WHERE username=#{username} AND role=0")
    int deleteMember(String username);
    
    @Select("SELECT * FROM administrators WHERE username=#{username}")
    Administrators getMemberByUsername(String username);
}
