package com.maihe.cms.app.controller.mall;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.maihe.cms.comm.push.AppOrderPush;
import com.maihe.cms.core.config.AlipayConfig;
import com.maihe.cms.core.config.WxpayConfig;
import com.maihe.cms.app.controller.BaseAppController;
import com.maihe.cms.app.service.support.SnatchRequest;
import com.maihe.cms.app.service.support.SnatchSupportService;
import com.maihe.cms.core.shiro.ShiroKit;
import com.maihe.cms.core.support.HttpKit;
import com.maihe.cms.core.utils.EncodingUtil;
import com.maihe.cms.core.utils.HttpUtils;
import com.maihe.cms.core.utils.MD5Util;
import com.maihe.cms.core.utils.WxPayUtil;
import com.maihe.cms.exception.RuleViolatedException;
import com.maihe.cms.model.entity.mall.*;
import com.maihe.cms.service.mall.GroupBuyService;
import com.maihe.cms.service.mall.OrderItemService;
import com.maihe.cms.service.mall.OrderService;
import com.maihe.cms.service.mall.ProcurementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 支付
 *
 * @author: wep
 * @since: 2018/10/26
 */
@Slf4j
@RestController
@RequestMapping("/pay")
public class PayController extends BaseAppController {

    private static final long serialVersionUID = 1450887874695063938L;

    @Autowired
    private OrderService orderService;
    @Autowired
    private SnatchSupportService snatchSupportService;
    @Autowired
    private AppOrderPush appOrderPush;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private GroupBuyService groupBuyService;
    @Autowired
    private ProcurementService procurementService;

    private static final String URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    @PostMapping(path = VERSION_1000 + "/success/{orderId}/{pageSize}/{pageNum}" + URL_EXT)
    @ResponseBody
    public Map<String,Object> success(@PathVariable("orderId") Long orderId,
                                      @PathVariable("pageSize") Integer pageSize,
                                      @PathVariable("pageNum") Integer pageNum){
        Map<String, Object> map = new HashMap<String, Object>();
        List<Goods> goods = orderService.guessLike(orderId,pageSize,pageNum);
        map.put("ret",0);
        if(goods == null || goods.size() <= 0){
            map.put("goodsList","[]");
        }else{
            map.put("goodsList",warp(goods,"id","name","firstImage","price","sales","hot"));
        }
        return map;
    }

    /**
     * type =1 已生成订单
     * type =2 未生成订单
     * @param jsonObject
     * @return
     */
    @PostMapping(path =VERSION_1000 +  "/build" + URL_EXT)
    public Map<String, Object> build(@RequestBody JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<String, Object>(8);
        String type=jsonObject.get("type").toString();
        char payStyle = jsonObject.get("payStyle").toString().toCharArray()[0];
        final Long memberId = ShiroKit.getMemberId();
        if(memberId==null){
            map.put("ret",2);
            map.put("msg","尚未登录");
        }else {
            BigDecimal payMoney = BigDecimal.ZERO;
            Order order = null;
            if (TYPE_HASORDER.equals(type)) {
                try {
                    Long oid = Long.valueOf(jsonObject.get("oid").toString());
                    order = orderService.findById(oid);
                    if (order == null) {
                        map.put("ret", 1);
                        map.put("msg", "出现未知错误");
                        return map;
                    }
                    if (!memberId.equals(order.getMember().getId())) {
                        map.put("ret", 1);
                        map.put("msg", "不能操作不是你的订单");
                        return map;
                    }
                    if(SnatchRequest.PAY_GOLD == payStyle){
                        Order goldOrder = orderService.payOrderByGold(oid);
                        appOrderPush.orderinPush(goldOrder.getId());
                    }
                } catch (RuleViolatedException e) {
                    map.put("ret", 1);
                    map.put("msg", e.getMessage());
                    return map;
                }
            } else if (TYPE_NOORDER.equals(type)) {
                try {
                    Long gid = Long.valueOf(jsonObject.get("gid").toString());
                    int num = Integer.parseInt(jsonObject.get("num").toString());
                    Long addressId = Long.valueOf(jsonObject.get("aid").toString());
                    order = orderService.createOrder(memberId, gid, num, addressId, payStyle);
                    if(SnatchRequest.PAY_GOLD == payStyle){
                        appOrderPush.orderinPush(order.getId());
                    }
                } catch (NumberFormatException e) {
                    map.put("ret", 1);
                    map.put("msg", "出现未知错误");
                    return map;
                } catch (RuleViolatedException e) {
                    map.put("ret", 1);
                    map.put("msg", e.getMessage());
                    return map;
                }
            } else {
                map.put("ret", 1);
                map.put("msg", "出现未知错误");
                return map;
            }
            if(SnatchRequest.PAY_ALI == payStyle){
                map = aliPay(order,map);
            }else if(SnatchRequest.PAY_WX == payStyle){
                map = wxPay(order,map);
            }else{
                map.put("ret",0);
                map.put("oid",order.getId());
            }
        }
        return map;
    }

    private void checkGoodsNum(Order order) {
        if (order.getType() == Order.TYPE_GROUP) {// 团购订单类型
            List<OrderItem> orderItems = orderItemService.findItemListByOrderId(order.getId());
            if (orderItems.size() > 0) {
                for (OrderItem orderItem : orderItems) {
                    GroupBuy groupBuy = groupBuyService.findByGoodsId(
                            orderItem.getGoods().getId());// 通过订单项中的商品id得到团购商品
                    if (groupBuy == null) {
						log.error("不存在的团购商品");
                        throw new RuleViolatedException("不存在的团购商品");
                    }
                    int gbQuantity = groupBuy.getQuantity();// 团购总量
                    int saled = groupBuy.getSaled();// 已团数量
                    int num = orderItem.getQuantity();// 会员购买数量
                    if (num > (gbQuantity - saled)) {
						log.error("库存不足，无法支付");
                        throw new RuleViolatedException("库存不足，无法支付");
                    }
                }
            }
        } else if (order.getType() == Order.TYPE_PROCUREMENT) {// 拼单订单类型
            List<OrderItem> orderItems = orderItemService.findByOrderId(order.getId());// 通过订单id得到订单项
            if (orderItems.size() > 0) {
                for (OrderItem orderItem : orderItems){
                    Procurement procurement = procurementService.findGoodsBygid(
                            orderItem.getGoods().getId());// 通过订单项中的商品id得到拼单商品
                    if (procurement == null) {
						log.error("不存在的拼单商品");
                        throw new RuleViolatedException("不存在的拼单商品");
                    }
                    int num = orderItem.getQuantity();// 会员购买数量
                    int alreadyNumber = procurement.getAlreadyNumber();// 已拼数量
                    int totalNumber = procurement.getTotalNumber();// 拼单总量
                    if (num > (totalNumber - alreadyNumber)) {
						log.error("库存不足，无法支付");
                        throw new RuleViolatedException("库存不足，无法支付");
                    }
                }
            }
        }
    }

    private Map<String,Object> wxPay(Order order, Map<String,Object> map) {
        try {
            checkGoodsNum(order);
        } catch (RuleViolatedException e) {
            map.put("ret",1);
            map.put("msg", e.getMessage());
            return map;
        }
        WxpayConfig wxConfig = new WxpayConfig();
        wxConfig.setNonceStr(WxPayUtil.getRandomStringByLength(31));
        wxConfig.setSpbillCreateIp(HttpKit.getIp());
        /**
         * 处理订单 orderno|HHmmss  暂时先不处理订单号
         */
        String orderSn=order.getOrderno();
        //SimpleDateFormat formatDateTime = new SimpleDateFormat("HHmmss");Date date = new Date();
        //String payTimestamp = formatDateTime.format(date);orderSn=orderSn+"|"+payTimestamp;
        BigDecimal payMoney= order.getTotal();
        wxConfig.setOutTradeNo(orderSn);
        wxConfig.setAttach(WxpayConfig.GOODS_ORDER_TYPE);
        wxConfig.setTotalFee(payMoney.multiply(new BigDecimal("100")).intValue());
        wxConfig.setBody("LUCKYGO-商品购买");
        String sendXml;
        // 2.获取要发送的数据
        wxConfig.setSign(wxConfig.makeAppUnifiedOrderSign());
        sendXml = wxConfig.generateAppUnifiedOrderXMLContent();
        log.debug("send xml :{}", sendXml);
        // 3.向微信服务器发送数据并获取返回值
        // String resXml = WxPayUtil.postData(URL,sendXml);
        String resXml = HttpUtils.PostXml(URL, sendXml, CHARACTER_ENCODING);
        log.debug("response xml :{}", resXml);
        if (StringUtils.isEmpty(resXml)) {
            map.put("ret",1);
            map.put("msg","App微信统一下单失败");
        }else{
            // 4.解析xml为map
            Map<String, String> wmap = WxPayUtil.doXMLParse(resXml);
            //5.根据微信服务器返回的结果进行业务的处理
            String returnCode = wmap.get("return_code"), returnMsg  = wmap.get("return_msg");
            log.debug("returnCode :{} ,returnMsg :{}", returnCode,returnMsg);
            if (RETURN_SUCCESS.equals(returnCode)) {
                String resultCode = wmap.get("result_code"),errCodeDes = wmap.get("err_code_des");
                log.debug("resultCode :{} ,errCodeDes :{}", resultCode,errCodeDes);
                if(RETURN_SUCCESS.equals(resultCode)){
                    final String pack = "Sign=WXPay";
                    Map<String, String> message = new HashMap<String, String>();
                    String appId    = wmap.get("appid");
                    String partnerId= wmap.get("mch_id");
                    String prepayId = wmap.get("prepay_id");
                    String nonceStr = wmap.get("nonce_str");
                    long timeStamp  = (System.currentTimeMillis())/1000;
                    final SortedMap<String, String> params = new TreeMap<String, String>();
                    params.put("appid", appId);
                    params.put("partnerid", partnerId);
                    params.put("prepayid", prepayId);
                    params.put("package", pack);
                    params.put("noncestr", nonceStr);
                    params.put("timestamp", String.valueOf(timeStamp));
                    final StringBuilder sbuf = new StringBuilder();
                    final Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();
                    for(int i = 0; itr.hasNext(); itr.remove(), ++i){
                        final Map.Entry<String, String> e = itr.next();
                        sbuf.append(i==0?"":'&').append(e.getKey()).append('=').append(e.getValue());
                    }
                    sbuf.append('&').append("key").append('=').append(WxpayConfig.getKey());
                    final String ctt = sbuf.toString();
                    final String paySign  = (MD5Util.MD5Encode(ctt,"UTF-8").toUpperCase());
                    message.put("appId", appId);
                    message.put("timeStamp", String.valueOf(timeStamp));
                    message.put("noncestr", nonceStr);
                    message.put("partnerid", partnerId);
                    message.put("prepayid", prepayId);
                    message.put("package", pack);
                    message.put("sign", paySign);
                    map.put("ret",0);
                    map.put("oid",order.getId());
                    map.put("msg",message);
                }else{
                    map.put("ret",1);
                    map.put("msg",errCodeDes);
                }
            } else {
                map.put("ret",1);
                map.put("msg",returnMsg);
            }
        }
        return map;
    }


    /**
     * 秒杀订单
     * gid:商品编号
     * aid:收货地址
     * num:数量
     * @return
     */
    @PostMapping(VERSION_1000 + "/snatch/{gid}/{aid}/{num}/{payStyle}")
    public ResponseBodyEmitter snatchPay(@PathVariable("gid") Long gid, @PathVariable("aid") Long aid,
                                         @PathVariable("num") int num, @PathVariable("payStyle") Character payStyle)
            throws IOException {
        final ResponseBodyEmitter responseBodyEmitter = new ResponseBodyEmitter(30000L);
        final Long memberId = ShiroKit.getMemberId();
        if(memberId == null){
            try {
                responseBodyEmitter.send(tip(2,"尚未登录",null));
            } finally {
                responseBodyEmitter.complete();
            }
        }
        final SnatchRequest request =
                new SnatchRequest(gid, memberId, aid, num, payStyle,HttpKit.getIp(), responseBodyEmitter);
        responseBodyEmitter.onError((t)->{
            request.error = true;
            // can't complete when error
        });
        responseBodyEmitter.onTimeout(()->{
            request.timeout = true;
            // 完成emitter
            try {
                responseBodyEmitter.send(errorTip("排队已超时"));
            }catch (final Exception e){
                // ignore
            }finally {
                responseBodyEmitter.complete();
            }
        });
        if(!snatchSupportService.offer(request)){
            try{
                responseBodyEmitter.send(errorTip("排队已满"));
            }finally {
                responseBodyEmitter.complete();
            }
        }
        return responseBodyEmitter;
    }

    /**
     * 团购订单
     * @param gid  商品id
     * @param aid  收货地址id
     * @param num  购买数量
     * @param payStyle  支付方式
     * @return
     */
    @PostMapping(VERSION_1000 + "/groupBuy/{gid}/{aid}/{num}/{payStyle}")
    public Map groupBuyPay(@PathVariable("gid") Long gid, @PathVariable("aid") Long aid, @PathVariable("num") int num,
                                         @PathVariable("payStyle") Character payStyle) {
        Map<String, Object> map = new HashMap<>();
        final Long memberId = ShiroKit.getMemberId();
        Order order = null;
        try {
            order = orderService.createGroupBuyOrder(memberId, gid, aid, num, payStyle);
        }catch (RuleViolatedException e) {
            map.put("ret", 1);
            map.put("msg", e.getMessage());
            return map;
        }
        if(SnatchRequest.PAY_ALI == payStyle){// 支付宝支付
            map = aliPay(order, map);
        }else if(SnatchRequest.PAY_WX == payStyle){// 微信支付
            map = wxPay(order, map);
        }else{
            map.put("ret", 0);
            map.put("oid", order.getId());
        }
        return map;
    }

    /**
     * 代购拼单订单
     * @param gid  商品id
     * @param aid  收货地址id
     * @param num  购买数量
     * @param payStyle  支付方式
     * @return
     */
    @PostMapping(VERSION_1000 + "/procurement/{gid}/{aid}/{num}/{payStyle}")
    public Map procurementPay(@PathVariable("gid") Long gid, @PathVariable("aid") Long aid, @PathVariable("num") int num,
                           @PathVariable("payStyle") Character payStyle) {
        Map<String, Object> map = new HashMap<>();
        final Long memberId = ShiroKit.getMemberId();
        Order order = null;
        try {
            order = orderService.createProcurementOrder(memberId, gid, aid, num, payStyle);
        }catch (RuleViolatedException e) {
            map.put("ret", 1);
            map.put("msg", e.getMessage());
            return map;
        }
        if(SnatchRequest.PAY_ALI == payStyle){// 支付宝支付
            map = aliPay(order, map);
        }else if(SnatchRequest.PAY_WX == payStyle){// 微信支付
            map = wxPay(order, map);
        }else{
            map.put("ret", 0);
            map.put("oid", order.getId());
        }
        return map;
    }

    private Map<String,Object> aliPay(Order order, Map<String,Object> map) {
        try {
            checkGoodsNum(order);
        } catch (RuleViolatedException e) {
            map.put("ret",1);
            map.put("msg", e.getMessage());
            return map;
        }
        //获得初始化的AlipayClient
        AlipayClient alipayClient = AlipayConfig.getClient();
        String orderno=order.getOrderno();
        BigDecimal payMoney= order.getTotal();
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setSubject("【LUCKYGO】" + orderno);
        model.setOutTradeNo(orderno);
        model.setTotalAmount(String.valueOf(payMoney));
        model.setProductCode("QUICK_MSECURITY_PAY");
        model.setPassbackParams(EncodingUtil.encode(AlipayConfig.GOODS_ORDER_TYPE, AlipayConfig.getCHARSET()));
        /**
         * 1表示普通订单
         */
        request.setBizModel(model);
        request.setNotifyUrl(AlipayConfig.getNotifyUrl());
        try {
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            final String body = response.getBody();
            log.info("alipay response body = {}",body);
            map.put("ret",0);
            map.put("oid",order.getId());
            map.put("msg",body);
        } catch (AlipayApiException e) {
            log.error("{}",e);
            map.put("ret",1);
            map.put("msg",e.getMessage());
        }
        return  map;
    }

}
