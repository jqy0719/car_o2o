package cn.wolfcode.car.business.service;


import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 审核流程接口
 */
public interface IBpmnInfoService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo);


    /**
     * 查单个
     * @param id
     * @return
     */
    BpmnInfo get(Long id);


    /**
     * 保存
     * @param bpmnInfo
     */
    void save(BpmnInfo bpmnInfo);

  
    /**
     * 更新
     * @param bpmnInfo
     */
    void update(BpmnInfo bpmnInfo);

    /**
     *  批量删除
     * @param ids
     * @param
     */
    void deleteBatch(String ids);

    /**
     * 查询全部
     * @return
     */
    List<BpmnInfo> list();

    AjaxResult upload(MultipartFile file);

    void deploy(BpmnInfo bpmnInfo);

    void delete(Long id);

    InputStream readResource(String deploymentId, String type);

    BpmnInfo queryByType(String car_package);

}
