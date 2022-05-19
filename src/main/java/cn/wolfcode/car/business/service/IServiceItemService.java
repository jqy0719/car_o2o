package cn.wolfcode.car.business.service;


import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.util.List;

/**
 * 结算单服务接口
 */
public interface IServiceItemService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<ServiceItem> query(ServiceItemQuery qo);


    /**
     * 查单个
     * @param id
     * @return
     */
    ServiceItem get(Long id);


    /**
     * 保存
     * @param serviceItem
     */
    void save(ServiceItem serviceItem);

  
    /**
     * 更新
     * @param serviceItem
     */
    void update(ServiceItem serviceItem);

    /**
     *  批量删除
     * @param ids
     */
    void deleteBatch(String ids);

    /**
     * 查询全部
     * @return
     */
    List<ServiceItem> list();


    void saleOn(Long id);

    void saleOff(Long id);

    boolean selectByName(String name,Long id);

    TablePageInfo<ServiceItem> selectAllSaleOnList();


    void startAudit(Long id, Long bpmnInfoId, Long director, Long finance, String info);

    void changeStatus(Long serviceItemId, Integer auditStatus);

}
