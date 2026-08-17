package com.hdl.soar.module.pay.dal.postgres.refund;

import com.hdl.soar.module.pay.dal.entity.refund.PayRefundPO;
import com.hdl.soar.module.pay.enums.PayRefundStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayRefundRepository extends JpaRepository<PayRefundPO, Long>,
        JpaSpecificationExecutor<PayRefundPO> {

    /** Duplicate-creation guard: a merchant refund id is unique per app. */
    Optional<PayRefundPO> findByAppIdAndMerchantRefundId(Long appId, String merchantRefundId);

    /** Lookup on the notify/sync path: resolve a refund from the channel's app id + our refund no. */
    Optional<PayRefundPO> findByAppIdAndNo(Long appId, String no);

    /** In-flight guard: is there already a WAITING refund for this order? */
    long countByAppIdAndOrderIdAndStatus(Long appId, Long orderId, PayRefundStatusEnum status);

    /** Sync driver: WAITING refunds created within the reconcile window, oldest first, capped. */
    List<PayRefundPO> findTop200ByStatusAndCreateTimeGreaterThanEqualOrderByIdAsc(
            PayRefundStatusEnum status, Instant createTimeAfter);

    /**
     * CAS the refund to SUCCESS. Returns rows affected: {@code 0} means another path (a duplicate
     * sync/callback) already moved it, so the caller must treat it as a no-op, not an error.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PayRefundPO r
               SET r.status = :newStatus,
                   r.channelRefundNo = :channelRefundNo,
                   r.successTime = :successTime,
                   r.channelNotifyData = :channelNotifyData
             WHERE r.id = :id AND r.status = :expected
            """)
    int updateStatusToSuccess(@Param("id") Long id,
                              @Param("expected") PayRefundStatusEnum expected,
                              @Param("newStatus") PayRefundStatusEnum newStatus,
                              @Param("channelRefundNo") String channelRefundNo,
                              @Param("successTime") Instant successTime,
                              @Param("channelNotifyData") String channelNotifyData);

    /** CAS the refund to FAILURE. Same 0-means-already-moved contract as {@link #updateStatusToSuccess}. */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PayRefundPO r
               SET r.status = :newStatus,
                   r.channelRefundNo = :channelRefundNo,
                   r.channelNotifyData = :channelNotifyData,
                   r.channelErrorCode = :channelErrorCode,
                   r.channelErrorMsg = :channelErrorMsg
             WHERE r.id = :id AND r.status = :expected
            """)
    int updateStatusToFailure(@Param("id") Long id,
                              @Param("expected") PayRefundStatusEnum expected,
                              @Param("newStatus") PayRefundStatusEnum newStatus,
                              @Param("channelRefundNo") String channelRefundNo,
                              @Param("channelNotifyData") String channelNotifyData,
                              @Param("channelErrorCode") String channelErrorCode,
                              @Param("channelErrorMsg") String channelErrorMsg);

}
