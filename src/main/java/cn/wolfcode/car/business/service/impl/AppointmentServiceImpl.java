package cn.wolfcode.car.business.service.impl;


import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.AppointmentMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.AppointmentQuery;
import cn.wolfcode.car.business.service.IAppointmentService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AppointmentServiceImpl implements IAppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private StatementMapper statementMapper;


    @Override
    public TablePageInfo<Appointment> query(AppointmentQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Appointment>(appointmentMapper.selectForList(qo));
    }


    @Override
    public void save(Appointment appointment) {
        appointment.setStatus(Appointment.STATUS_APPOINTMENT);
        appointment.setCreateTime(new Date());
        appointmentMapper.insert(appointment);
    }

    @Override
    public Appointment get(Long id) {
        return appointmentMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(Appointment appointment) {
        Appointment temp = appointmentMapper.selectByPrimaryKey(appointment.getId());
        if(temp == null || !Appointment.STATUS_APPOINTMENT.equals(appointment.getStatus())){

            throw new BusinessException("非预约中禁止修改！");
        }
        appointmentMapper.updateByPrimaryKey(appointment);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            appointmentMapper.updateDelete(dictId,Appointment.DELETE_YES);
        }
    }

    @Override
    public List<Appointment> list() {
        return appointmentMapper.selectAll();
    }

    @Override
    public void cancel(Long id) {
        Appointment temp = appointmentMapper.selectByPrimaryKey(id);
        if(temp == null || !Appointment.STATUS_APPOINTMENT.equals(temp.getStatus())){

            throw new BusinessException("非预约中禁止取消！");
        }
        appointmentMapper.changeStatus(id,Appointment.STATUS_CANCEL,null);
    }

    @Override
    public void arrival(Long id) {
        Appointment temp = appointmentMapper.selectByPrimaryKey(id);
        if(temp == null || !Appointment.STATUS_APPOINTMENT.equals(temp.getStatus())){
            throw new BusinessException("非预约中无法改为已到店！");
        }
        appointmentMapper.changeStatus(id,Appointment.STATUS_ARRIVAL, new Date());

    }

    @Override
    public Statement generateStatement(Long id) {
        Statement statement = statementMapper.selectByAppointmentId(id);
        if(statement == null){
            Appointment appointment = appointmentMapper.selectByPrimaryKey(id);
            statement = new Statement();
            if(appointment.getActualArrivalTime() == null){
                throw new BusinessException("未到店不允许生成结算单");
            }
            statement.setCustomerName(appointment.getCustomerName());
            statement.setCustomerPhone(appointment.getCustomerPhone().toString());
            statement.setActualArrivalTime(appointment.getActualArrivalTime());
            statement.setLicensePlate(appointment.getLicensePlate());
            statement.setCarSeries(appointment.getCarSeries());
            statement.setServiceType(Long.valueOf(appointment.getServiceType()));
            statement.setAppointmentId(appointment.getId());
            statement.setCreateTime(new Date());
            statement.setStatus(Statement.STATUS_CONSUME);
            statementMapper.insert(statement);
            appointmentMapper.changeStatus(id,Appointment.STATUS_SETTLE,appointment.getActualArrivalTime());
        }
        return statement;
    }


}
