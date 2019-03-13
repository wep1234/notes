package com.maihe.cms.model.entity.fans;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author: zwj
 * @since: 2018-11-14
 */
@Entity
@Table(name = "fans_member_account")
@Data
@DynamicUpdate
public class MemberAccount implements Serializable {
    private static final long serialVersionUID = -4282475525077862003L;

    @Id
    @GeneratedValue(generator = "assigned")
    @GenericGenerator(name = "assigned", strategy = "assigned")
    private Long id;

    @Column(nullable = false)
    private BigDecimal totalIntegral = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal usedIntegral = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal availIntegral = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal freezeIntegral = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal totalGold = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal usedGold = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal availGold = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal freezeGold = BigDecimal.ZERO;

    /**
     * 非持久化会员
     */
    @Transient
    private Member tempMember;

}
