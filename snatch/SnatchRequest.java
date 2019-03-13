package com.maihe.cms.app.service.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀请求
 * @author: wep
 * @since: 2018-10-24
 */
@Slf4j
public class SnatchRequest {
    public static final char PAY_WX = 'W';
    public static final char PAY_ALI = 'A';
    public static final char PAY_GOLD = 'G';
    public static final String SUSSCUSE_CAUSE ="下单成功";

    public final Long goodsId;//秒杀商品id
    public final Long memberId;//购买人
    public final Long addressId;//收货地址
    public final Integer num;//购买数量
    public final Character payStyle;//W:微信 A：支付宝 G：金米粒
    public final String ip;//ip 用于app微信支付
    public final ResponseBodyEmitter responseBodyEmitter;
    public volatile boolean timeout;
    public volatile boolean error;
    public volatile String cause;
    public volatile Long orderId;
    //相应的订单数据，用于支付
    public volatile String orderno;//订单
    public volatile BigDecimal total;//订单金额
    public volatile Map<String,Object> map;//用于返回数据

    public SnatchRequest(Long goodsId,Long memberId,Long addressId,Integer num,Character payStyle,String ip,ResponseBodyEmitter responseBodyEmitter) {
        this.goodsId = goodsId;
        this.addressId = addressId;
        this.memberId = memberId;
        this.num = num;
        this.payStyle = payStyle;
        this.ip = ip;
        this.responseBodyEmitter = responseBodyEmitter;
    }

    public void complete() {
        if(isTimeout()){
            return;
        }
        try {
            final Map<String,Object> map = this.map;
            responseBodyEmitter.send(map);
        }catch (Exception e) {
            log.debug("Snatch request complete error", e);
        } finally {
            responseBodyEmitter.complete();
        }
    }

    public boolean isTimeout(){
        return this.timeout;
    }

    public boolean hasError(){
        return this.error;
    }

    public final boolean isCancelled(){
        return (this.isTimeout() || this.hasError());
    }

    public void setCause(String cause){
        if(this.cause != null){
            return;
        }
        this.cause = cause;
    }

    public void setOrderId(Long orderId){
        if(this.orderId != null){
            return;
        }
        this.orderId = orderId;
    }

    @Override
    public String toString(){
        return (String.format("{goodsId: %d, memberId: %d,num:%d, timeout: %s }",
                goodsId, memberId,num, timeout));
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
