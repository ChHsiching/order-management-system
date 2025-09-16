package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.chhsich.backend.entity.Ltype;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface LtypeMapper extends BaseMapper<Ltype> {

    @Select("SELECT * FROM ltypes WHERE catelock = #{catelock}")
    List<Ltype> findByCatelock(Integer catelock);

    @Select("SELECT * FROM ltypes WHERE catename = #{catename}")
    Ltype findByCatename(String catename);

    // 添加按ID查询分类的方法，与BaseMapper的selectById功能一致但更明确
    @Select("SELECT * FROM ltypes WHERE id = #{id}")
    Ltype findById(Long id);
}
