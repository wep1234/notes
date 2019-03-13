package com.maihe.cms.model.entity.mall;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单项
 * @author wep
 * @since 2018/10/11
 */
@Entity
@Table(name = "mall_order_item")
@Data
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 3580551940907680802L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "goods_id", nullable = false)
    private Goods goods;

    @Column(nullable = false)
    private String name;

    //商品单价
    @Column(nullable = false)
    private BigDecimal price;

    //数量
    @Column(nullable = false)
    private Integer quantity;

    //图片
    @Column
    private String images;

    @Transient
    public BigDecimal getGprice(){
        return goods.getGoldPrice();
    }

    /**
     * 促销价格
     */
    @Transient
    private BigDecimal tempPromotionPrice;

    /**
     * 促销时间范围
     */
    @Transient
    private String tempPromotionTimeRange;

    @Transient
    public BigDecimal getTmpPrice(){
        return price;
    }

    @Transient
    public String getPriceText(){
        if(Store.DEFAULT_STORE_ID.equals(goods.getStore().getId())){
            if(Order.PAYMENT_STATUS_PAY==order.getPaymentStatus()){
                char payStyle = order.getPayStyle();
                if(Order.PAY_STYLE_GOLD==payStyle){
                    if(new BigDecimal(price.intValue()).compareTo(price)==0) {
                        return price.intValue()+"金米粒";
                    }else{
                        return price+"金米粒";
                    }
                }else if(Order.PAY_RICE_SILVER==payStyle){
                    if(new BigDecimal(price.intValue()).compareTo(price)==0) {
                        return price.intValue()+"银米粒";
                    }else{
                        return price+"银米粒";
                    }
                }else{
                    if(new BigDecimal(price.intValue()).compareTo(price)==0) {
                        return "¥"+price.intValue();
                    }else{
                        return "¥"+price;
                    }
                }
            }else{
                if(new BigDecimal(price.intValue()).compareTo(price)==0) {
                    return "¥"+price.intValue();
                }else{
                    return "¥"+price;
                }
            }
        }else{
            if(Goods.ALLOW_GOLD.equals(goods.getAllowGold())){
                return price+"金米粒";
            }else{
                return "¥"+price;
            }
        }
    }
}
