package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.query.AppointmentQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface AppointmentMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Appointment record);

    Appointment selectByPrimaryKey(Long id);

    List<Appointment> selectAll();

    int updateByPrimaryKey(Appointment record);

    List<Appointment> selectForList(AppointmentQuery qo);

    void changeStatus(@Param("id") Long id, @Param("status") Integer status, @Param("actualArrivalTime") Date actualArrivalTime);


    void updateDelete(@Param("id") Long id, @Param("deleteStatus") Boolean deleteStatus);
}