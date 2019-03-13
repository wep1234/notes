package com.maihe.cms.model.entity.mall;

import com.maihe.cms.model.entity.fans.Artist;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "mall_goods")
@Data
@DynamicUpdate
public class Goods implements Serializable {
    private static final long serialVersionUID = -260456341839653750L;

    //已入库（已删除）
    public static final Character STATUS_INBOUND = 'i';
    //已下架
    public static final Character STATUS_OFF_SHELF = 'S';
    //已上架
    public static final Character STATUS_SHELF = 's';
    //尾货（已删除）
    public static final Character STATUS_LEFTOVER = 'L';
    //已删除
    public static final Character STATUS_DELETED = 'D';
    public static final Character FREE_SHIPPING ='Y';//包邮
    public static final Character NO_FREE_SHIPPING ='N';//免邮
    public static final Character HOT_YES = 'Y';
    public static final Character HOT_NO = 'N';
    public static final Character TOP_YES = 'Y';
    public static final Character TOP_NO = 'N';
    public static final Character EDIT_STATUS_YES = 'Y';
    public static final Character EDIT_STATUS_NO = 'N';
    public static final Character ALLOW_GOLD = 'Y';
    public static final Character NOT_ALLOW_GOLD = 'N';
    public static final String DEFAULT_UNIT = "件";

    /** 审核通过 */
    public static final char REVIEW_PASS = 'Y';

    /** 审核不通过*/
    public static final char REVIEW_UNPASS = 'N';

    /** 未审核 */
    public static final char UNREVIEWED = 'U';

    private Long id;
    private String name;
    private String images;              //商品图片列表：即商品图片URL列表，URL由英文分号“;”分隔
    private String details;             //商品详情
    private BigDecimal price;           //商品价格
    private BigDecimal goldPrice;       //金米粒购买价格
    private BigDecimal   originalPrice; //商品原价：默认同price，字段暂留
    private Character status;           //商品状态：i - inbound 已入库，S - off Shelf 已下架， s - shelf 已上架，L - leftover 尾货， D -  deleted 已删除
    private String sno;                 //货号
    private Date createTime;
    private Date offshelfTime;          //下架时间
    private Date shelfTime;             //上架时间

    private Date closeTime;             //关闭时间
    private String offshelfReason;    //下架理由
    private Integer sales;              //销量
    private Long favors;                //收藏量
    private  Integer quantity;          //库存量
    private Integer tipoffs;            //举报数，暂留
    private Integer clicks;             //点击量（浏览量）
    private  String qrcode;         //商品二维码
    private String description;     //商品描述
    private  String unit;           //商品单位
    private String specImage;       //格图片，暂留
    private String otherSpecs;      //规格：其他，暂留
    private Integer ordersn;        //排序号，暂留
    private Character hot;          //是否是热销推荐：Y：是；N：否
    private Date hoteTime;          //热销推荐时间
    private Character top;          //是否置顶：Y：置顶；N：不置顶
    private Date topTime;           //置顶时间
    private Character editStatus;//编辑状态：Y - 已编辑， N - 未编辑
    private Date chnltopTime;//商品频道：置顶时间
    private String animimgMaster;//动画主图：主路径
    private String animimgSlaves;//动画主图：从路径后缀，“;”分隔
    private Integer dailySupply;//日供货量
    private Character freeShipping;//是否免邮：Y：免邮；N：不免邮
    private Integer weight;
    private Character allowGold;//是否可用金米粒兑换
    /** 审核状态：Y-通过，N-不通过，U-为审核 */
    private Character reviewStatus = REVIEW_PASS;

    private Store store;
    private GoodsPlatformCategory goodsPlatformCategory;
    private Artist artist;//所属艺人

    private List<GoodsReview> goodsReviews;

    private boolean mustGold = false; // 是否只能用金米粒购买

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column
    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    @Column
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Column
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Column
    public BigDecimal getGoldPrice() {
        return goldPrice;
    }

    public void setGoldPrice(BigDecimal goldPrice) {
        this.goldPrice = goldPrice;
    }

    @Column(name = "original_price")
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    @Column
    public Character getStatus() {
        return status;
    }

    public void setStatus(Character status) {
        this.status = status;
    }

    @Column
    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    @Column
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getOffshelfTime() {
        return offshelfTime;
    }

    public void setOffshelfTime(Date offshelfTime) {
        this.offshelfTime = offshelfTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getShelfTime() {
        return shelfTime;
    }

    public void setShelfTime(Date shelfTime) {
        this.shelfTime = shelfTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    @Column
    public String getOffshelfReason() {
        return offshelfReason;
    }

    public void setOffshelfReason(String offshelfReason) {
        this.offshelfReason = offshelfReason;
    }

    @Column
    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    @Column
    public Long getFavors() {
        return favors;
    }

    public void setFavors(Long favors) {
        this.favors = favors;
    }

    @Column
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Column
    public Integer getTipoffs() {
        return tipoffs;
    }

    public void setTipoffs(Integer tipoffs) {
        this.tipoffs = tipoffs;
    }

    @Column
    public Integer getClicks() {
        return clicks;
    }

    public void setClicks(Integer clicks) {
        this.clicks = clicks;
    }

    @Column
    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    @Column
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Column
    public String getSpecImage() {
        return specImage;
    }

    public void setSpecImage(String specImage) {
        this.specImage = specImage;
    }

    @Column
    public String getOtherSpecs() {
        return otherSpecs;
    }

    public void setOtherSpecs(String otherSpecs) {
        this.otherSpecs = otherSpecs;
    }

    @Column
    public Integer getOrdersn() {
        return ordersn;
    }

    public void setOrdersn(Integer ordersn) {
        this.ordersn = ordersn;
    }

    @Column
    public Character getHot() {
        return hot;
    }

    public void setHot(Character hot) {
        this.hot = hot;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getHoteTime() {
        return hoteTime;
    }

    public void setHoteTime(Date hoteTime) {
        this.hoteTime = hoteTime;
    }

    @Column
    public Character getTop() {
        return top;
    }

    public void setTop(Character top) {
        this.top = top;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getTopTime() {
        return topTime;
    }

    public void setTopTime(Date topTime) {
        this.topTime = topTime;
    }

    @Column
    public Character getEditStatus() {
        return editStatus;
    }

    public void setEditStatus(Character editStatus) {
        this.editStatus = editStatus;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getChnltopTime() {
        return chnltopTime;
    }

    public void setChnltopTime(Date chnltopTime) {
        this.chnltopTime = chnltopTime;
    }

    @Column
    public String getAnimimgMaster() {
        return animimgMaster;
    }

    public void setAnimimgMaster(String animimgMaster) {
        this.animimgMaster = animimgMaster;
    }

    @Column
    public String getAnimimgSlaves() {
        return animimgSlaves;
    }

    public void setAnimimgSlaves(String animimgSlaves) {
        this.animimgSlaves = animimgSlaves;
    }

    @Column
    public Integer getDailySupply() {
        return dailySupply;
    }

    public void setDailySupply(Integer dailySupply) {
        this.dailySupply = dailySupply;
    }

    @Column
    public Character getFreeShipping() {
        return freeShipping;
    }

    public void setFreeShipping(Character freeShipping) {
        this.freeShipping = freeShipping;
    }

    @Column
    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Column
    public Character getAllowGold() {
        return allowGold;
    }

    public void setAllowGold(Character allowGold) {
        this.allowGold = allowGold;
    }

    @Column(name = "review_status",nullable = false)
    public Character getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Character reviewStatus) {
        this.reviewStatus = reviewStatus;
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
    @JoinColumn(name = "cate_id", nullable = false)
    public GoodsPlatformCategory getGoodsPlatformCategory() {
        return goodsPlatformCategory;
    }

    public void setGoodsPlatformCategory(GoodsPlatformCategory goodsPlatformCategory) {
        this.goodsPlatformCategory = goodsPlatformCategory;
    }

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    @OneToMany(mappedBy = "goods")
    public List<GoodsReview> getGoodsReviews() {
        return goodsReviews;
    }

    public void setGoodsReviews(List<GoodsReview> goodsReviews) {
        this.goodsReviews = goodsReviews;
    }

    @Transient
    public String getFirstImage() {
        final String imgs = images;
        if (StringUtils.isNotEmpty(imgs)) {
            final String[] imga = imgs.split(";");
            if(imga.length>0){
                return imga[0];
            }else{
                return "";
            }
        }
        return "";
    }

    /**
     * 剩余数量（我的收藏中使用）
     */
    private Integer surplus;

    /**
     * 结束时间（我的收藏中使用）
     */
    private Date endTime;


    /**
     * 商品类型（我的收藏中使用）
     */
    private char goodsType;

    /**
     * 秒杀商品的id（我的收藏中使用）
     */
    private Long snatchId;

	@Transient
	public Integer getSurplus() {
		return surplus;
	}

	public void setSurplus(Integer surplus) {
		this.surplus = surplus;
	}

	@Transient
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Transient
	public char getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(char goodsType) {
		this.goodsType = goodsType;
	}

    @Transient
    public Long getSnatchId() {
        return snatchId;
    }

    public void setSnatchId(Long snatchId) {
        this.snatchId = snatchId;
    }

    @Transient
    public String getFreeText(){
        if(freeShipping==null){
            return "不包邮";
        }else if(FREE_SHIPPING.equals(freeShipping)){
            return "包邮";
        }else{
            return "不包邮";
        }
    }

    @Transient
    public String getHotText(){
        if(hot==null){
            return "不推荐";
        }else if(HOT_YES.equals(hot)){
            return "推荐";
        }else{
            return "不推荐";
        }
    }

    // 获取商品图片
    @Transient
    public List<String> getImageList() {
        final String imgs = images;
        if (StringUtils.isNotEmpty(imgs)) {
            final String[] imga = imgs.split(";");
            final int size = imga.length;
            final List<String> ilist = new ArrayList<String>(size);
            for (int i = 0; i < size; i++) {
                ilist.add(imga[i]);
            }
            return ilist;
        }
        return null;
    }

    /**
     * 获得状态文本
     * @return
     */
    @Transient
    public String getStatusText() {
        switch (status) {
            case 'S':
                return "已下架";
            case 's':
                return "已上架";
            case 'D':
                return "已删除";
            default:
                return "状态异常";
        }
    }

    @Transient
    public Boolean getShowGold(){
        if(Store.DEFAULT_STORE_ID.equals(store.getId())||NOT_ALLOW_GOLD.equals(allowGold)){
            return false;
        }else{
            return true;
        }
    }

    @Transient
    public String getReviewStatusName() {
        if(reviewStatus == REVIEW_PASS){
            return "通过";
        }
        if(reviewStatus == REVIEW_UNPASS){
            return "不通过";
        }
        return "未审核";
    }

    @Transient
    public String getReviewOperation() {
        if(reviewStatus == UNREVIEWED){
            return "审核";
        }
        return "查看详情";
    }

    @Transient
    public String getReviewRemark(){
        if(goodsReviews == null || goodsReviews.size() <= 0){
            return "";
        }
        GoodsReview goodsReview = goodsReviews.get(0);
        if(StringUtils.isBlank(goodsReview.getRemark())){
            return "";
        }
        return goodsReview.getRemark();
    }

    @Transient
    public String getFmtShelfTime(){
        if(shelfTime == null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(shelfTime);
    }

    @Transient
    public String getFmtOffshelfTime(){
        if(offshelfTime == null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(offshelfTime);
    }

    @Transient
    public boolean isMustGold() {
        return mustGold;
    }

    public void setMustGold(boolean mustGold) {
        this.mustGold = mustGold;
    }
}
