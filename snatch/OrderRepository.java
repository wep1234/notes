package com.maihe.cms.repository.mall;

import com.maihe.cms.model.entity.fans.MemberWatch;
import com.maihe.cms.model.entity.mall.Goods;
import com.maihe.cms.model.entity.mall.Order;
import com.maihe.cms.model.warpper.MineLineWarpper;
import com.maihe.cms.model.warpper.SnatchOrderWarpper;
import com.maihe.cms.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface OrderRepository extends BaseRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    default Order getOrderByOrderNo(String orderNo, boolean lock){
        LockModeType lockModeType = lock ? LockModeType.PESSIMISTIC_WRITE : LockModeType.NONE;
        String hql = "select o from Order as o where o.orderno=:orderno";
        TypedQuery query = createQuery(hql, Order.class).setParameter("orderno", orderNo).setLockMode(lockModeType);
        return singleResult(query);
    }

    default List<Long> batchSnatchHandler(final List<SnatchOrderWarpper> orderWarppers){
        final List<Long> oids = new ArrayList<Long>();
        getSession().doWork((conn) -> {
            PreparedStatement[] stmts = new PreparedStatement[MineLineWarpper.SQLS.length];
            for(int i = 0, size = orderWarppers.size(); i < size; ++i){
                final SnatchOrderWarpper warpper = orderWarppers.get(i);
                PreparedStatement stmt = stmts[warpper.type];
                if(stmt == null){
                    stmts[warpper.type] = stmt = conn.prepareStatement(warpper.getSql(),Statement.RETURN_GENERATED_KEYS);
                }
                warpper.setValues(stmt);
            }

            for(final PreparedStatement stmt: stmts){
                if(stmt != null){
                    stmt.executeBatch();
                    ResultSet rs = stmt.getGeneratedKeys();
                    while(rs.next()) {
                        oids.add(rs.getLong(1));
                    }
                }
            }
        });
        return oids;
    };

    default void batchHandler(List<SnatchOrderWarpper> warppers){
        getSession().doWork((conn) -> {
            PreparedStatement[] stmts = new PreparedStatement[SnatchOrderWarpper.SQLS.length];
            for(int i = 0, size = warppers.size(); i < size; ++i){
                final SnatchOrderWarpper warpper = warppers.get(i);
                PreparedStatement stmt = stmts[warpper.type];
                if(stmt == null){
                    stmts[warpper.type] = stmt = conn.prepareStatement(warpper.getSql());
                }
                warpper.setValues(stmt);
            }

            for(final PreparedStatement stmt: stmts){
                if(stmt != null){
                    stmt.executeBatch();
                }
            }
        });
    };

    default Order getOrderById(Long oid,boolean lock){
        LockModeType lockModeType = lock ? LockModeType.PESSIMISTIC_WRITE : LockModeType.NONE;
        String hql = "select o from Order as o where o.id=:oid";
        TypedQuery query = createQuery(hql, Order.class).setParameter("oid", oid).setLockMode(lockModeType);
        return singleResult(query);
    }

    @Query("select o from Order o join fetch o.store s where o.id=:id")
    public Order findOrderInfo(@Param("id")Long id);

    @Query(value = "select o from Order o join fetch o.member m join fetch o.store s left join fetch s.member join fetch o.area " +
            "where m.id=:id and o.closed='c' order by o.orderTime desc ",
            countQuery = "select count(o) from  Order o left join o.member m where m.id=:id and o.closed='c' ")
    Page<Order> findMyAllOrder(Pageable pageable, @Param("id") Long id);

    @Query(value = "from Order o join fetch o.member m join fetch o.store s left join fetch s.member join fetch o.area " +
            "where m.id=:id and o.status=:type and o.closed='c' order by o.orderTime desc ",
            countQuery = "select count(o) from  Order o left join o.member m where m.id=:id and o.status=:type and o.closed='c' ")
    Page<Order> findOneTypeOrder(Pageable pageable, @Param("id") Long id, @Param("type") Character type);

    @Query("select count(o) from Order o where o.status='N' and o.closed='c' and o.orderTime<:overTime")
    int findNumOfOverTimeOrder(@Param("overTime")Date overTime);

    @Query("select o from Order o where o.status='N' and o.closed='c' and o.orderTime<:overTime ")
    Page<Order> findOverTimeOrder(Pageable pageable,@Param("overTime") Date overTime);

    @Modifying
    @Query(value="update mall_order set status='U',close_time=:now where id in :oids", nativeQuery = true)
    void batchCloseOrder(@Param("oids")List<Long> oids,@Param("now")Date now);

    @Query("select o from Order o where o.status='N' and o.closed='c' and o.orderTime<:overTime")
    List<Order> findOverTimeOrders(@Param("overTime")Date overTime);

    @Query("select o from Order o where o.status='s' and o.closed='c' and o.postTime<:confirmDate")
    List<Order> findShippedOrder(@Param("confirmDate")Date confirmDate);

    @Query("select count(o) from Order o where o.status='s' and o.closed='c' and o.postTime<:littleDate")
    int countShipperOrderByStatus(@Param("littleDate")Date littleDate);

    @Query("select o from Order o where o.status='s' and o.closed='c' and o.postTime<:confirmDate")
    Page<Order> findShippedOrder(Pageable pageable,@Param("confirmDate") Date confirmDate);

    @Query("select o from  Order o where o.status <>'U' and o.status <>'C' and o.id in (select oi.order.id from OrderItem oi where oi.goods.id =:gid)")
    List<Order> findOrderByGoodsIdNoSuccess(@Param("gid")Long gid);

    /**
     * 根据订单id查询订单以及下单会员，收件地区，订单日志
     * @param id
     * @return
     */
    @Query("select o from Order o join fetch o.member left join fetch o.orderLoggers left join fetch o.area a left join fetch a.parent ap left join fetch " +
            "ap.parent where o.id = :id")
    Order findOrderWithMemberAndAreaAndOrderLoggers(@Param("id") Long id);

    @Query(value = "from Order o join fetch o.member m join fetch o.store s join fetch s.member sm join fetch o.area " +
            "where sm.id=:id and o.status=:type and o.closed='c' and o.id<:lstMsgId order by o.orderTime desc ",
            countQuery = "select count(o) from  Order o left join o.store s left join s.member sm where sm.id=:id and o.id<:lstMsgId and o.status=:type and o.closed='c' ")
    Page<Order> findStoreOrderByType(Pageable pageable,@Param("id") Long id,@Param("type") char type,@Param("lstMsgId")Long lstMsgId);

    @Query(value = "from Order o join fetch o.member m join fetch o.store s join fetch s.member sm join fetch o.area " +
            "where sm.id=:id and o.closed='c' and o.id<:lstMsgId order by o.orderTime desc ",
            countQuery = "select count(o) from  Order o left join o.store s left join s.member sm where sm.id=:id and o.closed='c' and o.id<:lstMsgId ")
    Page<Order> findAllStoreOrder(Pageable pageable,@Param("id") Long id,@Param("lstMsgId")Long lstMsgId);

    @Query("select o from Order o left join fetch o.member left join fetch o.orderItems oi left join fetch oi.goods g " +
            "left join fetch g.store s left join fetch s.member where o.id=:orderId")
    Order findOrderWithMemberAndOrderItemsAndGoodsAndStoreAndMemberById(@Param("orderId") Long orderId);

    @Query(value = "select o.*, m.* from mall_order o left join fans_member m on o.member_id = m.id where o" +
            ".payment_status = " +
            ":paymentStatus and (o.pay_style = :alipay or o.pay_style = :wechat) and if(:startTime is null, 1=1, " +
            ":startTime <= o.payment_time) and if(:endTime is null, 1=1, o.payment_time < :endTime)", nativeQuery =
            true)
    List<Order> findPaidOrdersByPaymentStatusAndPayStyle(@Param("paymentStatus")Character paymentStatus, @Param
            ("alipay")Character alipay, @Param("wechat")Character wechat, @Param("startTime")Date startTime, @Param
            ("endTime")Date endTime);
}

