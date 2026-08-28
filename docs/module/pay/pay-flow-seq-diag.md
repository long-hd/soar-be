Luồng thanh toán thật (cổng async kiểu VNPay) tách **3 pha**: tạo đơn → submit kênh → IPN settle + notify merchant.

## Happy path (VNPay / cổng async)

```mermaid
sequenceDiagram
    autonumber
    actor Merchant as Merchant<br/>(module nghiệp vụ)
    participant Api as PayOrderApi
    participant Svc as PayOrderService
    participant Redis as Redis<br/>(PayNoRedisDAO)
    participant DB as DB
    participant Client as PayClient<br/>(VNPay)
    actor User as User / Browser
    participant Rail as Cổng thanh toán
    participant CB as PayChannelCallbackController
    participant Outbox as PayNotifyService

    Note over Merchant,Outbox: Pha 1 — create: chưa chọn kênh, PayOrder.no = null

    Merchant->>Api: createOrder(appKey, merchantOrderId, price, …)
    Api->>Svc: createOrder
    Svc->>DB: validApp(appKey)
    Svc->>DB: findByAppIdAndMerchantOrderId
    alt Đã tồn tại (idempotent)
        Svc-->>Merchant: orderId cũ
    else Đơn mới
        Svc->>DB: INSERT pay_order WAITING<br/>notifyUrl ← app.orderNotifyUrl
        Svc-->>Merchant: orderId mới
    end

    Note over User,Client: Pha 2 — submit: chọn kênh, sinh no, gọi rail

    User->>Svc: POST /pay/order/submit<br/>{id, channelCode, returnUrl}
    Svc->>DB: order WAITING? chưa hết hạn?<br/>chưa có extension SUCCESS?
    Svc->>DB: validChannel(appId, channelCode)
    Svc->>Redis: generate("P") → no
    Svc->>DB: INSERT pay_order_extension WAITING<br/>no = PyyyyMMddHHmmssN
    Svc->>Client: unifiedOrder(outTradeNo=extension.no,<br/>notifyUrl=/pay/notify/order/{channelId})
    Client->>Client: ký + ghép redirect URL (không HTTP outbound)
    Client-->>Svc: WAITING + displayContent (URL)
    Note right of Svc: notifyOrder với WAITING = no-op
    Svc-->>User: status WAITING + URL thanh toán

    User->>Rail: mở URL, thanh toán
    Rail-->>User: redirect returnUrl (chỉ UI)

    Note over Rail,Outbox: Pha 3 — IPN: settle theo no, copy no lên order, outbox notify

    Rail->>CB: IPN GET/POST /pay/notify/order/{channelId}
    CB->>Client: parseOrderNotify (verify HMAC)
    Client-->>CB: SUCCESS + outTradeNo + channelOrderNo
    CB->>Svc: notifyOrder(channelId, notify)
    Svc->>DB: load channel @TenantIgnore → chạy trong tenant của channel
    Svc->>DB: CAS extension WAITING → SUCCESS<br/>match by no = outTradeNo
    Svc->>DB: CAS order WAITING → SUCCESS<br/>copy extensionId, no, channelOrderNo, fee
    Svc->>Outbox: createPayNotifyTask (cùng transaction)
    Outbox->>DB: INSERT pay_notify_task WAITING

    Note over Outbox,Merchant: afterCommit — fast-path; PayNotifyJob là backstop

    Outbox->>Merchant: POST notifyUrl<br/>{merchantOrderId, payOrderId}
    Merchant->>Api: getOrder(payOrderId) — nguồn sự thật
    Merchant-->>Outbox: CommonResult.code = 0
    Outbox->>DB: task SUCCESS
```

Điểm quan trọng trên diagram:

- **`merchant_order_id`** đi từ merchant lúc create, và được echo lại lúc notify.
- **`no`** sinh lúc submit, gửi rail như `outTradeNo`; chỉ được copy lên `PayOrder` khi CAS SUCCESS.
- Callback **không mang tenant-id**; tenant lấy từ channel rồi mới settle.

## Mock (SUCCESS ngay lúc submit)

`MockPayClient.unifiedOrder` trả `SUCCESS` luôn → bước 3 chạy **trong cùng request submit**, không cần IPN.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Svc as PayOrderService
    participant Client as MockPayClient
    participant DB
    participant Outbox as PayNotifyService
    actor Merchant

    User->>Svc: submit(channelCode=mock)
    Svc->>DB: INSERT extension WAITING
    Svc->>Client: unifiedOrder
    Client-->>Svc: SUCCESS ngay
    Svc->>DB: CAS extension + order → SUCCESS
    Svc->>Outbox: createPayNotifyTask
    Outbox->>Merchant: POST notifyUrl
    Svc-->>User: status SUCCESS
```

## Recovery nếu IPN rơi / đơn hết hạn

```mermaid
sequenceDiagram
    autonumber
    participant Sync as PayOrderSyncJob
    participant Expire as PayOrderExpireJob
    participant Svc as PayOrderService
    participant Client as PayClient
    participant DB
    participant NotifyJob as PayNotifyJob
    participant Outbox as PayNotifyService
    actor Merchant

    Note over Sync,Client: Sync (~60s): chỉ kéo SUCCESS, không đóng đơn

    Sync->>Svc: syncOrder()
    Svc->>DB: 200 extension WAITING (còn mới)
    Svc->>Client: getOrder(outTradeNo=no)
    alt Rail đã SUCCESS
        Svc->>Svc: notifyOrder → CAS + outbox (idempotent với IPN)
    else Vẫn WAITING / CLOSED
        Svc-->>Sync: bỏ qua, đợi IPN hoặc expire
    end

    Note over Expire,DB: Expire (~5 phút): hỏi rail lần cuối rồi mới đóng

    Expire->>Svc: expireOrder()
    Svc->>DB: 200 order WAITING đã quá expireTime
    loop mỗi extension WAITING
        Svc->>Client: getOrder(no)
        alt Rail SUCCESS
            Svc->>Svc: notifyOrder — recover, không đóng
        end
    end
    opt Không attempt nào paid
        Svc->>DB: CAS order WAITING → CLOSED
        Svc->>DB: đóng mọi extension còn WAITING
    end

    Note over NotifyJob,Merchant: Notify retry: 9 lần, backoff 15s … 1h

    NotifyJob->>Outbox: executeNotify() (per tenant)
    Outbox->>Merchant: POST notifyUrl
    alt ack code=0
        Outbox->>DB: task SUCCESS
    else fail, còn lượt
        Outbox->>DB: nextNotifyTime += backoff
    else hết 9 lần
        Outbox->>DB: task FAILURE
    end
```

Merchant **không settle tiền từ body notify** — payload chỉ có `merchantOrderId` + `payOrderId`; phải gọi lại `PayOrderApi.getOrder` để lấy status/amount chính thức.


Đoạn này bắt đầu khi cổng gọi IPN. Browser lúc đó **đã** (hoặc sắp) về `returnUrl` — không nằm trong diagram này.

## Callback → settle → tạo 1 task → POST merchant

```mermaid
sequenceDiagram
    autonumber
    participant Rail as Cổng (VNPay)
    participant CB as PayChannelCallbackController<br/>GET/POST /pay/notify/order/{channelId}
    participant Client as PayClient
    participant Svc as PayOrderService.notifyOrder
    participant DB as DB
    participant Outbox as PayNotifyService
    participant Exec as Executor (sau commit)
    participant Lock as Redis lock per task
    actor Merchant as Merchant notifyUrl

    Rail->>CB: IPN (params/body, không có tenant-id)
    Note over CB: @PermitAll + @TenantIgnore

    CB->>Client: getPayClient(channelId)
    CB->>Client: parseOrderNotify (VNPay: verify HMAC)
    Client-->>CB: PayOrderChannelRespDTO<br/>status + outTradeNo(=no) + channelOrderNo + price

    CB->>Svc: notifyOrder(channelId, notify)

    Svc->>DB: TenantUtils.executeIgnore<br/>validChannel(channelId) → lấy tenantId
    Svc->>Svc: TenantUtils.execute(tenantId)<br/>notifyOrderInTransaction

    alt notify.status = SUCCESS
        Svc->>DB: findByNo(outTradeNo) → 1 extension
        Svc->>DB: CAS extension WAITING → SUCCESS
        Svc->>DB: findById(orderId) → 1 order
        Svc->>DB: so khớp price (nếu rail gửi)
        Svc->>DB: CAS order WAITING → SUCCESS<br/>copy extensionId, no, channelOrderNo, fee

        alt Đã SUCCESS bởi đúng extension này (IPN trùng)
            Note right of Svc: alreadyPaid — không tạo task
        else Lần đầu SUCCESS
            Svc->>Outbox: createPayNotifyTask(order)
            Outbox->>DB: INSERT 1 pay_notify_task WAITING<br/>(cùng transaction)
            Note over Svc,Outbox: commit: order SUCCESS + task cùng lúc
            Outbox->>Exec: afterCommit → executeNotify0(taskId)
        end

    else notify.status = CLOSED
        Svc->>DB: findByNo → CAS extension WAITING → CLOSED
        Note right of Svc: không đụng order, không tạo task

    else notify.status = WAITING
        Note right of Svc: no-op
    end

    Svc-->>CB: return
    CB-->>Rail: "success"

    Note over Exec,Merchant: HTTP gửi sau commit — không giữ TX

    Exec->>Lock: tryLock(taskId)
    alt Không lấy được lock (job đang gửi)
        Exec-->>Exec: skip
    else Lấy được lock
        Exec->>DB: load task, còn WAITING?
        Exec->>Merchant: POST notifyUrl<br/>{merchantOrderId, payOrderId}<br/>+ header tenant-id
        alt Merchant ack code=0
            Exec->>DB: task SUCCESS + 1 pay_notify_log
        else Fail / timeout
            Exec->>DB: notify_times++, next_notify_time += backoff<br/>+ 1 pay_notify_log FAILURE
        end
        Exec->>Lock: unlock
    end
```

## Nếu merchant chưa ack — job bù

```mermaid
sequenceDiagram
    autonumber
    participant Job as PayNotifyJob (~30–60s, per tenant)
    participant Outbox as PayNotifyService
    participant DB as DB
    participant Lock as Redis lock
    actor Merchant as Merchant notifyUrl

    Job->>Outbox: executeNotify()
    Outbox->>DB: tối đa 200 task WAITING<br/>next_notify_time ≤ now
    loop mỗi task (song song, có lock)
        Outbox->>Lock: tryLock(taskId)
        Outbox->>Merchant: POST lại cùng body
        alt ack OK
            Outbox->>DB: task SUCCESS
        else còn lượt (tối đa 9)
            Outbox->>DB: hẹn next_notify_time
        else hết 9 lần
            Outbox->>DB: task FAILURE
        end
    end
```

Luồng này **không** scan `pay_order`. Mỗi IPN: 1 `outTradeNo` → 1 extension → 1 order → tối đa **1** task (chỉ lần SUCCESS đầu). Job sau chỉ quét `pay_notify_task` đã đến hạn để gửi HTTP lại.

Luồng **một** `pay_notify_task`: tạo lúc CAS → gửi lần 1 → retry theo backoff.

```mermaid
sequenceDiagram
    autonumber
    participant Svc as PayOrderService<br/>(CAS SUCCESS)
    participant Outbox as PayNotifyService
    participant DB as DB
    participant Exec as Executor<br/>(afterCommit)
    participant Lock as Redis lock
    actor Merchant as Merchant notifyUrl
    participant Job as PayNotifyJob<br/>(~30–60s / tenant)

    Note over Svc,DB: Cùng TX với order SUCCESS — chưa gọi HTTP

    Svc->>Outbox: createPayNotifyTask(order)
    Outbox->>DB: INSERT pay_notify_task<br/>status=WAITING, times=0<br/>next_notify_time=now
    Outbox->>Exec: registerAfterCommit → executeNotify0(taskId)
    Note over Svc,DB: commit

    Note over Exec,Merchant: Lần 1 — ngay sau commit

    Exec->>Lock: tryLock(taskId)
    Exec->>DB: load task, còn WAITING?
    Exec->>Merchant: POST {merchantOrderId, payOrderId}

    alt Lần 1 ack code=0
        Exec->>DB: task SUCCESS, times=1
        Exec->>DB: INSERT log #1 SUCCESS
        Note right of DB: xong — job không nhặt task này
    else Lần 1 fail
        Exec->>DB: task vẫn WAITING, times=1<br/>next_notify_time = now+15s
        Exec->>DB: INSERT log #1 FAILURE
        Exec->>Lock: unlock
    end

    Note over Job,Merchant: Lần 2…9 — chỉ khi task còn WAITING và đến giờ

    loop Mỗi tick Quartz
        Job->>Outbox: executeNotify()
        Outbox->>DB: WAITING AND next_notify_time ≤ now<br/>(tối đa 200)
        alt Không có task đến hạn
            Outbox-->>Job: dispatched=0
        else Có task
            Outbox->>Lock: tryLock(taskId)
            alt Không lấy được lock (lần 1 đang gửi)
                Note right of Lock: skip
            else Lấy được
                Outbox->>Merchant: POST lại cùng body
                alt Ack code=0
                    Outbox->>DB: task SUCCESS
                    Outbox->>DB: INSERT log SUCCESS
                else Fail, times < 9
                    Outbox->>DB: times++<br/>next_notify_time = now + FREQUENCY[times-1]
                    Outbox->>DB: INSERT log FAILURE
                    Note right of DB: 15s, 15s, 30s, 3p, 30p×3, 1h
                else Fail, times = 9
                    Outbox->>DB: task FAILURE
                    Outbox->>DB: INSERT log FAILURE
                    Note right of DB: hết retry — cần người xử lý
                end
            end
        end
    end
```

Backoff nằm lúc ghi fail: `next_notify_time = now + NOTIFY_FREQUENCY[attempt - 1]`. Job không ngủ đúng 15s — nó hỏi định kỳ: task nào `next_notify_time` đã tới thì gửi.