package tech.chhsich.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import tech.chhsich.backend.pojo.Administrators;

@Mapper
public interface AdminMapper {
    @Select("SELECT COUNT(username) FROM administrators WHERE username=#{username} AND password=#{password}")
    public int login1(String username,String password);
}
