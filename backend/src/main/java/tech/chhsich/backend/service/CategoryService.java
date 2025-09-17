package tech.chhsich.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import tech.chhsich.backend.entity.Ltype;
import tech.chhsich.backend.mapper.LtypeMapper;

import java.util.List;

@Service
public class CategoryService {

    private static final Integer CATEGORY_STATUS_ACTIVE = 0;
    private static final Integer CATEGORY_STATUS_DELETED = 1;

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
        return ltypeMapper.findByCatelock(CATEGORY_STATUS_ACTIVE);
    }

    /**
     * 获取所有类别（包括已删除的）
     */
    public List<Ltype> getAllCategoriesIncludingDeleted() {
        return ltypeMapper.selectList(null);
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
        return ltypeMapper.findById(id);
    }

    /**
     * 添加新类别
     */
    public boolean addCategory(Ltype category) {
        // 检查类别名是否已存在
        Ltype existingCategory = ltypeMapper.findByCatename(category.getCateName());
        if (existingCategory != null) {
            throw new RuntimeException("类别名称已存在");
        }

        category.setCateLock(CATEGORY_STATUS_ACTIVE);
        return ltypeMapper.insert(category) > 0;
    }

    /**
     * 更新类别信息
     */
    public boolean updateCategory(Ltype category) {
        Ltype existingCategory = ltypeMapper.findById(category.getId());
        if (existingCategory == null) {
            return false;
        }

        // 检查类别名是否与其他类别冲突
        Ltype nameConflictCategory = ltypeMapper.findByCatename(category.getCateName());
        if (nameConflictCategory != null && !nameConflictCategory.getId().equals(category.getId())) {
            throw new RuntimeException("类别名称已存在");
        }

        return ltypeMapper.updateById(category) > 0;
    }

    /**
     * 删除类别（逻辑删除）
     */
    public boolean deleteCategory(Long id) {
        Ltype category = ltypeMapper.findById(id);
        if (category == null) {
            return false;
        }

        category.setCateLock(CATEGORY_STATUS_DELETED);
        return ltypeMapper.updateById(category) > 0;
    }

    /**
     * 恢复已删除的类别
     */
    public boolean restoreCategory(Long id) {
        Ltype category = ltypeMapper.findById(id);
        if (category == null) {
            return false;
        }

        category.setCateLock(CATEGORY_STATUS_ACTIVE);
        return ltypeMapper.updateById(category) > 0;
    }

    /**
     * 检查类别是否存在
     */
    public boolean categoryExists(Long id) {
        return ltypeMapper.findById(id) != null;
    }

    /**
     * 检查类别名是否已存在（排除指定ID）
     */
    public boolean categoryNameExistsExcludeId(String cateName, Long excludeId) {
        QueryWrapper<Ltype> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("catename", cateName);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return ltypeMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 获取类别数量统计
     */
    public long getCategoryCount() {
        QueryWrapper<Ltype> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("catelock", CATEGORY_STATUS_ACTIVE);
        return ltypeMapper.selectCount(queryWrapper);
    }
}
