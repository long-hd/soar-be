package com.hdl.soar.module.pay.dal.postgres.order;

import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayOrderRepository extends JpaRepository<PayOrderPO, Long>,
        JpaSpecificationExecutor<PayOrderPO> {

    Optional<PayOrderPO> findByAppIdAndMerchantOrderId(Long appId, String merchantOrderId);

    /**
     * Compare-and-swap the order to SUCCESS.
     * <p>
     * The {@code AND o.status = :expected} clause makes this an atomic compare-and-swap: only the
     * caller that observes the order still in {@code expected} state wins. A return of {@code 0} means
     * another callback already moved it, so the caller must treat this as a duplicate rather than
     * paying twice. Bulk updates bypass the persistence context, so {@code clearAutomatically} clears
     * any stale managed copy.
     *
     * @return number of rows updated (1 on success, 0 if the order was no longer in {@code expected})
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PayOrderPO o
               SET o.status = :newStatus,
                   o.channelId = :channelId,
                   o.channelCode = :channelCode,
                   o.successTime = :successTime,
                   o.extensionId = :extensionId,
                   o.no = :no,
                   o.channelOrderNo = :channelOrderNo,
                   o.channelUserId = :channelUserId,
                   o.channelFeeRate = :channelFeeRate,
                   o.channelFeePrice = :channelFeePrice
             WHERE o.id = :id
               AND o.status = :expected
            """)
    int updateStatusToSuccess(@Param("id") Long id,
                              @Param("expected") PayOrderStatusEnum expected,
                              @Param("newStatus") PayOrderStatusEnum newStatus,
                              @Param("channelId") Long channelId,
                              @Param("channelCode") String channelCode,
                              @Param("successTime") Instant successTime,
                              @Param("extensionId") Long extensionId,
                              @Param("no") String no,
                              @Param("channelOrderNo") String channelOrderNo,
                              @Param("channelUserId") String channelUserId,
                              @Param("channelFeeRate") Double channelFeeRate,
                              @Param("channelFeePrice") BigDecimal channelFeePrice);

    /** WAITING orders past their expire time, oldest first, capped — the expire job's driving query. */
    List<PayOrderPO> findTop200ByStatusAndExpireTimeLessThanOrderByIdAsc(
            PayOrderStatusEnum status, Instant expireTime);

    /**
     * CAS-close a WAITING order (WAITING -> CLOSED). Returns rows affected: 0 means another path
     * already moved it (a late callback or reconcile), so the caller must not treat it as closed.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PayOrderPO o
               SET o.status = :newStatus
             WHERE o.id = :id AND o.status = :oldStatus
            """)
    int updateStatusToClosed(@Param("id") Long id,
                             @Param("oldStatus") PayOrderStatusEnum oldStatus,
                             @Param("newStatus") PayOrderStatusEnum newStatus);

}
