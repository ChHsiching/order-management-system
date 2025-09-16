package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tech.chhsich.backend.entity.Ltype;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface LtypeMapper extends BaseMapper<Ltype> {

    /**
     * Retrieves all Ltype records with the specified catelock value.
     *
     * @param catelock the catelock value to match
     * @return a list of matching Ltype entities; empty if none found
     */
    @Select("SELECT * FROM ltypes WHERE catelock = #{catelock}")
    List<Ltype> findByCatelock(Integer catelock);

    /**
     * Retrieves a single Ltype record matching the given category name.
     *
     * @param catename the category name to search for
     * @return the matching Ltype, or {@code null} if no record exists
     */
    @Select("SELECT * FROM ltypes WHERE catename = #{catename}")
    Ltype findByCatename(String catename);

    /**
     * Retrieve an Ltype by its primary key.
     *
     * @param id the primary key of the Ltype record
     * @return the matching Ltype, or null if no record exists with the given id
     */
    @Select("SELECT * FROM ltypes WHERE id = #{id}")
    Ltype findById(Long id);
}
