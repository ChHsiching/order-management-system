package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Ltype;
import tech.chhsich.backend.mapper.LtypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private LtypeMapper ltypeMapper;

    /**
     * Retrieves all categories that are not marked as deleted.
     *
     * @return a list of Ltype representing non-deleted categories (records with catelock == 0)
     */
    public List<Ltype> getAllCategories() {
        return ltypeMapper.findByCatelock(0); // 0表示未删除的分类
    }

    /**
     * Retrieves a category by its name.
     *
     * @param catename the exact name of the category to look up
     * @return the matching {@code Ltype} if found, otherwise {@code null}
     */
    public Ltype getCategoryByName(String catename) {
        return ltypeMapper.findByCatename(catename);
    }

    /**
     * Retrieve a category by its database ID.
     *
     * @param id the category's primary key
     * @return the matching Ltype, or {@code null} if no category with the given ID exists
     */
    public Ltype getCategoryById(Long id) {
        return ltypeMapper.findById(id); // 使用Mapper中新增的findById方法
    }
}
