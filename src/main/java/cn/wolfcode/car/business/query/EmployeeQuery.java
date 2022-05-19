package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter @Setter
public class EmployeeQuery extends QueryObject {
    private String keyword;
    private Integer admin;
    private Integer status;
    private Integer deptId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date hiredateStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date hiredateEnd;
}
