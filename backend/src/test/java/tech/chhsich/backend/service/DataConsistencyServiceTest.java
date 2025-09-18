package tech.chhsich.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tech.chhsich.backend.service.DataConsistencyService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据一致性服务测试类
 *
 * 测试数据库外键约束和数据一致性检查功能。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@SpringBootTest
@Transactional
public class DataConsistencyServiceTest {

    @Autowired
    private DataConsistencyService dataConsistencyService;

    /**
     * 测试数据一致性检查功能
     */
    @Test
    public void testDataConsistencyCheck() {
        DataConsistencyService.ConsistencyReport report = dataConsistencyService.checkAllConstraints();

        // 验证报告不为空
        assertNotNull(report);

        // 验证报告包含预期的约束检查
        assertEquals(4, report.getResults().size());
        assertTrue(report.getResults().containsKey("菜品分类外键约束"));
        assertTrue(report.getResults().containsKey("订单条目菜品外键约束"));
        assertTrue(report.getResults().containsKey("订单条目订单外键约束"));
        assertTrue(report.getResults().containsKey("订单用户外键约束"));

        // 输出检查结果
        System.out.println("=== 数据一致性检查报告 ===");
        report.getSummary().forEach(System.out::println);
    }

    /**
     * 测试单个约束检查功能
     */
    @Test
    public void testIndividualConstraintChecks() {
        DataConsistencyService.ConsistencyReport report = dataConsistencyService.checkAllConstraints();

        // 检查每个约束的详细结果
        for (DataConsistencyService.CheckResult result : report.getResults().values()) {
            assertNotNull(result.getConstraintName());
            assertNotNull(result.getConstraintDescription());
            assertNotNull(result.getIssues());

            // 输出详细检查结果
            System.out.println(result);
        }
    }

    /**
     * 测试一致性报告的toString方法
     */
    @Test
    public void testReportToString() {
        DataConsistencyService.ConsistencyReport report = dataConsistencyService.checkAllConstraints();
        String reportString = report.toString();

        assertNotNull(reportString);
        assertTrue(reportString.contains("数据一致性检查报告"));
        assertTrue(reportString.contains("约束:"));
    }
}