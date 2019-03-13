package com.maihe.cms.model.entity.mall;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "mall_order_logger")
@Data
public class OrderLogger implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long  id;
    private String operator;//操作人员
    private Date optime;
    private String operation;//操作名称
    private String remark;//操作备注
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

}
