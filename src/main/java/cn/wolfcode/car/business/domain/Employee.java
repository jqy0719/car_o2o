package cn.wolfcode.car.business.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class Employee {

    public static final Integer FREEZE_NO = 0;
    public static final Integer FREEZE_YES = 1;
    public static final Integer DELETE_NO = 0;
    public static final Integer DELETE_YES = 1;
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String email;

    /**
     *
     */
    private Integer age;

    /**
     *
     */
    private Integer admin;

    /**
     *
     */
    private Integer status;

    /**
     *
     */
    private Department department;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date hiredate;

    private Integer deleteStatus;


}