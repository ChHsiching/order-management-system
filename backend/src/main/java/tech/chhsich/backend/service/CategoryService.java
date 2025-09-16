package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Ltype;
import tech.chhsich.backend.mapper.LtypeMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    private final LtypeMapper ltypeMapper;

    /**
     * Creates a CategoryService wired with the given LtypeMapper.
     *
     * The mapper is used for all category data access operations.
     */
    public CategoryService(LtypeMapper ltypeMapper) {
        this.ltypeMapper = ltypeMapper;
    }

    /**
     * Return all categories that are not marked as deleted.
     *
     * Retrieves Ltype records whose `catelock` flag equals 0 (non-deleted).
     *
     * @return a list of non-deleted Ltype categories
     */
    public List<Ltype> getAllCategories() {
        return ltypeMapper.findByCatelock(0); // 0表示未删除的分类
    }

    /**
     * Retrieve a category by its name.
     *
     * @param catename the category name to search for
     * @return the matching Ltype, or null if no category with the given name exists
     */
    public Ltype getCategoryByName(String catename) {
        return ltypeMapper.findByCatename(catename);
    }

    /**
     * Retrieve a category by its database ID.
     *
     * @param id the category's primary key
     * @return the Ltype with the given id, or null if no matching category exists
     */
    public Ltype getCategoryById(Long id) {
        return ltypeMapper.findById(id); // 使用Mapper中新增的findById方法
    }
}
