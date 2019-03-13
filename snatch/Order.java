package com.maihe.cms.model.entity.mall;

import com.maihe.cms.core.modular.entity.Area;
import com.maihe.cms.core.utils.BufferPool;
import com.maihe.cms.core.utils.TimeUtil;
import com.maihe.cms.model.entity.fans.DeliveryAddress;
import com.maihe.cms.model.entity.fans.Member;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name = "mall_order")
@Data
public class Order implements Serializable {
    private static final long serialVersionUID = -8959827111450985323L;

    public static final char PAY_GOLD ='G';
    public static final char STATUS_ALL = 'A';
    public static final char STATUS_NOPAY = 'N';// 待付款
    public static final char STATUS_PAY = 'P';// 已付款
    public static final char STATUS_SHIPPED = 's';// 已发货
    public static final char STATUS_DELIVERED = 'D';// 已送达
    public static final char STATUS_SUCCESS = 'S';// 交易成功
    public static final char STATUS_UNSUCCESS = 'U';// 交易未成功
    public static final char STATUS_CANCELED = 'C';// 已取消
    public static final char UNCLOSED = 'c';// 未关闭
    public static final char CLOSED = 'C';// 已关闭

    public static final char PAYMENT_STATUS_WAIT = 'N';// 待支付
    public static final char PAYMENT_STATUS_PAY = 'P';// 已支付

    public static final char TYPE_COMMON = 'C';//普通订单
    public static final char TYPE_SNATCH = 'S';//秒杀订单
    public static final char TYPE_GROUP = 'G';//团购订单
    public static final char TYPE_PROCUREMENT = 'P';//拼单订单
    /** 银米粒订单 */
    public static final char TYPE_SILVER = 'R';
    public static final char ORDER_DEFAULT = 'N';


    public static final char PAY_STYLE_ALIPAY = '1';//1-支付宝支付
    public static final char PAY_STYLE_WX = '2';//2-APP微信支付
    public static final char PAY_STYLE_GOLD = '3';//3-金米粒支付
    /** 银米粒支付 */
    public static final char PAY_RICE_SILVER = '4';

    /**
     * 未申请退款
     */
    public static final Character REFUND_NOT = 'N';
    /**
     * 已申请退款
     */
    public static final Character REFUND_HAVE_SUBMITTED = 'H';
    /**
     * 同意退款
     */
    public static final Character REFUND_AGREE = 'A';
    /**
     * 不同意退款
     */
    public static final Character REFUND_DISAGREE = 'D';

    public static final char LOAN_STATUS ='S';//已放款
    public static final char NO_LOAN_STATUS ='N';//未放款

    private Long id;
    private String orderno;
    private Character status;//订单状态：N - Not paid 待付款，P - Paid 已付款， s - shipped 已发货， D - Delivered 已送达， S - Success 交易成功， U - Unsccess 交易未成功， C - Canceled 已取消
    private Character closed;//关闭标识：c - 未关闭， C - 已关闭
    private Character type;//订单类型: C-普通订单 S-秒杀订单 G - 团购订单 R-银米粒订单
    private Date orderTime;
    private Character paymentStatus;//订单支付状态：N - 待支付， P - 已支付
    private Date paymentTime;
    private BigDecimal total;//订单总额：实付款￥
    private BigDecimal paidAmount;//已付总额：已付款￥，暂留，默认为实付款
    private BigDecimal amount;//商品金额，商品实际应付金额
    private Date postTime;  //发货时间，物流追踪，暂留
    private String trackno; //物流单号，物流追踪，暂留
    private BigDecimal freight;//运费￥
    private String receiver; //收货人：姓名， copy 选择的地址信息
    private String phone;
    private String address;
    private String bapaysCode;//余额支付编码：null表示不使用余额支付， 暂留
    private BigDecimal bapaysAmount;//余额支付金额
    private String thrpaysName; //第三方支付方式名称
    private BigDecimal thrpaysAmount;   //使用第三方支付的金额
    private String memberNote;      //会员留言
    private Character deliveryType;//发货方式，暂留
    private Date takeTime;      //收货时间
    private String revcontact;  //收货人的联系人, 暂留
    private Date closeTime;     //成交时间
    private String agent;
    private Character transType;
    private String agentname;
    private Integer seqno;      //付款批次
    private Character payStyle;//支付方式：1-农行支付 ，2-银联支付， 3-微信支付
    private Character loanStatus;//放款状态:S - 已放款，N-未放款
    private Character refund;//退款状态，暂留  ：N - 未申请退款；H - 已申请退款；A - 同意退款; D - 不同意退款
    private Character sendOrder;//N：未发送；H：已发送
    private Character isDealay;//是否延时付款 : Y - 延时，N - 不延时
    private String reason;          //订单取消、拒绝合同原因
    private String remark;          //订单后台备注
    private Character smallSupply; //订单是否微供：N -不是，Y-微供
    private String trackphoto;      //物流发货图，暂留
    private String delayLoanReason;//延时放款原因， 暂留
    private String delayLoanPic; //延时放款图片，暂留
    private Character sendMes; //是否发过短信 Y:发过 N:没发过
    private String deliveryName;//快递公司

    /**
     * 下单会员
     */
    private Member member;
    private Store store;
    private Area area;
    /**
     * 订单项
     */
    private List<OrderItem> orderItems;
    /**
     *  订单日志
     */
    private List<OrderLogger> orderLoggers;
    /**
     *  非持久化放款记录
     */
    private LoanOrder tempLoanOrder;
    // 非持久化订单项 - 便于批量查询 @since 2018-11-07 pzp
    private List<OrderItem> tmpItems;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public Character getStatus() {
        return status;
    }

    public void setStatus(Character status) {
        this.status = status;
    }

    public Character getClosed() {
        return closed;
    }

    public void setClosed(Character closed) {
        this.closed = closed;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public Character getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Character paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Date getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Date paymentTime) {
        this.paymentTime = paymentTime;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public String getTrackno() {
        return trackno;
    }

    public void setTrackno(String trackno) {
        this.trackno = trackno;
    }

    public BigDecimal getFreight() {
        return freight;
    }

    public void setFreight(BigDecimal freight) {
        this.freight = freight;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBapaysCode() {
        return bapaysCode;
    }

    public void setBapaysCode(String bapaysCode) {
        this.bapaysCode = bapaysCode;
    }

    public String getThrpaysName() {
        return thrpaysName;
    }

    public void setThrpaysName(String thrpaysName) {
        this.thrpaysName = thrpaysName;
    }

    public BigDecimal getThrpaysAmount() {
        return thrpaysAmount;
    }

    public void setThrpaysAmount(BigDecimal thrpaysAmount) {
        this.thrpaysAmount = thrpaysAmount;
    }

    public String getMemberNote() {
        return memberNote;
    }

    public void setMemberNote(String memberNote) {
        this.memberNote = memberNote;
    }

    public Character getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(Character deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Date getTakeTime() {
        return takeTime;
    }

    public void setTakeTime(Date takeTime) {
        this.takeTime = takeTime;
    }

    public String getRevcontact() {
        return revcontact;
    }

    public void setRevcontact(String revcontact) {
        this.revcontact = revcontact;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public Character getTransType() {
        return transType;
    }

    public void setTransType(Character transType) {
        this.transType = transType;
    }

    public String getAgentname() {
        return agentname;
    }

    public void setAgentname(String agentname) {
        this.agentname = agentname;
    }

    public Integer getSeqno() {
        return seqno;
    }

    public void setSeqno(Integer seqno) {
        this.seqno = seqno;
    }

    public Character getPayStyle() {
        return payStyle;
    }

    public void setPayStyle(Character payStyle) {
        this.payStyle = payStyle;
    }

    public Character getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(Character loanStatus) {
        this.loanStatus = loanStatus;
    }

    public Character getRefund() {
        return refund;
    }

    public void setRefund(Character refund) {
        this.refund = refund;
    }

    public Character getSendOrder() {
        return sendOrder;
    }

    public void setSendOrder(Character sendOrder) {
        this.sendOrder = sendOrder;
    }

    public Character getIsDealay() {
        return isDealay;
    }

    public void setIsDealay(Character isDealay) {
        this.isDealay = isDealay;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Character getSmallSupply() {
        return smallSupply;
    }

    public void setSmallSupply(Character smallSupply) {
        this.smallSupply = smallSupply;
    }

    public String getTrackphoto() {
        return trackphoto;
    }

    public void setTrackphoto(String trackphoto) {
        this.trackphoto = trackphoto;
    }

    public String getDelayLoanReason() {
        return delayLoanReason;
    }

    public void setDelayLoanReason(String delayLoanReason) {
        this.delayLoanReason = delayLoanReason;
    }

    public String getDelayLoanPic() {
        return delayLoanPic;
    }

    public void setDelayLoanPic(String delayLoanPic) {
        this.delayLoanPic = delayLoanPic;
    }

    public Character getSendMes() {
        return sendMes;
    }

    public void setSendMes(Character sendMes) {
        this.sendMes = sendMes;
    }

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }


    @ManyToOne
    @JoinColumn(name = "area_id", nullable = false)
    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public BigDecimal getBapaysAmount() {
        return bapaysAmount;
    }

    public void setBapaysAmount(BigDecimal bapaysAmount) {
        this.bapaysAmount = bapaysAmount;
    }

    public Character getType() {
        return type;
    }

    public void setType(Character type) {
        this.type = type;
    }

    @OneToMany(mappedBy = "order")
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @OneToMany(mappedBy = "order")
    public List<OrderLogger> getOrderLoggers() {
        return orderLoggers;
    }

    public void setOrderLoggers(List<OrderLogger> orderLoggers) {
        this.orderLoggers = orderLoggers;
    }

    @Transient
    public LoanOrder getTempLoanOrder() {
        return tempLoanOrder;
    }

    public void setTempLoanOrder(LoanOrder tempLoanOrder) {
        this.tempLoanOrder = tempLoanOrder;
    }

    public Order addTmpItem(OrderItem item) {
        if(this.tmpItems == null){
            this.tmpItems = new ArrayList<>(2);
        }
        this.tmpItems.add(item);
        return this;
    }

    @Transient
    public List<OrderItem> getTmpItems() {
        return this.tmpItems;
    }

    public String getDeliveryName() {
        return deliveryName;
    }

    public void setDeliveryName(String deliveryName) {
        this.deliveryName = deliveryName;
    }

    @Transient
    public Date getDeadLine(){
        Date createtime = orderTime;
        Date dateLine = TimeUtil.getDateAfterHour(createtime,2);
        return dateLine;
    }

    @Transient
    public String getSellName(){
        return store.getShortTitle();
    }

    @Transient
    public String getBuyerName(){
        return member.getNickname();
    }

    @Transient
    public String getPayStyleText(){
        if(payStyle==null){
            return "";
        }else{
            switch (payStyle){
                case PAY_STYLE_ALIPAY:
                    return "支付宝";
                case PAY_STYLE_WX:
                    return "微信";
                case PAY_STYLE_GOLD:
                    return "金米粒";
                case PAY_RICE_SILVER:
                    return "银米粒";
                default:
                    return "";
            }
        }
    }

    @Transient
    public static final boolean isValidType(Character type){
        if (type == null) {
            return false;
        }
        switch (type) {
            case 'C':
            case 'S':
            case 'G':
            case 'R':
            case 'P':
                return true;
            default:
                return false;
        }
    }

    @Transient
    public String getTypeText(){
        switch (type) {
            case 'C':
                return "普通订单";
            case 'S':
                return "秒杀订单";
            case 'G':
                return "团购订单";
            case 'R':
                return "银米粒兑换订单";
            case 'P':
                return "代购拼单订单";
            default:
                return "类型异常";
        }
    }

    @Transient
    public String getAreaText(){
        StringBuilder sb = BufferPool.getStringBuffer();
        sb.append(area.getAreaName());
        Area parent = area.getParent();
        if (parent != null) {
            String areaName = parent.getAreaName();
            boolean needIgnore = areaName.equals("市辖区") || areaName.equals("县");
            if (!needIgnore) {
                sb.insert(0," ").insert(0, parent.getAreaName());
            }
            Area grandParent = parent.getParent();
            if (grandParent != null) {
                sb.insert(0, " ").insert(0, grandParent.getAreaName());
            }
        }
        String result = sb.toString();
        BufferPool.release(sb);
        return result;
    }

    @Transient
    public String getRefundText(){
        switch (refund) {
            case 'N':
                return "未申请退款";
            case 'H':
                return "已申请退款";
            case 'A':
                return "同意退款";
            case 'D':
                return "不同意退款";
            default:
                return "退款状态异常";
        }
    }

    @Transient
    public String getLoanStatusText(){
        switch (loanStatus) {
            case 'N':
                return "未放款";
            case 'S':
                return "已放款";
            default:
                return "";
        }
    }

    @Transient
    public String getOrderTimeText(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.orderTime);
    }

    @Transient
    public String getStatusText(){
        if(status==STATUS_UNSUCCESS){
            return "交易失败";
        }else if(status == STATUS_NOPAY){
            return "未付款";
        }else if(status == STATUS_PAY){
            return "待发货";
        }else if(status == STATUS_SHIPPED){
            return "待收货";
        }else if(status == STATUS_SUCCESS){
            return "交易成功";
        }else{
            return "交易取消";
        }

    }

    /**
     * 0 人民币 1 金米粒 2 银米粒
     * @return
     */
    @Transient
    public int getPayUnit(){
        if(PAYMENT_STATUS_PAY==paymentStatus){
            if(Order.PAY_STYLE_GOLD==payStyle){
                return 1;
            }else if(Order.PAY_RICE_SILVER==payStyle){
                return 2;
            }else{
                return 0;
            }
        }else{
            if(Store.DEFAULT_STORE_ID.equals(store.getId())){
                return 0;
            }else{
                int payUnit = 0;
                if(tmpItems!=null&&tmpItems.size()>0){
                    OrderItem item = tmpItems.get(0);
                    if(Goods.ALLOW_GOLD.equals(item.getGoods().getAllowGold())){
                        payUnit = 1;
                    }
                }
                return payUnit;
            }
        }
    }

    @Transient
    public String getTotalText(){
        int payUnit = getPayUnit();
        if(payUnit==0){
            if(new BigDecimal(total.intValue()).compareTo(total)==0) {
                return "¥"+total.intValue();
            }else{
                return "¥"+total;
            }
        }else if(payUnit==1){
            if(new BigDecimal(total.intValue()).compareTo(total)==0) {
                return total.intValue()+"金米粒";
            }else{
                return total+"金米粒";
            }
        }else{
            if(new BigDecimal(total.intValue()).compareTo(total)==0) {
                return total.intValue()+"银米粒";
            }else{
                return total+"银米粒";
            }
        }
    }
}
