package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.chhsich.backend.entity.Administrator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdministratorMapper extends BaseMapper<Administrator> {

    @Select("SELECT * FROM administrators WHERE username = #{username} AND password = #{password}")
    Administrator findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Select("SELECT COUNT(*) FROM administrators WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM administrators WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM administrators WHERE phone = #{phone}")
    boolean existsByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM administrators WHERE username = #{username}")
    Administrator findByUsername(@Param("username") String username);
}