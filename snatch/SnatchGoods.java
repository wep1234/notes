package com.maihe.cms.model.entity.mall;

import com.maihe.cms.core.utils.TimeUtil;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "mall_snatch_goods")
@Data
public class SnatchGoods implements Serializable {
    private static final long serialVersionUID = -7429525174094109601L;

    public static final Character STATUS_OFF_SHELF = 'S';
    public static final Character STATUS_SHELF = 's';
    public static final Character STATUS_DELETED = 'D';

    private Long id;
    private Character status;//商品状态：i - inbound 已入库，S - off Shelf 已下架， s - shelf 已上架，L - leftover 尾货， D -  deleted 已删除
    private Integer quantity;//秒杀总量 暂不使用
    private Integer saled;//已抢数量 暂不使用
    private Integer buyLimit;
    private BigDecimal snatchPrice;//商品价格
    private Date snatchBegin;//秒杀抢购开始时间
    private Date snatchEnd;//秒杀抢购结束时间
    private Date createTime;

    private Goods goods;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Character getStatus() {
        return status;
    }

    public void setStatus(Character status) {
        this.status = status;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSaled() {
        return saled;
    }

    public void setSaled(Integer saled) {
        this.saled = saled;
    }

    public Integer getBuyLimit() {
        return buyLimit;
    }

    public void setBuyLimit(Integer buyLimit) {
        this.buyLimit = buyLimit;
    }

    public BigDecimal getSnatchPrice() {
        return snatchPrice;
    }

    public void setSnatchPrice(BigDecimal snatchPrice) {
        this.snatchPrice = snatchPrice;
    }

    public Date getSnatchBegin() {
        return snatchBegin;
    }

    public void setSnatchBegin(Date snatchBegin) {
        this.snatchBegin = snatchBegin;
    }

    public Date getSnatchEnd() {
        return snatchEnd;
    }

    public void setSnatchEnd(Date snatchEnd) {
        this.snatchEnd = snatchEnd;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @ManyToOne
    @JoinColumn(name = "goods_id",nullable = false)
    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    @Transient
    public String getStatusText() {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 'S':
                return "已下架";
            case 's':
                return "已上架";
            case 'D':
                return "已删除";
            default:
                return null;
        }
    }

    @Transient
    public String getSnatchTimeRange(){
        if (snatchBegin == null || snatchEnd == null) {
            return "";
        }
        return (TimeUtil.getDateString(snatchBegin) + " ~ " + TimeUtil.getDateString(snatchEnd));
    }

    @Transient
    public int getPercent(){
        int sales = goods.getSales();
        return sales*100/quantity;
    }
}