package cn.wolfcode.car.business.controller;



import cn.wolfcode.car.base.service.IUserService;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.query.StatementItemQuery;
import cn.wolfcode.car.business.service.IStatementItemService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 结算单明细控制器
 */
@Controller
@RequestMapping("business/statementItem")
public class StatementItemController {
    //模板前缀
    private static final String prefix = "business/statementItem/";

    @Autowired
    private IStatementItemService statementItemService;
    @Autowired
    private IStatementService statementService;
    @Autowired
    private IUserService userService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:statementItem:view")
    @RequestMapping("itemDetail")
    public String listPage(Long statementId,Model model){
        Statement statement = statementService.get(statementId);
        model.addAttribute("statement",statement);
        if(Statement.STATUS_CONSUME.equals(statement.getStatus())){
            return prefix + "editDetail";
        }else{
            statement.setPayee(userService.get(statement.getPayeeId()));
        return prefix + "showDetail";
        }
    }

    @RequiresPermissions("business:statementItem:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<StatementItem> query(Long statementId, StatementItemQuery qo){
        qo.setStatementId(statementId);
        return statementItemService.query(qo);

    }


    @RequiresPermissions("business:statementItem:saveItems")
    @RequestMapping("/saveItems")
    @ResponseBody
    public AjaxResult saveItems(@RequestBody List<StatementItem> items){
        statementItemService.saveItems(items);
        return AjaxResult.success();
    }

    @RequiresPermissions("business:statementItem:payStatement")
    @RequestMapping("/payStatement")
    @ResponseBody
    public AjaxResult payStatement(Long statementId){
        statementService.payStatement(statementId);
        return AjaxResult.success();
    }

}
