package com.hdl.soar.module.pay.framework.pay.core.client.impl.vnpay;

import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.PayOrderGetReqDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderChannelRespDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.exception.PayClientException;
import com.hdl.soar.module.pay.framework.pay.core.client.impl.AbstractPayClient;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * VNPay client (rail: VNPay redirect gateway).
 * <p>
 * {@code unifiedOrder} builds a signed redirect URL — no outbound HTTP. The user's browser is sent to
 * that URL, pays, and VNPay: (1) redirects the browser back to {@code vnp_ReturnUrl}, and (2) calls
 * this gateway's IPN endpoint (configured per terminal in the VNPay merchant portal — not sent per
 * request) which lands on {@code parseOrderNotify}. Success requires a valid signature plus
 * {@code vnp_ResponseCode == "00"} and {@code vnp_TransactionStatus == "00"}.
 */
@Slf4j
public class VnpayPayClient extends AbstractPayClient<VnpayPayClientConfig> {

    private static final String VERSION = "2.1.0";
    private static final String COMMAND = "pay";
    private static final String ORDER_TYPE = "other";
    private static final String CURR_CODE = "VND";
    private static final String LOCALE = "vn";
    private static final String SUCCESS_CODE = "00";
    private static final String QUERY_COMMAND = "querydr";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private HttpClient httpClient;

    public VnpayPayClient(Long channelId, VnpayPayClientConfig config) {
        super(channelId, config);
    }

    @Override
    protected void doInit() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // never hang the reconcile loop on a slow channel
                .build();
    }

    @Override
    protected PayOrderChannelRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Throwable {
        // VNPay amount is the VND amount multiplied by 100
        long amount = reqDTO.getPrice().multiply(BigDecimal.valueOf(100)).longValueExact();

        // Sorted (TreeMap) so the signature is built over ascending keys, as VNPay requires
        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", VERSION);
        params.put("vnp_Command", COMMAND);
        params.put("vnp_TmnCode", config.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", CURR_CODE);
        params.put("vnp_TxnRef", reqDTO.getOutTradeNo());
        params.put("vnp_OrderInfo", reqDTO.getSubject());
        params.put("vnp_OrderType", ORDER_TYPE);
        params.put("vnp_Locale", LOCALE);
        params.put("vnp_ReturnUrl", reqDTO.getReturnUrl());
        params.put("vnp_IpAddr", reqDTO.getUserIp());
        params.put("vnp_CreateDate", LocalDateTime.now(ZONE).format(DATE_FORMAT));
        if (reqDTO.getExpireTime() != null) {
            params.put("vnp_ExpireDate", LocalDateTime.ofInstant(reqDTO.getExpireTime(), ZONE).format(DATE_FORMAT));
        }

        String hashData = buildHashData(params);
        String query = buildQuery(params);
        String secureHash = hmacSHA512(config.getHashSecret(), hashData);
        String payUrl = config.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;

        // WAITING: the user must be redirected to payUrl; success arrives later via the callback
        return PayOrderChannelRespDTO.waitingOf("url", payUrl, reqDTO.getOutTradeNo(), null);
    }

    @Override
    protected PayOrderChannelRespDTO doParseOrderNotify(Map<String, String> params, String body,
                                                        Map<String, String> headers) throws Throwable {
        // 1. Verify the signature: recompute over all params except the hash fields
        String receivedHash = params.get("vnp_SecureHash");
        TreeMap<String, String> signed = new TreeMap<>(params);
        signed.remove("vnp_SecureHash");
        signed.remove("vnp_SecureHashType");
        String expectedHash = hmacSHA512(config.getHashSecret(), buildHashData(signed));
        if (receivedHash == null || !receivedHash.equalsIgnoreCase(expectedHash)) {
            throw new PayClientException("VNPay callback signature mismatch");
        }

        // 2. Map the result
        String outTradeNo = params.get("vnp_TxnRef");
        String channelOrderNo = params.get("vnp_TransactionNo");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String rawData = JsonUtils.toJsonString(params);

        if (SUCCESS_CODE.equals(responseCode) && SUCCESS_CODE.equals(transactionStatus)) {
            Instant successTime = parsePayDate(params.get("vnp_PayDate"));
            return PayOrderChannelRespDTO.successOf(channelOrderNo, null, successTime, outTradeNo, rawData);
        }
        return PayOrderChannelRespDTO.closedOf(responseCode, "VNPay response code " + responseCode,
                outTradeNo, rawData);
    }

    @Override
    protected PayOrderChannelRespDTO doGetOrder(PayOrderGetReqDTO reqDTO) throws Throwable {
        String outTradeNo = reqDTO.getOutTradeNo();
        String requestId = UUID.randomUUID().toString();
        String createDate = LocalDateTime.now(ZONE).format(DATE_FORMAT);
        // vnp_TransactionDate MUST be the original pay request's create date — carried in via createTime.
        String transactionDate = LocalDateTime.ofInstant(reqDTO.getCreateTime(), ZONE).format(DATE_FORMAT);
        String orderInfo = "Query transaction " + outTradeNo;
        String ipAddr = resolveServerIp();

        // querydr signs a PIPE-joined string in this exact field order (a VNPay quirk — NOT the sorted
        // key=value form used for the pay URL).
        String hashData = String.join("|",
                requestId, VERSION, QUERY_COMMAND, config.getTmnCode(), outTradeNo,
                transactionDate, createDate, ipAddr, orderInfo);
        String secureHash = hmacSHA512(config.getHashSecret(), hashData);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("vnp_RequestId", requestId);
        body.put("vnp_Version", VERSION);
        body.put("vnp_Command", QUERY_COMMAND);
        body.put("vnp_TmnCode", config.getTmnCode());
        body.put("vnp_TxnRef", outTradeNo);
        body.put("vnp_OrderInfo", orderInfo);
        body.put("vnp_TransactionDate", transactionDate);
        body.put("vnp_CreateDate", createDate);
        body.put("vnp_IpAddr", ipAddr);
        body.put("vnp_SecureHash", secureHash);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getQueryUrl()))
                .timeout(Duration.ofSeconds(10)) // read timeout
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String raw = response.body();

        @SuppressWarnings("unchecked")
        Map<String, String> result = JsonUtils.parseObject(raw, Map.class);
        String responseCode = result != null ? result.get("vnp_ResponseCode") : null;
        String transactionStatus = result != null ? result.get("vnp_TransactionStatus") : null;
        String channelOrderNo = result != null ? result.get("vnp_TransactionNo") : null;

        if (SUCCESS_CODE.equals(responseCode) && SUCCESS_CODE.equals(transactionStatus)) {
            return PayOrderChannelRespDTO.successOf(channelOrderNo, null, Instant.now(), outTradeNo, raw);
        }

        // Anything not clearly successful -> WAITING: reconcile must NOT close here (closing is the
        // expire job's careful decision). Keeps sync strictly a forward-only, success-only driver.
        return PayOrderChannelRespDTO.waitingOf(null, null, outTradeNo, raw);
    }

    // ================ helpers ================

    /** Build the string that is signed: {@code name=urlEncode(value)} for non-empty values, joined by '&'. */
    private String buildHashData(Map<String, String> sortedParams) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sortedParams.entrySet()) {
            String value = e.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=').append(encode(value));
        }
        return sb.toString();
    }

    /** Build the redirect query string: {@code urlEncode(name)=urlEncode(value)}, joined by '&'. */
    private String buildQuery(Map<String, String> sortedParams) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sortedParams.entrySet()) {
            String value = e.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(encode(e.getKey())).append('=').append(encode(value));
        }
        return sb.toString();
    }

    private static String encode(String value) {
        // US-ASCII to match VNPay's server-side verification (their official Java sample)
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new PayClientException(ex);
        }
    }

    private Instant parsePayDate(String payDate) {
        if (payDate == null || payDate.isEmpty()) {
            return Instant.now();
        }
        try {
            return LocalDateTime.parse(payDate, DATE_FORMAT).atZone(ZONE).toInstant();
        } catch (Exception ex) {
            log.warn("[parsePayDate][cannot parse vnp_PayDate({})]", payDate);
            return Instant.now();
        }
    }

    /** The merchant server's IP for {@code vnp_IpAddr}. Loopback only if the host lookup fails. */
    private String resolveServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            return "127.0.0.1";
        }
    }

}
