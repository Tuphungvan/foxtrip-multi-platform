package haui.foxtrip.order.service.util;

import haui.foxtrip.order.config.VNPayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService {
    
    private final VNPayConfig vnPayConfig;
    
    /**
     * Generate VNPay payment URL
     * @param orderCode Order code (vnp_TxnRef)
     * @param totalAmount Total amount in VND
     * @param orderInfo Order description
     * @param ipAddress Client IP address
     * @return Payment URL
     */
    public String generatePaymentUrl(String orderCode, BigDecimal totalAmount, String orderInfo, String ipAddress) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(totalAmount.multiply(new BigDecimal("100")).longValue()));
            vnpParams.put("vnp_CurrCode", "VND");
            // Tạo mã giao dịch duy nhất bằng cách thêm timestamp để tránh lỗi quá hạn của VNPay
            String vnp_TxnRef = orderCode + "-" + System.currentTimeMillis();
            vnpParams.put("vnp_TxnRef", vnp_TxnRef);
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", ipAddress);
            
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            String vnpCreateDate = formatter.format(new Date());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            
            // Build query string
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            
            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            
            String vnpSecureHash = generateHmacSHA512(hashData.toString(), vnPayConfig.getHashSecret());
            query.append("&vnp_SecureHash=").append(vnpSecureHash);
            
            String paymentUrl = vnPayConfig.getUrl() + "?" + query.toString();
            log.info("Generated VNPay payment URL for order: {}", orderCode);
            
            return paymentUrl;
            
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to generate VNPay payment URL", e);
            throw new RuntimeException("Không thể tạo URL thanh toán", e);
        }
    }
    
    /**
     * Verify VNPay IPN signature
     * @param vnpParams All params from VNPay IPN
     * @return true if valid
     */
    public boolean verifyIpnSignature(Map<String, String> vnpParams) {
        String vnpSecureHash = vnpParams.get("vnp_SecureHash");
        if (vnpSecureHash == null) {
            return false;
        }
        
        // Remove hash params
        Map<String, String> paramsToHash = new HashMap<>(vnpParams);
        paramsToHash.remove("vnp_SecureHash");
        paramsToHash.remove("vnp_SecureHashType");
        
        // Build hash data
        List<String> fieldNames = new ArrayList<>(paramsToHash.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = paramsToHash.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                        hashData.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    log.error("Failed to encode IPN field", e);
                }
            }
        }
        
        String calculatedHash = generateHmacSHA512(hashData.toString(), vnPayConfig.getHashSecret());
        
        boolean isValid = calculatedHash.equalsIgnoreCase(vnpSecureHash);
        if (!isValid) {
            log.warn("VNPay IPN signature verification failed. Expected: {}, Got: {}", calculatedHash, vnpSecureHash);
        }
        
        return isValid;
    }
    
    private String generateHmacSHA512(String data, String secret) {
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_512, secret).hmacHex(data);
    }
}
