package haui.foxtrip.order.service.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.order.config.QRConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class QRCodeService {
    
    private final QRConfig qrConfig;
    
    /**
     * Generate QR code with signature
     * Format: orderCode|signature
     * Signature: HMAC-SHA256 of orderCode
     * Output: Base64 string with data URI prefix
     */
    public String generate(String orderCode) {
        try {
            String signature = generateSignature(orderCode);
            String qrContent = orderCode + "|" + signature;
            
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                qrContent, 
                BarcodeFormat.QR_CODE, 
                200, 
                200
            );
            
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            
            String base64 = Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
            return "data:image/png;base64," + base64;
            
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for order: {}", orderCode, e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Không thể tạo mã QR");
        }
    }
    
    /**
     * Verify QR payload
     * @param qrPayload format: orderCode|signature
     * @return orderCode if valid
     * @throws BusinessException if invalid
     */
    public String verify(String qrPayload) {
        String[] parts = qrPayload.split("\\|");
        if (parts.length != 2) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Mã QR không hợp lệ");
        }
        
        String orderCode = parts[0];
        String signature = parts[1];
        
        String expectedSignature = generateSignature(orderCode);
        if (!signature.equals(expectedSignature)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Mã QR không hợp lệ hoặc đã bị giả mạo");
        }
        
        return orderCode;
    }
    
    /**
     * Generate QR payload (orderCode|signature)
     */
    public String generatePayload(String orderCode) {
        String signature = generateSignature(orderCode);
        return orderCode + "|" + signature;
    }
    
    private String generateSignature(String orderCode) {
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, qrConfig.getSecretKey())
            .hmacHex(orderCode);
    }
}
