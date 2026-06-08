package haui.foxtrip.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/cloudinary")
public class CloudinarySignatureController {

    @Value("${application.cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${application.cloudinary.api-key:}")
    private String apiKey;

    @GetMapping("/signature")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SignatureResponse> createSignature(
            @RequestParam(defaultValue = "foxtrip/locations") String folder
    ) {
        long timestamp = Instant.now().getEpochSecond();
        if (!folder.equals("foxtrip/locations")
                && !folder.equals("foxtrip/avatars")
                && !folder.equals("foxtrip/tours")) {
            folder = "foxtrip/locations";
        }
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("folder", folder);
        String signature = sign(params, apiSecret);
        SignatureResponse res = new SignatureResponse();
        res.setSignature(signature);
        res.setTimestamp(timestamp);
        res.setApiKey(apiKey);
        res.setFolder(folder);
        return ApiResponse.success("Tạo Cloudinary signature thành công", res);
    }

    private String sign(Map<String, Object> params, String apiSecret) {
        StringBuilder sb = new StringBuilder();
        new TreeMap<>(params).forEach((k, v) -> {
            if (v != null && !v.toString().isBlank()) {
                if (!sb.isEmpty()) sb.append("&");
                sb.append(k).append("=").append(v);
            }
        });
        sb.append(apiSecret);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot sign upload request", e);
        }
    }

    @Data
    public static class SignatureResponse {
        private String signature;
        private long timestamp;
        private String apiKey;
        private String folder;
    }
}