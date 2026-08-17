package com.hdl.soar.module.pay.service.notify;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.controller.admin.notify.dto.PayNotifyTaskPageReqDTO;
import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyLogPO;
import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyTaskPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.dal.entity.refund.PayRefundPO;

import java.util.List;

/**
 * Outbox notify service: enqueue a task when an order is paid, then relay it to the merchant reliably.
 */
public interface PayNotifyService {

    /**
     * Enqueue an outbox notify task for a just-paid order.
     * <p>
     * MUST be called inside the order-success transaction so the task row commits atomically with the
     * SUCCESS transition. The order is passed in (not re-fetched) to avoid a query and to keep this
     * service free of a dependency on {@code PayOrderService}.
     */
    void createPayNotifyTask(PayOrderPO order);

    /**
     * Enqueue an outbox notify task for a just-succeeded/failed refund. MUST be called inside the
     * refund transaction. The refund is passed in (not re-fetched), consistent with the order path.
     */
    void createPayNotifyTask(PayRefundPO refund);

    /**
     * Poll entry point (called by the per-tenant job): dispatch all due WAITING tasks for the current
     * tenant. Returns the number of tasks dispatched.
     */
    int executeNotify();

    // ---- query (admin) ----

    PayNotifyTaskPO getNotifyTask(Long id);

    PageResult<PayNotifyTaskPO> getNotifyTaskPage(PayNotifyTaskPageReqDTO pageReqDTO);

    List<PayNotifyLogPO> getNotifyLogList(Long taskId);

}
