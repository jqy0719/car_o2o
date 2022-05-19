package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.query.StatementQuery;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StatementMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Statement record);

    Statement selectByPrimaryKey(Long id);

    List<Statement> selectAll();

    int updateByPrimaryKey(Statement record);

    List<Statement> selectForList(StatementQuery qo);

    void updateAmount(@Param("id") Long id, @Param("totalAmount") BigDecimal totalAmount, @Param("totalQuantity") BigDecimal totalQuantity, @Param("discountAmount") BigDecimal discountAmount);

    void payStatement(Statement statement);

    Statement selectByAppointmentId(Long id);

    void UpdateDeleteByPrimaryKey(@Param("id") Long id, @Param("deleteStatus") Boolean deleteStatus);
}