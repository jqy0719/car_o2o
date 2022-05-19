package cn.wolfcode.car.business.controller;



import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.web.AjaxResult;
import cn.wolfcode.car.shiro.ShiroUtils;
import org.apache.poi.util.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 结算单控制器
 */
@Controller
@RequestMapping("business/carPackageAudit")
public class CarPackageAuditController {
    //模板前缀
    private static final String prefix = "business/carPackageAudit/";

    @Autowired
    private ICarPackageAuditService carPackageAuditService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:carPackageAudit:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }


    @RequestMapping("/auditPage")
    public String auditPage(Long id,Model model){

        model.addAttribute("id",id);
        return prefix + "audit";
    }

    @RequiresPermissions("business:carPackageAudit:add")
    @RequestMapping("/addPage")
    public String addPage(){
        return prefix + "add";
    }


    @RequestMapping("/processImg")
    public void  processImg(Long id, HttpServletResponse response){
    InputStream inputStream = carPackageAuditService.getProcessImgByAuditId(id);
        try {
            IOUtils.copy(inputStream,response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException("参数异常");
        }
    }

    @RequestMapping("/todoPage")
    public String  todoPage(){
       return prefix + "todoPage";
    }

    @RequestMapping("/donePage")
    public String  donePage(){
       return prefix + "donePage";
    }

    @RequiresPermissions("business:carPackageAudit:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("carPackageAudit", carPackageAuditService.get(id));
        return prefix + "edit";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:carPackageAudit:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo){
        return carPackageAuditService.query(qo);
    }

    @RequestMapping("/todoQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> todoQuery(CarPackageAuditQuery qo){

        qo.setStatus(CarPackageAudit.STATUS_IN_ROGRESS);
        qo.setAuditId(ShiroUtils.getUserId());
        return carPackageAuditService.query(qo);
    }

    @RequestMapping("/doneQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> doneQuery(CarPackageAuditQuery qo){

//        qo.setAuditId(ShiroUtils.getUserId());
        qo.setRecord(ShiroUtils.getLoginName());
        return carPackageAuditService.query(qo);
    }

    @RequestMapping("/audit")
    @ResponseBody
    public AjaxResult audit(Long id,Integer auditStatus,String info){
        carPackageAuditService.audit(id,auditStatus,info);
        return AjaxResult.success();
    }


    //新增
    @RequiresPermissions("business:carPackageAudit:add")
    @RequestMapping("/add")
    @ResponseBody
    public AjaxResult addSave(CarPackageAudit carPackageAudit){
        carPackageAuditService.save(carPackageAudit);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:carPackageAudit:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(CarPackageAudit carPackageAudit){
        carPackageAuditService.update(carPackageAudit);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:carPackageAudit:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String id){
        carPackageAuditService.deleteBatch(id);
        return AjaxResult.success();
    }


    @RequiresPermissions("business:carPackageAudit:cancelApply")
    @RequestMapping("/cancelApply")
    @ResponseBody
    public AjaxResult cancelApply(Long id){
        carPackageAuditService.cancelApply(id);
        return AjaxResult.success();
    }

}
