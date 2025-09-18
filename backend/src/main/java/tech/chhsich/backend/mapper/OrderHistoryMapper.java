package tech.chhsich.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import tech.chhsich.backend.entity.OrderHistory;

import java.util.List;

/**
 * 订单历史记录数据访问层
 *
 * 提供订单历史记录的持久化操作，包括查询、插入等功能。
 *
 * @author chhsich
 * @version 1.0
 * @since 2025-09-18
 */
@Mapper
public interface OrderHistoryMapper extends BaseMapper<OrderHistory> {

    /**
     * 根据订单ID查询历史记录
     *
     * @param orderId 订单ID
     * @return 该订单的所有历史记录，按操作时间倒序排列
     */
    @Select("SELECT * FROM order_history WHERE order_id = #{orderId} ORDER BY operation_time DESC")
    List<OrderHistory> findByOrderId(String orderId);

    /**
     * 查询指定时间范围内的订单历史记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间范围内的历史记录
     */
    @Select("SELECT * FROM order_history WHERE operation_time BETWEEN #{startTime} AND #{endTime} ORDER BY operation_time DESC")
    List<OrderHistory> findByTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 根据操作人员查询历史记录
     *
     * @param operator 操作人员
     * @return 该操作人员的历史记录
     */
    @Select("SELECT * FROM order_history WHERE operator = #{operator} ORDER BY operation_time DESC")
    List<OrderHistory> findByOperator(String operator);

    /**
     * 删除指定时间范围内的历史记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 删除的记录数
     */
    @Delete("DELETE FROM order_history WHERE operation_time BETWEEN #{startTime} AND #{endTime}")
    int deleteByTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}