package cn.wolfcode.car.business.service.impl;


import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ServiceItemServiceImpl implements IServiceItemService {

    @Autowired
    private ServiceItemMapper serviceItemMapper;
    @Autowired
    private ICarPackageAuditService carPackageAuditService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IBpmnInfoService bpmnInfoService;

    @Override
    public TablePageInfo<ServiceItem> query(ServiceItemQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<ServiceItem>(serviceItemMapper.selectForList(qo));
    }


    @Override
    public void save(ServiceItem serviceItem) {
        serviceItem.setCreateTime(new Date());
        if(serviceItem.getCarPackage()){
            serviceItem.setAuditStatus(ServiceItem.AUDITSTATUS_INIT);
        }else {
            serviceItem.setAuditStatus(ServiceItem.AUDITSTATUS_NO_REQUIRED);
        }
        serviceItem.setSaleStatus(ServiceItem.SALESTATUS_OFF);

        serviceItemMapper.insert(serviceItem);
    }

    @Override
    public ServiceItem get(Long id) {
        return serviceItemMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(ServiceItem serviceItem) {
        ServiceItem item = serviceItemMapper.selectByPrimaryKey(serviceItem.getId());
        if(item == null ){
            throw new BusinessException("参数异常");
        }
        if(ServiceItem.SALESTATUS_ON.equals(item.getSaleStatus())){
            throw new BusinessException("上架服务项目不能修改，请下架后再修改");
        }
        if(ServiceItem.AUDITSTATUS_AUDITING.equals(item.getAuditStatus())){
            throw new BusinessException("服务项目正在审核中,不可修改");
        }
        serviceItemMapper.updateByPrimaryKey(serviceItem);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            serviceItemMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<ServiceItem> list() {
        return serviceItemMapper.selectAll();
    }

    @Override
    public void saleOn(Long id) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        if(serviceItem == null || !ServiceItem.SALESTATUS_OFF.equals(serviceItem.getSaleStatus())){
            throw new BusinessException("参数异常");
        }
        if(!serviceItem.getCarPackage()){
            serviceItemMapper.setSaleStatus(id,ServiceItem.SALESTATUS_ON);
            return;
        }
        if(serviceItem.getCarPackage()){
            if(ServiceItem.AUDITSTATUS_APPROVED.equals(serviceItem.getAuditStatus())){
                serviceItemMapper.setSaleStatus(id,ServiceItem.SALESTATUS_ON);
                return;
            }else{
                throw new BusinessException("只有在审核通过情况下才允许上架");
            }
        }

        throw new BusinessException("参数异常");
    }

    @Override
    public void saleOff(Long id) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        if(serviceItem == null || ServiceItem.SALESTATUS_OFF.equals(serviceItem.getSaleStatus())){
            throw new BusinessException("参数异常");
        }
        serviceItemMapper.setSaleStatus(id,ServiceItem.SALESTATUS_OFF);

    }

    @Override
    public boolean selectByName(String name,Long id) {

        ServiceItem serviceItem = serviceItemMapper.selectByName(name);
        if(serviceItem == null){
            return false;
        }
         return !serviceItem.getId().equals(id);

    }

    @Override
    public TablePageInfo<ServiceItem> selectAllSaleOnList() {
        return new TablePageInfo<>(serviceItemMapper.selectAllSaleOnList(ServiceItem.SALESTATUS_ON));
    }

    @Override
    public void startAudit(Long id, Long bpmnInfoId, Long director, Long finance, String info) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        if(serviceItem == null ||!ServiceItem.AUDITSTATUS_INIT.equals(serviceItem.getAuditStatus())){
            throw new BusinessException("当前无法进行套餐审核");
        }
        serviceItemMapper.changeAuditStatus(id,ServiceItem.AUDITSTATUS_AUDITING);
        CarPackageAudit carPackageAudit = new CarPackageAudit();
        carPackageAudit.setServiceItemId(serviceItem.getId());
        carPackageAudit.setServiceItemPrice(serviceItem.getDiscountPrice());
        carPackageAudit.setServiceItemInfo(serviceItem.getInfo());
        carPackageAudit.setCreator(ShiroUtils.getUser().getLoginName());
        carPackageAudit.setCreateTime(new Date());
        carPackageAudit.setBpmnInfoId(bpmnInfoId);
        carPackageAudit.setAuditorId(director);
        carPackageAudit.setInfo(info);
        carPackageAuditService.save(carPackageAudit);

        Map<String , Object> map = new HashMap<>();
        if(director != null){
            map.put("director",director);
        }
        if(finance != null){
            map.put("finance",finance);
        }

        map.put("discountPrice",serviceItem.getDiscountPrice().longValue());
        BpmnInfo bpmnInfo = bpmnInfoService.get(bpmnInfoId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(bpmnInfo.getActProcessId(), carPackageAudit.getId().toString(), map);
        carPackageAudit.setInstanceId(processInstance.getId());
        carPackageAuditService.update(carPackageAudit);
    }

    @Override
    public void changeStatus(Long serviceItemId, Integer auditStatus) {
        serviceItemMapper.changeAuditStatus(serviceItemId,auditStatus);
    }


}

