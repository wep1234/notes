package com.maihe.cms.model.warpper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 秒杀包装类
 * @author wep
 * @since 2018/10/25
 */
public class SnatchOrderWarpper {
    /**
     *  update `mall_snatch_goods` set status=? where id=?
     */
    public static final char SNATCH_STATUS_UPDATE = 0;
    /**
     *  update `mall_goods` set status=?,offshelf_time=? where id=?
     */
    public static final char GOODS_STATUS_UPDATE = 1;
    /**
     *  insert into `mall_order`
     *  (member_id,store_id,orderno,status,closed,order_time,payment_status,total,paid_amount,amount,freight,receiver,phone,area_id,address,bapays_amount,thrpays_amount,type)
     *  values (?,?,?,?,'c',?,'N',?,?,?,'0.00',?,?,?,?,'0.00','0.00','S')
     */
    public static final char ORDER_INSERT = 2;
    /**
     * INSERT INTO `mall_order_item`
     * (goods_id,order_id,name,price,quantity)
     * VALUES (?, ?, ?, ?, ?, ?);
     */
    public static final char ORDER_ITEM_INSERT = 3;
    /**
     * INSERT INTO `mall_order_logger`
     * (order_id,operator,optime,operation)
     * VALUES (?, ?, ?, ?);
     */
    public static final char ORDER_LOGGER_INSERT = 4;
    /**
     * update `mall_goods` set quantity=? where id=?
     */
    public static final char GOODS_QUANTITY_UPDATE = 5;

    /**
     * INSERT INTO `fans_gold_log`
     * (member_id,content,type,value,create_time,action)
     * VALUES (?, ?, ?, ?,?,?);
     */
    public static final char GOLD_LOGGER_INSERT = 6;

    /**
     *  update `fans_member` set used_gold=?,avail_gold=? where id=?
     */
    public static final char MEMBER_GOLD_UPDATE = 7;

    /**
     *  update `fans_gold_account` set order_freeze=? where id=?
     */
    public static final char PLAT_GOLD_ACCOUNT = 8;

    /**
     *  insert into fans_gold_account_record
     *      (account_id, admin_id, amount, balance, create_time, description, member_id, opt_type, option, option_id, remark, target)
     *  values
     *      (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
     */
    public static final char PLAT_GOLD_RECORD = 9;

    public static final String SQLS[] = {
            "update `mall_snatch_goods` set status=? where id=?",
            "update `mall_goods` set status=?,offshelf_time=? where id=?",
            "insert into `mall_order`" +
                    " (member_id,store_id,orderno,status,closed,order_time,payment_status,total,paid_amount,amount,freight,receiver,phone,area_id,address,bapays_amount,thrpays_amount,type,pay_style,payment_time)" +
                    "values (?,?,?,?,'c',?,?,?,?,?,'0.00',?,?,?,?,'0.00','0.00','S',?,?)",
            "INSERT INTO `mall_order_item` (goods_id,order_id,name,price,quantity,images) VALUES (?, ?, ?, ?, ?,?)",
            "INSERT INTO `mall_order_logger` (order_id,operator,optime,operation) VALUES (?, ?, ?, ?)",
            "update `mall_goods` set quantity=? where id=?",
            "INSERT INTO `fans_gold_log` (member_id,content,type,value,create_time,action) VALUES (?, ?, ?, ?,?,?)",
            "update `fans_member_account` set freeze_gold=?,avail_gold=? where id=?",
            "update `fans_gold_account` set order_freeze=? where id=?",
            "insert into `fans_gold_account_record` (account_id, admin_id, amount, balance, create_time, description, member_id, opt_type, operating, option_id, remark, target) " +
                    " values" +
                    " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    };

    public final char type;
    public final Object[] params;

    public SnatchOrderWarpper(char type, Object ... params){
        this.type = type;
        this.params = params;
    }

    public String getSql() {
        return SQLS[type];
    }

    public void setValues(PreparedStatement stmt)throws SQLException {
        for(int i = 0, size = params.length; i < size; ++i){
            stmt.setObject(i + 1, params[i]);
        }
        stmt.addBatch();
    }

    @Override
    public String toString() {
        final String f = getSql().replace("?", "%s");
        return String.format(f, params);
    }
}
