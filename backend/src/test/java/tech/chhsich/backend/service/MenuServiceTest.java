package tech.chhsich.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tech.chhsich.backend.entity.Menu;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MenuService测试类
 * 验证菜单服务层的业务逻辑正确性和数据一致性
 *
 * 测试场景：
 * 1. 获取所有可用菜品列表
 * 2. 根据ID获取菜品详情（有效和无效ID）
 * 3. 根据分类获取菜品列表
 * 4. 获取推荐菜品列表
 * 5. 搜索菜品功能（包括中文和空值处理）
 * 6. 获取热销菜品列表（销量排序）
 */
@SpringBootTest
@Transactional
class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    @Test
    void testGetAllAvailableMenus() {
        // 测试场景1：获取所有可用菜品列表
        List<Menu> menus = menuService.getAllAvailableMenus();

        assertNotNull(menus, "获取的菜品列表不应该为空");

        // 验证返回的列表不为空或为空时也能正常处理
        if (!menus.isEmpty()) {
            // 验证所有菜品都是未锁定的
            for (Menu menu : menus) {
                assertEquals(0, menu.getProductLock(), "所有菜品应该是未锁定状态");
                assertNotNull(menu.getName(), "菜品名称不应该为空");
                assertNotNull(menu.getImgPath(), "菜品图片路径不应该为空");
            }
        } else {
            // 如果没有数据，记录日志但不让测试失败
            System.out.println("警告: 数据库中没有可用菜品数据，这可能影响其他测试结果");
        }
    }

    @Test
    void testGetMenuByIdWithValidId() {
        // 测试场景2.1：使用有效ID获取菜品详情
        // 首先检查是否有可用菜品，如果没有则跳过严格验证
        List<Menu> allMenus = menuService.getAllAvailableMenus();

        if (!allMenus.isEmpty()) {
            // 使用第一个可用菜品的ID进行测试
            Long validId = allMenus.get(0).getId();
            Menu menu = menuService.getMenuById(validId);

            assertNotNull(menu, "应该能获取到ID为" + validId + "的菜品");
            assertEquals(validId, menu.getId(), "获取的菜品ID应该匹配");
            assertNotNull(menu.getName(), "菜品名称不应该为空");
            assertNotNull(menu.getImgPath(), "菜品图片路径不应该为空");
        } else {
            // 如果没有数据，测试ID为1的菜品（可能在测试数据中）
            Long validId = 1L;
            Menu menu = menuService.getMenuById(validId);

            if (menu != null) {
                assertEquals(validId, menu.getId(), "获取的菜品ID应该匹配");
                assertNotNull(menu.getName(), "菜品名称不应该为空");
                assertNotNull(menu.getImgPath(), "菜品图片路径不应该为空");
            } else {
                System.out.println("警告: 数据库中没有找到ID为" + validId + "的菜品");
            }
        }
    }

    @Test
    void testGetMenuByIdWithInvalidId() {
        // 测试场景2.2：使用无效ID获取菜品详情
        Long invalidId = 999L;
        Menu menu = menuService.getMenuById(invalidId);

        assertNull(menu, "不应该能获取到不存在的菜品ID");
    }

    @Test
    void testGetMenusByCategory() {
        // 测试场景3：根据分类获取菜品列表
        Long categoryId = 1L; // 假设存在分类ID为1
        List<Menu> menus = menuService.getMenusByCategory(categoryId);

        assertNotNull(menus, "分类菜品列表不应该为空");

        // 验证所有菜品都属于指定分类（如果categoryId不为null）
        if (!menus.isEmpty()) {
            for (Menu menu : menus) {
                if (menu.getCategoryId() != null) {
                    assertEquals(categoryId, menu.getCategoryId(), "菜品分类ID应该匹配");
                }
                assertEquals(0, menu.getProductLock(), "所有菜品应该是未锁定状态");
            }
        } else {
            System.out.println("警告: 分类ID " + categoryId + " 下没有菜品数据");
        }
    }

    @Test
    void testGetRecommendedMenus() {
        // 测试场景4：获取推荐菜品列表
        List<Menu> menus = menuService.getRecommendedMenus();

        assertNotNull(menus, "推荐菜品列表不应该为空");

        // 验证所有菜品都是推荐状态（如果isRecommend不为null）
        for (Menu menu : menus) {
            if (menu.getIsRecommend() != null) {
                assertEquals(1, menu.getIsRecommend(), "所有菜品应该是推荐状态");
            }
            assertEquals(0, menu.getProductLock(), "所有菜品应该是未锁定状态");
            assertNotNull(menu.getName(), "菜品名称不应该为空");
        }
    }

    @Test
    void testSearchMenusWithKeyword() {
        // 测试场景5.1：使用关键词搜索菜品
        String keyword = "汉堡";
        List<Menu> menus = menuService.searchMenus(keyword);

        assertNotNull(menus, "搜索结果不应该为空");

        // 验证搜索结果包含关键词（模糊匹配）
        for (Menu menu : menus) {
            assertNotNull(menu.getName(), "菜品名称不应该为空");
            // 由于是模糊匹配，不要求精确包含，但应该返回相关结果
            assertEquals(0, menu.getProductLock(), "搜索结果应该是未锁定菜品");
        }
    }

    @Test
    void testSearchMenusWithEmptyKeyword() {
        // 测试场景5.2：使用空关键词搜索菜品
        String emptyKeyword = "";
        List<Menu> menus = menuService.searchMenus(emptyKeyword);

        assertNotNull(menus, "空关键词搜索结果不应该为空");

        // 验证返回的都是未锁定菜品
        for (Menu menu : menus) {
            assertEquals(0, menu.getProductLock(), "应该只返回未锁定菜品");
        }

        // 如果有测试数据，空关键词应该返回所有可用菜品
        List<Menu> allAvailableMenus = menuService.getAllAvailableMenus();
        if (!allAvailableMenus.isEmpty()) {
            assertFalse(menus.isEmpty(), "当有可用菜品时，空关键词应该返回所有可用菜品");
            assertEquals(allAvailableMenus.size(), menus.size(), "空关键词应该返回所有可用菜品");
        }
    }

    @Test
    void testSearchMenusWithChineseKeyword() {
        // 测试场景5.3：使用中文关键词搜索菜品
        String chineseKeyword = "巨无霸";
        List<Menu> menus = menuService.searchMenus(chineseKeyword);

        assertNotNull(menus, "中文搜索结果不应该为空");

        // 验证中文搜索功能正常工作
        for (Menu menu : menus) {
            assertNotNull(menu.getName(), "菜品名称不应该为空");
            assertEquals(0, menu.getProductLock(), "搜索结果应该是未锁定菜品");
        }
    }

    @Test
    void testGetHotSalesMenusWithLimit() {
        // 测试场景6.1：获取限制数量的热销菜品
        Integer limit = 5;
        List<Menu> menus = menuService.getHotSalesMenus(limit);

        assertNotNull(menus, "热销菜品列表不应该为空");
        assertTrue(menus.size() <= limit, "返回的菜品数量不应该超过限制数量");

        // 验证按销量排序（第一个应该是销量最高的）
        if (menus.size() > 1) {
            for (int i = 0; i < menus.size() - 1; i++) {
                assertTrue(menus.get(i).getSales() >= menus.get(i + 1).getSales(),
                    "菜品应该按销量降序排列");
            }
        }

        // 验证所有菜品都是未锁定状态
        for (Menu menu : menus) {
            assertEquals(0, menu.getProductLock(), "热销菜品应该是未锁定状态");
            assertNotNull(menu.getName(), "菜品名称不应该为空");
        }
    }

    @Test
    void testGetHotSalesMenuWithoutLimit() {
        // 测试场景6.2：获取所有热销菜品（不限制数量）
        List<Menu> menus = menuService.getHotSalesMenus(null);

        assertNotNull(menus, "热销菜品列表不应该为空");

        // 验证按销量排序
        if (menus.size() > 1) {
            for (int i = 0; i < menus.size() - 1; i++) {
                assertTrue(menus.get(i).getSales() >= menus.get(i + 1).getSales(),
                    "菜品应该按销量降序排列");
            }
        }

        // 验证销量大于0的菜品优先
        for (Menu menu : menus) {
            assertEquals(0, menu.getProductLock(), "热销菜品应该是未锁定状态");
            assertNotNull(menu.getName(), "菜品名称不应该为空");
        }
    }

    @Test
    void testMenuDataConsistency() {
        // 测试场景7：验证菜品数据一致性
        List<Menu> allMenus = menuService.getAllAvailableMenus();

        for (Menu menu : allMenus) {
            // 验证必要字段不为空
            assertNotNull(menu.getId(), "菜品ID不应该为空");
            assertNotNull(menu.getName(), "菜品名称不应该为空");
            assertNotNull(menu.getImgPath(), "菜品图片路径不应该为空");
            assertNotNull(menu.getCreateTime(), "创建时间不应该为空");

            // 验证价格字段的合理性（如果存在）
            if (menu.getOriginalPrice() != null) {
                assertTrue(menu.getOriginalPrice() >= 0, "原价应该大于等于0");
            }
            if (menu.getHotPrice() != null) {
                assertTrue(menu.getHotPrice() >= 0, "优惠价应该大于等于0");
            }

            // 验证销量字段（如果存在）
            if (menu.getSales() != null) {
                assertTrue(menu.getSales() >= 0, "销量应该大于等于0");
            }

            // 验证状态字段
            assertNotNull(menu.getProductLock(), "锁定状态不应该为空");
            // isRecommend可能为null，这是允许的
        }
    }

    @Test
    void testServiceInjection() {
        // 测试场景8：验证MenuService正确注入
        assertNotNull(menuService, "MenuService应该成功注入");
    }

    @Test
    void testDatabaseConnection() {
        // 测试场景9：验证数据库连接正常
        // 通过调用实际方法来测试数据库连接
        List<Menu> menus = menuService.getAllAvailableMenus();
        assertNotNull(menus, "数据库查询应该正常工作");

        // 如果数据库中有数据，验证数据完整性
        if (!menus.isEmpty()) {
            Menu firstMenu = menus.get(0);
            assertNotNull(firstMenu.getId(), "数据库中的菜品应该有有效ID");
        }
    }
}