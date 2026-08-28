package com.hdl.soar.module.pay.dal.postgres.order;

import com.hdl.soar.module.pay.dal.entity.order.PayOrderExtensionPO;
import com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum;
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
public interface PayOrderExtensionRepository extends JpaRepository<PayOrderExtensionPO, Long>,
        JpaSpecificationExecutor<PayOrderExtensionPO> {

    Optional<PayOrderExtensionPO> findByNo(String no);

    List<PayOrderExtensionPO> findAllByOrderId(Long orderId);

    /** Recent WAITING attempts, oldest first, capped — the sync job's driving query (global). */
    List<PayOrderExtensionPO> findTop200ByStatusAndCreateTimeGreaterThanEqualOrderByIdAsc(
            PayOrderStatusEnum status, Instant createTime);

    /** All attempts of an order — used by the expire job to re-check before closing. */
    List<PayOrderExtensionPO> findByOrderId(Long orderId);

    /**
     * Compare-and-swap the extension to SUCCESS. See
     * {@link PayOrderRepository#updateStatusToSuccess} for why the {@code AND status = :expected}
     * clause makes this safe against duplicate callbacks.
     *
     * @return number of rows updated (1 on success, 0 if no longer in {@code expected})
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PayOrderExtensionPO e
               SET e.status = :newStatus,
                   e.channelNotifyData = :channelNotifyData
             WHERE e.id = :id
               AND e.status = :expected
            """)
    int updateStatusToSuccess(@Param("id") Long id,
                              @Param("expected") PayOrderStatusEnum expected,
                              @Param("newStatus") PayOrderStatusEnum newStatus,
                              @Param("channelNotifyData") String channelNotifyData);

    /**
     * Compare-and-swap the extension to CLOSED.
     *
     * @return number of rows updated (1 on success, 0 if no longer in {@code expected})
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PayOrderExtensionPO e
               SET e.status = :newStatus,
                   e.channelNotifyData = :channelNotifyData,
                   e.channelErrorCode = :channelErrorCode,
                   e.channelErrorMsg = :channelErrorMsg
             WHERE e.id = :id
               AND e.status = :expected
            """)
    int updateStatusToClosed(@Param("id") Long id,
                             @Param("expected") PayOrderStatusEnum expected,
                             @Param("newStatus") PayOrderStatusEnum newStatus,
                             @Param("channelNotifyData") String channelNotifyData,
                             @Param("channelErrorCode") String channelErrorCode,
                             @Param("channelErrorMsg") String channelErrorMsg);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PayOrderExtensionPO e
               SET e.status = :newStatus
             WHERE e.id = :id
               AND e.status = :expected
            """)
    int updateStatusToClosed(@Param("id") Long id,
                             @Param("expected") PayOrderStatusEnum expected,
                             @Param("newStatus") PayOrderStatusEnum newStatus);

}
