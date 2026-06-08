package haui.foxtrip.order.web.rest;

import haui.foxtrip.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    /**
     * 2.42 GET /api/payments/vnpay/ipn - VNPay IPN webhook (PUBLIC)
     * 
     * IMPORTANT: This is a GET endpoint (not POST) as per VNPay specification
     * Response format: {"RspCode":"00","Message":"Confirm Success"}
     */
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> handleVNPayIPN(@RequestParam Map<String, String> vnpParams) {
        log.info("Received VNPay IPN: {}", vnpParams.get("vnp_TxnRef"));

        Map<String, String> response = new HashMap<>();
        try {
            String rspCode = orderService.processVNPayIPN(vnpParams);
            response.put("RspCode", rspCode);

            switch (rspCode) {
                case "00":
                    response.put("Message", "Confirm Success");
                    break;
                case "01":
                    response.put("Message", "Order not found");
                    break;
                case "02":
                    response.put("Message", "Order already confirmed");
                    break;
                case "04":
                    response.put("Message", "Invalid amount");
                    break;
                case "97":
                    response.put("Message", "Invalid signature");
                    break;
                default:
                    response.put("Message", "Unknown error");
                    break;
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to process VNPay IPN", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 2.xx GET /api/payments/vnpay/return - VNPay Return (CLIENT REDIRECT)
     * 
     * This endpoint is visited by the user's browser/WebView.
     * It processes the result and redirects to the final mobile success page.
     */
    @GetMapping("/vnpay/return")
    public ResponseEntity<Void> handleVNPayReturn(@RequestParam Map<String, String> vnpParams) {
        log.info("Received VNPay Return for order: {}", vnpParams.get("vnp_TxnRef"));
        
        try {
            orderService.processVNPayReturn(vnpParams);
        } catch (Exception e) {
            log.error("Failed to process VNPay Return", e);
        }

        // Redirect to the mobile success page with only essential VNPay parameters
        // This avoids issues with overly long URLs or invalid characters in raw VNPay params
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://foxtrip.vn/payment-callback");
        
        String[] essentialParams = {"vnp_ResponseCode", "vnp_TxnRef", "vnp_Amount", "vnp_OrderInfo"};
        for (String param : essentialParams) {
            String value = vnpParams.get(param);
            if (value != null) {
                builder.queryParam(param, value);
            }
        }
        
        String redirectUrl = builder.build().encode().toUriString();
        log.info("Redirecting to mobile success page: {}", redirectUrl);
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}
