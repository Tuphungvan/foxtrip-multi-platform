package haui.foxtrip.order.service.util;

import haui.foxtrip.order.domain.Order;
import haui.foxtrip.order.domain.OrderAddon;
import haui.foxtrip.order.domain.OrderItem;
import haui.foxtrip.order.repository.OrderAddonRepository;
import haui.foxtrip.order.repository.OrderItemRepository;
import haui.foxtrip.tour.domain.TourAddon;
import haui.foxtrip.tour.repository.TourAddonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddonRepository orderAddonRepository;
    private final TourAddonRepository tourAddonRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
        .ofPattern("dd/MM/yyyy HH:mm")
        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    /**
     * Send order confirmation email with QR code (async)
     */
    public void sendOrderConfirmationEmail(Order order, String qrBase64) {
        try {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            List<OrderAddon> orderAddons = orderAddonRepository.findByOrderId(order.getId());
            
            Map<UUID, String> addonNames = getAddonNames(orderAddons);
            
            // CID for the QR image
            String qrCid = "qrCodeImage";
            String htmlContent = buildOrderConfirmationHtml(order, orderItems, orderAddons, addonNames, qrCid);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Xác nhận đặt tour - " + order.getOrderCode());
            helper.setText(htmlContent, true);
            
            // Extract raw base64 and attach as inline resource
            // qrBase64 format: data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...
            String base64Data = qrBase64.substring(qrBase64.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            helper.addInline(qrCid, new ByteArrayResource(imageBytes), "image/png");
            
            mailSender.send(message);
            log.info("Sent order confirmation email to: {} for order: {}", order.getCustomerEmail(), order.getOrderCode());
            
        } catch (MessagingException e) {
            log.error("Failed to send order confirmation email for order: {}", order.getOrderCode(), e);
        }
    }
    
    private Map<UUID, String> getAddonNames(List<OrderAddon> orderAddons) {
        List<UUID> addonIds = orderAddons.stream()
            .map(OrderAddon::getTourAddonId)
            .collect(Collectors.toList());
        
        if (addonIds.isEmpty()) {
            return Map.of();
        }
        
        return tourAddonRepository.findAllById(addonIds).stream()
            .collect(Collectors.toMap(TourAddon::getId, TourAddon::getName));
    }
    
    private String buildOrderConfirmationHtml(Order order, List<OrderItem> orderItems, 
                                               List<OrderAddon> orderAddons, Map<UUID, String> addonNames, 
                                               String qrCid) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;'>");
        
        // Header
        html.append("<h2 style='color: #2c3e50; text-align: center;'>Xác nhận đặt tour thành công</h2>");
        html.append("<p style='text-align: center; color: #7f8c8d;'>Cảm ơn bạn đã đặt tour tại Foxtrip!</p>");
        html.append("<hr style='border: none; border-top: 2px solid #3498db; margin: 20px 0;'>");
        
        // Order info
        html.append("<h3 style='color: #2c3e50;'>Thông tin đơn hàng</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse;'>");
        html.append("<tr><td style='padding: 8px 0;'><strong>Mã đơn hàng:</strong></td><td style='padding: 8px 0;'>").append(order.getOrderCode()).append("</td></tr>");
        html.append("<tr><td style='padding: 8px 0;'><strong>Khách hàng:</strong></td><td style='padding: 8px 0;'>").append(order.getCustomerName()).append("</td></tr>");
        html.append("<tr><td style='padding: 8px 0;'><strong>Số điện thoại:</strong></td><td style='padding: 8px 0;'>").append(order.getCustomerPhone()).append("</td></tr>");
        html.append("<tr><td style='padding: 8px 0;'><strong>Email:</strong></td><td style='padding: 8px 0;'>").append(order.getCustomerEmail()).append("</td></tr>");
        html.append("</table>");
        
        // Tour info
        if (!orderItems.isEmpty()) {
            OrderItem item = orderItems.get(0);
            html.append("<h3 style='color: #2c3e50; margin-top: 20px;'>Thông tin tour</h3>");
            html.append("<table style='width: 100%; border-collapse: collapse;'>");
            html.append("<tr><td style='padding: 8px 0;'><strong>Tên tour:</strong></td><td style='padding: 8px 0;'>").append(item.getTourNameAtTime()).append("</td></tr>");
            html.append("<tr><td style='padding: 8px 0;'><strong>Số lượng:</strong></td><td style='padding: 8px 0;'>").append(item.getQuantity()).append(" người</td></tr>");
            html.append("<tr><td style='padding: 8px 0;'><strong>Ngày khởi hành:</strong></td><td style='padding: 8px 0;'>").append(DATE_FORMATTER.format(item.getStartDateAtTime())).append("</td></tr>");
            html.append("<tr><td style='padding: 8px 0;'><strong>Ngày kết thúc:</strong></td><td style='padding: 8px 0;'>").append(DATE_FORMATTER.format(item.getEndDateAtTime())).append("</td></tr>");
            html.append("</table>");
        }
        
        // Addons
        if (!orderAddons.isEmpty()) {
            html.append("<h3 style='color: #2c3e50; margin-top: 20px;'>Dịch vụ bổ sung</h3>");
            html.append("<ul style='list-style-type: none; padding: 0;'>");
            for (OrderAddon addon : orderAddons) {
                String addonName = addonNames.getOrDefault(addon.getTourAddonId(), "N/A");
                html.append("<li style='padding: 5px 0;'>• ").append(addonName)
                    .append(" x ").append(addon.getQuantity())
                    .append(" - ").append(CURRENCY_FORMATTER.format(addon.getPriceAtTime()))
                    .append("</li>");
            }
            html.append("</ul>");
        }
        
        // Total amount
        html.append("<div style='margin-top: 20px; padding: 15px; background-color: #ecf0f1; border-radius: 5px;'>");
        html.append("<h3 style='color: #2c3e50; margin: 0;'>Tổng tiền: <span style='color: #e74c3c;'>")
            .append(CURRENCY_FORMATTER.format(order.getTotalAmount())).append("</span></h3>");
        html.append("</div>");
        
        // QR Code using CID
        html.append("<h3 style='color: #2c3e50; margin-top: 20px; text-align: center;'>Mã QR check-in</h3>");
        html.append("<div style='text-align: center; margin: 20px 0;'>");
        html.append("<img src='cid:").append(qrCid).append("' alt='QR Code' style='max-width: 200px; border: 2px solid #3498db; border-radius: 8px;'/>");
        html.append("</div>");
        html.append("<p style='text-align: center; color: #7f8c8d; font-size: 14px;'>Vui lòng xuất trình mã QR này khi check-in tour</p>");
        
        // Footer
        html.append("<hr style='border: none; border-top: 1px solid #ddd; margin: 30px 0;'>");
        html.append("<p style='text-align: center; color: #7f8c8d; font-size: 12px;'>Cảm ơn bạn đã sử dụng dịch vụ của Foxtrip!</p>");
        html.append("<p style='text-align: center; color: #7f8c8d; font-size: 12px;'>Nếu có thắc mắc, vui lòng liên hệ: support@foxtrip.com</p>");
        
        html.append("</div></body></html>");
        
        return html.toString();
    }

    /**
     * Send tour completion thank you and review nudge (async)
     */
    public void sendReviewNudgeEmail(Order order, String tourName) {
        try {
            String htmlContent = buildReviewNudgeHtml(order, tourName);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Cảm ơn bạn đã tham gia tour " + tourName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Sent review nudge email to: {} for order: {}", order.getCustomerEmail(), order.getOrderCode());
            
        } catch (MessagingException e) {
            log.error("Failed to send review nudge email for order: {}", order.getOrderCode(), e);
        }
    }
    
    private String buildReviewNudgeHtml(Order order, String tourName) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;'>");
        
        // Header
        html.append("<h2 style='color: #2c3e50; text-align: center;'>Tour đã hoàn thành!</h2>");
        html.append("<p style='text-align: center; color: #7f8c8d;'>Chào <strong>").append(order.getCustomerName()).append("</strong>,</p>");
        html.append("<p style='text-align: center;'>Cảm ơn bạn đã tin tưởng và đồng hành cùng Foxtrip trong tour <strong>").append(tourName).append("</strong> vừa qua.</p>");
        html.append("<hr style='border: none; border-top: 2px solid #3498db; margin: 20px 0;'>");
        
        // Content
        html.append("<p>Hy vọng bạn đã có một hành trình đáng nhớ. Sự hài lòng của bạn là niềm hạnh phúc của chúng tôi.</p>");
        html.append("<p>Hãy dành chút thời gian để chia sẻ cảm nhận của bạn về chuyến đi này nhé! Những ý kiến của bạn sẽ giúp Foxtrip cải thiện dịch vụ tốt hơn.</p>");
        
        // Nudge
        html.append("<div style='margin: 30px 0; text-align: center;'>");
        html.append("<p style='font-weight: bold; color: #e67e22;'>Bạn có thể thực hiện đánh giá ngay trong ứng dụng Foxtrip:</p>");
        html.append("<p style='font-style: italic; color: #7f8c8d;'>Menu -> Lịch sử đơn hàng -> Chọn Tour và nhấn Đánh giá</p>");
        html.append("</div>");
        
        // Note
        html.append("<p style='font-size: 13px; color: #e74c3c; background-color: #fdf2f2; padding: 10px; border-radius: 4px;'>");
        html.append("<strong>Lưu ý:</strong> Để đảm bảo tính khách quan, đánh giá sau khi gửi sẽ không thể chỉnh sửa. Cảm ơn bạn!</p>");
        
        // Footer
        html.append("<hr style='border: none; border-top: 1px solid #ddd; margin: 30px 0;'>");
        html.append("<p style='text-align: center; color: #7f8c8d; font-size: 12px;'>Hẹn gặp lại bạn trong những chuyến đi tiếp theo!</p>");
        html.append("<p style='text-align: center; color: #7f8c8d; font-size: 12px;'> support@foxtrip.com | Foxtrip Team</p>");
        
        html.append("</div></body></html>");
        
        return html.toString();
    }

    /**
     * Send cancellation rejection email (async)
     */
    public void sendCancellationRejectionEmail(Order order, String reason) {
        try {
            String htmlContent = buildCancellationRejectionHtml(order, reason);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Thông báo về yêu cầu hủy đơn hàng - " + order.getOrderCode());
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Sent cancellation rejection email to: {} for order: {}", order.getCustomerEmail(), order.getOrderCode());
            
        } catch (MessagingException e) {
            log.error("Failed to send cancellation rejection email for order: {}", order.getOrderCode(), e);
        }
    }

    private String buildCancellationRejectionHtml(Order order, String reason) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;'>");
        
        // Header
        html.append("<h2 style='color: #c0392b; text-align: center;'>Yêu cầu hủy đơn hàng bị từ chối</h2>");
        html.append("<p>Chào <strong>").append(order.getCustomerName()).append("</strong>,</p>");
        html.append("<p>Chúng tôi đã nhận được yêu cầu hủy đơn hàng mã <strong>").append(order.getOrderCode()).append("</strong> của bạn.</p>");
        html.append("<p>Tuy nhiên, sau khi xem xét, chúng tôi rất tiếc phải thông báo rằng yêu cầu hủy này <strong>không được chấp nhận</strong>.</p>");
        
        // Reason
        html.append("<div style='margin: 20px 0; padding: 15px; background-color: #fcf8f2; border-left: 4px solid #f39c12;'>");
        html.append("<strong>Lý do từ chối:</strong><br/>");
        html.append(reason != null ? reason : "Lý do không được cung cấp.");
        html.append("</div>");
        
        html.append("<p>Đơn hàng của bạn hiện đã được chuyển về trạng thái <strong>Đã thanh toán (PAID)</strong> và vẫn còn hiệu lực. Vui lòng tham gia tour đúng thời gian quy định.</p>");
        
        // Footer
        html.append("<hr style='border: none; border-top: 1px solid #ddd; margin: 30px 0;'>");
        html.append("<p style='text-align: center; color: #7f8c8d; font-size: 12px;'>Nếu có thắc mắc, vui lòng phản hồi email này hoặc liên hệ hotline: 1900 xxxx</p>");
        html.append("<p style='text-align: center; color: #7f8c8d; font-size: 12px;'>Foxtrip Team</p>");
        
        html.append("</div></body></html>");
        
        return html.toString();
    }
}
