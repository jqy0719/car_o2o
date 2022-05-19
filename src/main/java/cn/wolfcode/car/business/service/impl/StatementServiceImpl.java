package cn.wolfcode.car.business.service.impl;


import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.StatementQuery;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class StatementServiceImpl implements IStatementService {

    @Autowired
    private StatementMapper statementMapper;


    @Override
    public TablePageInfo<Statement> query(StatementQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<>(statementMapper.selectForList(qo));

    }


    @Override
    public void save(Statement statement) {
        statement.setCreateTime(new Date());
        statement.setStatus(Statement.STATUS_CONSUME);
        statementMapper.insert(statement);
    }

    @Override
    public Statement get(Long id) {
        return statementMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(Statement statement) {
        Statement select = statementMapper.selectByPrimaryKey(statement.getId());
        if(select == null || Statement.STATUS_PAID.equals(select.getStatus())){
            throw new BusinessException("该结算单无法修改！");
        }
        statementMapper.updateByPrimaryKey(statement);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            statementMapper.UpdateDeleteByPrimaryKey(dictId,Statement.DELETE_YES);
        }
    }

    @Override
    public List<Statement> list() {
        return statementMapper.selectAll();
    }

    @Override
    public void payStatement(Long statementId) {
        Statement statement = statementMapper.selectByPrimaryKey(statementId);
        if(!Statement.STATUS_CONSUME.equals(statement.getStatus())){
            throw  new BusinessException("不能重复支付");
        }
        statement.setStatus(Statement.STATUS_PAID);
        statement.setPayeeId(ShiroUtils.getUserId());
        statement.setPayTime(new Date());
        statementMapper.payStatement(statement);
    }


}
