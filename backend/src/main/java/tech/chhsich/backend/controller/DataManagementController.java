package tech.chhsich.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.chhsich.backend.entity.ResponseMessage;
import tech.chhsich.backend.service.DataConsistencyService;

import java.util.Map;

/**
 * 数据管理控制器
 *
 * 提供数据库一致性检查和管理功能，仅管理员可访问。
 * 用于维护数据库外键约束和数据完整性。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@RestController
@RequestMapping("/api/admin/data")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "数据管理", description = "数据库一致性检查和管理接口（仅管理员）")
public class DataManagementController {

    @Autowired
    private DataConsistencyService dataConsistencyService;

    /**
     * 检查所有外键约束的一致性
     *
     * 提供完整的数据库外键约束检查报告，
     * 包括菜品分类、订单条目、用户等约束的验证结果。
     *
     * @return 包含详细检查报告的响应对象
     */
    @GetMapping("/consistency/check")
    @Operation(summary = "检查数据一致性", description = "检查所有外键约束和数据一致性问题")
    public ResponseMessage checkDataConsistency() {
        try {
            DataConsistencyService.ConsistencyReport report = dataConsistencyService.checkAllConstraints();
            return ResponseMessage.success(report);
        } catch (Exception e) {
            return ResponseMessage.error("检查数据一致性失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据一致性检查摘要
     *
     * 提供简化的检查结果摘要，用于快速了解系统状态。
     *
     * @return 包含检查摘要的响应对象
     */
    @GetMapping("/consistency/summary")
    @Operation(summary = "获取一致性检查摘要", description = "获取简化的数据一致性检查结果摘要")
    public ResponseMessage getConsistencySummary() {
        try {
            DataConsistencyService.ConsistencyReport report = dataConsistencyService.checkAllConstraints();
            return ResponseMessage.success(Map.of(
                "allValid", report.isAllValid(),
                "totalChecks", report.getResults().size(),
                "passedChecks", (int) report.getResults().values().stream()
                    .filter(DataConsistencyService.CheckResult::isValid).count(),
                "failedChecks", (int) report.getResults().values().stream()
                    .filter(result -> !result.isValid()).count(),
                "summary", report.getSummary()
            ));
        } catch (Exception e) {
            return ResponseMessage.error("获取一致性检查摘要失败: " + e.getMessage());
        }
    }

    /**
     * 验证特定约束的健康状态
     *
     * @param constraintType 约束类型（menu-category、order-entry-menu、order-entry-order、order-user）
     * @return 指定约束的检查结果
     */
    @GetMapping("/consistency/{constraintType}")
    @Operation(summary = "验证特定约束", description = "检查特定类型的外键约束状态")
    public ResponseMessage checkSpecificConstraint(
            @PathVariable @Parameter(description = "约束类型") String constraintType) {
        try {
            DataConsistencyService.ConsistencyReport fullReport = dataConsistencyService.checkAllConstraints();
            DataConsistencyService.CheckResult specificResult = fullReport.getResults().get(constraintType);

            if (specificResult == null) {
                return ResponseMessage.error("不支持的约束类型: " + constraintType);
            }

            return ResponseMessage.success(specificResult);
        } catch (Exception e) {
            return ResponseMessage.error("检查约束失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库健康状态
     *
     * 提供数据库连接和基本健康状态的检查结果。
     *
     * @return 数据库健康状态信息
     */
    @GetMapping("/health")
    @Operation(summary = "数据库健康检查", description = "检查数据库连接和基本健康状态")
    public ResponseMessage checkDatabaseHealth() {
        try {
            // 尝试执行一致性检查作为健康检查的一部分
            DataConsistencyService.ConsistencyReport report = dataConsistencyService.checkAllConstraints();

            return ResponseMessage.success(Map.of(
                "status", report.isAllValid() ? "HEALTHY" : "WARNING",
                "timestamp", System.currentTimeMillis(),
                "details", "数据库连接正常，发现 " +
                    report.getResults().values().stream()
                        .filter(result -> !result.isValid())
                        .count() + " 个一致性问题"
            ));
        } catch (Exception e) {
            return ResponseMessage.success(Map.of(
                "status", "ERROR",
                "timestamp", System.currentTimeMillis(),
                "details", "数据库检查失败: " + e.getMessage()
            ));
        }
    }
}