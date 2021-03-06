package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ServiceItemMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ServiceItem record);

    ServiceItem selectByPrimaryKey(Long id);

    List<ServiceItem> selectAll();

    int updateByPrimaryKey(ServiceItem record);

    List<ServiceItem> selectForList(ServiceItemQuery qo);

    void setSaleStatus(@Param("id") Long id, @Param("saleStatus") Integer saleStatus);

    ServiceItem selectByName(String name);

    List<ServiceItem> selectAllSaleOnList(Integer salestatusOn);

    void changeAuditStatus(@Param("id") Long id, @Param("auditStatus") Integer auditStatus);

}