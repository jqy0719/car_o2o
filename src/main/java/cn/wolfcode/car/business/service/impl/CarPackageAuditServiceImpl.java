package cn.wolfcode.car.business.service.impl;


import cn.wolfcode.car.base.service.IUserService;
import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.mapper.CarPackageAuditMapper;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import com.github.pagehelper.PageHelper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CarPackageAuditServiceImpl implements ICarPackageAuditService {

    @Autowired
    private CarPackageAuditMapper carPackageAuditMapper;

    @Autowired
    private IBpmnInfoService bpmnInfoService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IServiceItemService serviceItemService;
    @Autowired
    private IUserService userService;


    @Override
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<CarPackageAudit>(carPackageAuditMapper.selectForList(qo));

    }


    @Override
    public void save(CarPackageAudit carPackageAudit) {

        carPackageAuditMapper.insert(carPackageAudit);
    }

    @Override
    public CarPackageAudit get(Long id) {
        return carPackageAuditMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(CarPackageAudit carPackageAudit) {

        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
    }

    @Override
    public void deleteBatch(String ids) {

    }

    @Override
    public List<CarPackageAudit> list() {
        return carPackageAuditMapper.selectAll();
    }

    @Override
    public InputStream getProcessImgByAuditId(Long id) {

        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        BpmnInfo bpmnInfo = bpmnInfoService.get(carPackageAudit.getBpmnInfoId());
        InputStream inputStream = null;
        //更加流程文件使用代码方式画出来
        BpmnModel model = repositoryService.getBpmnModel(bpmnInfo.getActProcessId());
        ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
        //1:审核中
        if (CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())) {

            Task task = taskService.createTaskQuery()
                    .processInstanceId(carPackageAudit.getInstanceId()).singleResult();
            List<String> activeActivityIds =
                    runtimeService.getActiveActivityIds(task.getExecutionId());

            inputStream = generator.generateDiagram(model, activeActivityIds,
                    Collections.EMPTY_LIST,
                    "宋体", "宋体", "宋体");
        } else {
            //2：审核结束
            inputStream = generator.generateDiagram(model, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                    "宋体", "宋体", "宋体");
        }
        return inputStream;
    }

    @Override
    public void cancelApply(Long id) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())) {
            throw new BusinessException("非审核中禁止撤销");
        }

        serviceItemService.changeStatus(carPackageAudit.getServiceItemId(), ServiceItem.AUDITSTATUS_INIT);
        carPackageAuditMapper.changeStatus(id, CarPackageAudit.STATUS_CANCEL);
        runtimeService.deleteProcessInstance(carPackageAudit.getInstanceId(), "撤销流程");
    }

    @Override
    public void audit(Long id, Integer auditStatus, String info) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        if (carPackageAudit == null || !CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())) {
            throw new BusinessException("当前无法进行审批");
        }

        String temp = userService.get(carPackageAudit.getAuditorId()).getLoginName();
        if (auditStatus == 2) {
            temp += "【" + temp + "】同意";
        }else{
            temp += "【" + temp + "】拒绝";
        }
        carPackageAudit.setInfo(info);
        carPackageAudit.setAuditRecord(temp );
        //流程审核记录表记录当前审核时间
        carPackageAudit.setAuditTime(new Date());

        Task task = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();


        taskService.setVariable(task.getId(),"auditStatus",auditStatus);
        taskService.addComment(task.getId(),carPackageAudit.getInstanceId(),info);
        taskService.complete(task.getId());

        Task nextTask = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();

        if(CarPackageAudit.STATUS_PASS.equals(auditStatus)){
            if(nextTask != null){
                carPackageAudit.setAuditorId(Long.valueOf(nextTask.getAssignee()));
            }else {
                serviceItemService.changeStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_APPROVED);
                carPackageAudit.setStatus(CarPackageAudit.STATUS_PASS);
            }

        }else{
            serviceItemService.changeStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_INIT);
            carPackageAudit.setStatus(CarPackageAudit.STATUS_REJECT);
        }

        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
    }


}
