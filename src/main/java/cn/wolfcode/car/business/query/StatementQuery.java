package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StatementQuery extends QueryObject {
    private String customerName;
    private String customerPhone;
    private String licensePlate;
}
