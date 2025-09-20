package tech.chhsich.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tech.chhsich.backend.entity.Ltype;
import tech.chhsich.backend.entity.Menu;
import tech.chhsich.backend.mapper.MenuMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类服务测试类
 *
 * 测试分类管理功能和外键约束检查。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@SpringBootTest
@Transactional
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MenuMapper menuMapper;

    /**
     * 测试删除分类时的外键约束检查
     */
    @Test
    public void testDeleteCategoryWithForeignKeyCheck() {
        // 首先创建一个新的分类
        Ltype category = new Ltype();
        category.setCateName("外键约束测试分类_" + System.currentTimeMillis());
        category.setAddress("测试地址");
        category.setProductName("测试产品");
        boolean addResult = categoryService.addCategory(category);
        assertTrue(addResult);

        // 获取分类ID
        Long categoryId = category.getId();
        assertNotNull(categoryId);

        // 创建一个关联的菜品
        Menu menu = new Menu();
        menu.setName("测试菜品");
        menu.setCategoryId(categoryId);
        menu.setImgPath("/path/to/image.jpg");
        menu.setInfo("测试菜品简介");
        menu.setOriginalPrice(10.0);
        menu.setHotPrice(8.0);
        menu.setCreateTime(LocalDateTime.now());
        menuMapper.insert(menu);

        // 尝试删除分类，应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            categoryService.deleteCategory(categoryId);
        });

        // 验证分类确实没有被删除
        Ltype existingCategory = categoryService.getCategoryById(categoryId);
        assertNotNull(existingCategory);
    }

    /**
     * 测试删除没有关联菜品的分类
     */
    @Test
    public void testDeleteCategoryWithoutAssociations() {
        // 创建一个新的分类
        Ltype category = new Ltype();
        category.setCateName("独立测试分类_" + System.currentTimeMillis());
        category.setAddress("独立测试地址");
        category.setProductName("独立测试产品");
        boolean addResult = categoryService.addCategory(category);
        assertTrue(addResult);

        Long categoryId = category.getId();
        assertNotNull(categoryId);

        // 删除分类，应该成功
        boolean deleteResult = categoryService.deleteCategory(categoryId);
        assertTrue(deleteResult);

        // 验证分类已被逻辑删除
        Ltype deletedCategory = categoryService.getCategoryById(categoryId);
        assertEquals(1, deletedCategory.getCateLock());
    }

    /**
     * 测试分类名称重复检查
     */
    @Test
    public void testCategoryNameDuplicateCheck() {
        // 创建第一个分类
        Ltype category1 = new Ltype();
        category1.setCateName("重复测试分类");
        category1.setAddress("地址1");
        category1.setProductName("产品1");
        categoryService.addCategory(category1);

        // 尝试创建同名分类，应该抛出异常
        Ltype category2 = new Ltype();
        category2.setCateName("重复测试分类");
        category2.setAddress("地址2");
        category2.setProductName("产品2");

        assertThrows(RuntimeException.class, () -> {
            categoryService.addCategory(category2);
        });
    }

    /**
     * 测试获取所有分类功能
     */
    @Test
    public void testGetAllCategories() {
        List<Ltype> categories = categoryService.getAllCategories();
        assertNotNull(categories);
        assertTrue(categories.size() > 0);

        // 验证返回的都是未删除的分类
        for (Ltype category : categories) {
            assertEquals(0, category.getCateLock());
        }
    }
}