package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.chhsich.backend.entity.Ltype;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface LtypeMapper extends BaseMapper<Ltype> {

    /**
     * Retrieves all Ltype records whose `catelock` column matches the given value.
     *
     * @param catelock the catelock value to filter by
     * @return a list of matching Ltype entities; empty if none found
     */
    @Select("SELECT * FROM ltypes WHERE catelock = #{catelock}")
    List<Ltype> findByCatelock(Integer catelock);

    /**
     * Retrieves an Ltype whose `catename` column matches the provided value.
     *
     * @param catename the category name to search for
     * @return the matching Ltype, or {@code null} if no match is found
     */
    @Select("SELECT * FROM ltypes WHERE catename = #{catename}")
    Ltype findByCatename(String catename);

    /**
     * Retrieve the Ltype with the given primary key.
     *
     * @param id the primary key of the Ltype to fetch
     * @return the matching Ltype, or {@code null} if none found
     */
    @Select("SELECT * FROM ltypes WHERE id = #{id}")
    Ltype findById(Long id);
}
