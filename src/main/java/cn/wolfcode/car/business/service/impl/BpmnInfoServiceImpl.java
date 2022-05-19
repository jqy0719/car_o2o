package cn.wolfcode.car.business.service.impl;


import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.config.SystemConfig;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.file.FileUploadUtils;
import cn.wolfcode.car.common.web.AjaxResult;
import com.github.pagehelper.PageHelper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
@Transactional
public class BpmnInfoServiceImpl implements IBpmnInfoService {

    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;


    @Override
    public TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<BpmnInfo>(bpmnInfoMapper.selectForList(qo));
    }


    @Override
    public void save(BpmnInfo bpmnInfo) {

        bpmnInfoMapper.insert(bpmnInfo);
    }

    @Override
    public BpmnInfo get(Long id) {
        return bpmnInfoMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(BpmnInfo bpmnInfo) {
        bpmnInfoMapper.updateByPrimaryKey(bpmnInfo);
    }

    @Override
    public void deleteBatch(String ids) {

    }

    @Override
    public List<BpmnInfo> list() {
        return bpmnInfoMapper.selectAll();
    }

    @Override
    public AjaxResult upload(MultipartFile file) {
        if(file != null && file.getSize()>0){
            String filename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(filename);
            String path = null;
            if("zip".equalsIgnoreCase(extension)||"bpmn".equalsIgnoreCase(extension)){
                try {
                    path = FileUploadUtils.upload(SystemConfig.getUploadPath(), file);
                } catch (Exception e) {
                    throw new BusinessException("流程文件上传失败");
                }
                return AjaxResult.success("上传成功",path);
            }
            throw new BusinessException("只支持“zip”和“bpmn”格式文件");

        }
        throw new BusinessException("无法上传空文件！");
    }

    @Override
    public void deploy(BpmnInfo bpmnInfo) {
        String path = bpmnInfo.getBpmnPath();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(SystemConfig.getProfile(), path));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("流程文件获取失败");
        }
        DeploymentBuilder builder = repositoryService.createDeployment();
        Deployment deploy = null;
        if("zip".equalsIgnoreCase(FilenameUtils.getExtension(path))){
            deploy = builder.addZipInputStream(new ZipInputStream(inputStream)).deploy();
        }

        if("bpmn".equalsIgnoreCase(FilenameUtils.getExtension(path))){
            deploy = builder.addInputStream(bpmnInfo.getBpmnPath(),inputStream).deploy();
        }

        if(deploy == null){
            throw new BusinessException("流程部署失败");
        }

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deploy.getId()).latestVersion().singleResult();
        bpmnInfo.setActProcessId(processDefinition.getId());
        bpmnInfo.setBpmnName(processDefinition.getName());
        bpmnInfo.setActProcessKey(processDefinition.getKey());
        bpmnInfo.setDeploymentId(deploy.getId());
        bpmnInfo.setDeployTime(deploy.getDeploymentTime());

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bpmnInfoMapper.insert(bpmnInfo);
    }

    @Override
    public void delete(Long id) {
        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(id);
        File file = new File(SystemConfig.getProfile(), bpmnInfo.getBpmnPath());
        if(file.exists()){
            file.delete();
        }


        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().processDefinitionKey(bpmnInfo.getActProcessKey()).list();

        for (ProcessInstance instance : list) {

        }

        repositoryService.deleteDeployment(bpmnInfo.getDeploymentId(),true);

        bpmnInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public InputStream readResource(String deploymentId, String type) {

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).latestVersion().singleResult();
        if("xml".equalsIgnoreCase(type)){
            return repositoryService.getResourceAsStream(deploymentId, definition.getResourceName());
        }

        if("png".equalsIgnoreCase(type)){
            BpmnModel model = repositoryService.getBpmnModel(definition.getId());
            ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
            //generateDiagram(流程模型,需要高亮的节点,需要高亮的线条,后面三个参数都表示是字体)
            return generator.generateDiagram(model, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                    "宋体","宋体","宋体");

        }
        return null;
    }

    @Override
    public BpmnInfo queryByType(String type) {
        return bpmnInfoMapper.selectByType(type);
    }


}
