package com.maihe.cms.app.service.support;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.maihe.cms.core.config.AlipayConfig;
import com.maihe.cms.core.config.WxpayConfig;
import com.maihe.cms.core.utils.EncodingUtil;
import com.maihe.cms.core.utils.HttpUtils;
import com.maihe.cms.core.utils.MD5Util;
import com.maihe.cms.core.utils.WxPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 *     秒杀异步服务。
 * </p>
 * @since 2018-10-24
 * @author pzp
 */
@Slf4j
@Component
public class SnatchAsyncService {

    private static final String ORDER_TYPE_COMMON = "1";
    private static final String CHARACTER_ENCODING = "UTF-8";
    private static final String URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    public SnatchAsyncService(){

    }

    @Async("ioTaskExecutor")
    public void complete(SnatchRequest request){
        //处理支付逻辑
        Map<String, Object> map = new HashMap<String, Object>(8);
        final Character payStyle = request.payStyle;
        final Long oid = request.orderId;
        final String ip = request.ip;
        final String cause = request.cause;
        final String orderno = request.orderno;
        final BigDecimal total = request.total;
        if(SnatchRequest.SUSSCUSE_CAUSE.equals(cause)){
            if(payStyle.equals(SnatchRequest.PAY_ALI)){
                AlipayClient alipayClient = AlipayConfig.getClient();
                AlipayTradeAppPayRequest aliRequest = new AlipayTradeAppPayRequest();
                AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
                model.setSubject("【LUCKYGO】" + orderno);
                model.setOutTradeNo(orderno);
                model.setTotalAmount(String.valueOf(total));
                model.setProductCode("QUICK_MSECURITY_PAY");
                model.setPassbackParams(EncodingUtil.encode(AlipayConfig.GOODS_ORDER_TYPE, AlipayConfig.getCHARSET()));
                aliRequest.setBizModel(model);
                aliRequest.setNotifyUrl(AlipayConfig.getNotifyUrl());
                try {
                    AlipayTradeAppPayResponse response = alipayClient.sdkExecute(aliRequest);
                    final String body = response.getBody();
                    log.info("alipay response body = {}",body);
                    map.put("ret",0);
                    map.put("oid",oid);
                    map.put("msg",body);
                } catch (AlipayApiException e) {
                    log.error("{}",e);
                    map.put("ret",1);
                    map.put("msg",e.getMessage());
                }
            }else if(payStyle.equals(SnatchRequest.PAY_WX)){
                WxpayConfig wxConfig = new WxpayConfig();
                wxConfig.setNonceStr(WxPayUtil.getRandomStringByLength(31));
                wxConfig.setSpbillCreateIp(ip);
                wxConfig.setOutTradeNo(orderno);
                wxConfig.setAttach(WxpayConfig.GOODS_ORDER_TYPE);
                wxConfig.setTotalFee(total.multiply(new BigDecimal("100")).intValue());
                wxConfig.setBody("LUCKYGO-商品购买");
                String sendXml="";
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
                    String returnCode = wmap.get("return_code");
                    String returnMsg  = wmap.get("return_msg");
                    log.debug("returnCode :{} ,returnMsg :{}", returnCode,returnMsg);
                    if (returnCode.equals("SUCCESS")) {
                        String resultCode = wmap.get("result_code");
                        String errCodeDes = wmap.get("err_code_des");
                        log.debug("resultCode :{} ,errCodeDes :{}", resultCode,errCodeDes);
                        if(resultCode.equals("SUCCESS")){
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
                                sbuf.append(i==0?"":'&')
                                        .append(e.getKey()).append('=').append(e.getValue());
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
                            map.put("oid",oid);
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
            }else if(payStyle.equals(SnatchRequest.PAY_GOLD)){
                map.put("ret",0);
                map.put("oid",oid);
            }else{
                map.put("ret",3);
                map.put("msg","请选择正确的支付方式");
            }
        }else{
            map.put("ret",1);
            map.put("msg",cause);
        }
        request.setMap(map);
        request.complete();
    }

}
