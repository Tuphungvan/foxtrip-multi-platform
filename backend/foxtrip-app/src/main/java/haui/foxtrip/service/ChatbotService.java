package haui.foxtrip.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.common.enums.Province;
import haui.foxtrip.location.domain.Location;
import haui.foxtrip.location.domain.enums.LocType;
import haui.foxtrip.location.repository.LocationRepository;
import haui.foxtrip.location.service.dto.response.LocationResponse;
import haui.foxtrip.order.service.OrderService;
import haui.foxtrip.common.util.SecurityUtils;
import haui.foxtrip.order.service.dto.MyOrderListItemDTO;
import haui.foxtrip.tour.service.TourService;
import haui.foxtrip.tour.service.dto.response.TourCardResponse;
import haui.foxtrip.web.client.GroqClient;
import haui.foxtrip.web.rest.dto.chatbot.ChatbotDTOs;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final GroqClient groqClient;
    private final OrderService orderService;
    private final TourService tourService;
    private final LocationRepository locationRepository;

    public ChatbotDTOs.ChatResponse getChatResponse(String message, List<ChatbotDTOs.ChatRequest.Message> history) {
        String lowerMsg = message.toLowerCase();
        StringBuilder contextBuilder = new StringBuilder();
        List<TourCardResponse> tours = new ArrayList<>();
        List<LocationResponse> locations = new ArrayList<>();

        // 1. Dữ liệu chung: Số lượng tour
        PageData<TourCardResponse> allTours = tourService.searchTours(null, null, null, null, null, null, null,
                PageRequest.of(0, 1));
        contextBuilder.append(String.format("Tổng số tour hiện có trên hệ thống: %d\n", allTours.getTotalItems()));

        // 2. Lấy NHIỀU tỉnh từ tin nhắn (hỗ trợ so sánh nhiều tỉnh cùng lúc)
        Set<Province> provinces = new LinkedHashSet<>(extractProvinces(lowerMsg));
        if (provinces.isEmpty() && history != null) {
            for (int i = history.size() - 1; i >= 0; i--) {
                List<Province> historyProvinces = extractProvinces(history.get(i).getContent().toLowerCase());
                if (!historyProvinces.isEmpty()) {
                    provinces.addAll(historyProvinces);
                    break;
                }
            }
        }
        // Dùng tỉnh đầu tiên cho các logic đơn tỉnh (địa điểm)
        Province province = provinces.isEmpty() ? null : provinces.iterator().next();

        // 3. Phân loại ý định và lấy dữ liệu context
        // Lịch sử đơn hàng / Đặt tour
        if (lowerMsg.contains("đơn hàng") || lowerMsg.contains("lịch sử") || lowerMsg.contains("đã đặt") || lowerMsg.contains("đã mua") || (lowerMsg.contains("tour") && (lowerMsg.contains("của tôi") || lowerMsg.contains("đã")))) {
            UUID userId = SecurityUtils.getCurrentUserId().orElse(null);
            if (userId != null) {
                PageData<MyOrderListItemDTO> orders = orderService.getMyOrders(null, PageRequest.of(0, 10));
                if (orders != null && orders.getItems() != null && !orders.getItems().isEmpty()) {
                    contextBuilder.append("Danh sách các Tour người dùng đã đặt (Lịch sử đơn hàng):\n")
                            .append(orders.getItems().stream()
                                    .map(o -> String.format("- Mã: %s, Tour: %s, Trạng thái: %s, Tổng tiền: %,.0f VND",
                                            o.getOrderCode(),
                                            o.getTour() != null ? o.getTour().getTourNameAtTime() : "N/A",
                                            o.getStatus(),
                                            o.getTotalAmount()))
                                    .collect(Collectors.joining("\n")))
                            .append("\n");
                } else {
                    contextBuilder.append("Người dùng chưa có đơn hàng nào hoặc chưa đặt tour.\n");
                }
            } else {
                contextBuilder.append("Người dùng chưa đăng nhập, không thể xem đơn hàng.\n");
            }
        }

        // Tìm kiếm Tour — query từng tỉnh riêng để không bỏ sót
        if (lowerMsg.contains("tour") || lowerMsg.contains("du lịch") || lowerMsg.contains("chuyến đi")
                || (!provinces.isEmpty() && (lowerMsg.contains("có gì") || lowerMsg.contains("gợi ý")))) {
            Double priceTo = extractPrice(lowerMsg);
            BigDecimal priceToBD = priceTo != null ? BigDecimal.valueOf(priceTo) : null;

            String tourKeyword = extractKeyword(lowerMsg, "tour", "du lịch", "chuyến đi", "tìm", "xem", "có", "gợi ý", "ở", "tại");
            if (provinces.isEmpty()) {
                // Không có tỉnh cụ thể → tìm chung theo keyword
                tours = tourService.searchTours(tourKeyword.isEmpty() ? null : tourKeyword, null, null, null, priceToBD, null, null,
                        PageRequest.of(0, 5)).getItems();
            } else {
                // Có nhiều tỉnh → query từng tỉnh, kèm keyword
                Set<UUID> seenIds = new LinkedHashSet<>();
                for (Province p : provinces) {
                    List<TourCardResponse> provinceTours = tourService.searchTours(tourKeyword.isEmpty() ? null : tourKeyword, p, null, null,
                            priceToBD, null, null, PageRequest.of(0, 3)).getItems();
                    for (TourCardResponse t : provinceTours) {
                        if (seenIds.add(t.getId())) {
                            tours.add(t);
                        }
                    }
                }
            }

            if (!tours.isEmpty()) {
                contextBuilder.append("Các tour du lịch phù hợp (dữ liệu chính xác từ hệ thống):\n")
                        .append(tours.stream()
                                .map(t -> String.format("- Tên: \"%s\", Giá chính xác: %,.0f VND, Tỉnh: %s",
                                        t.getName(), t.getFinalPrice(), provinceToVi(t.getProvince())))
                                .collect(Collectors.joining("\n")))
                        .append("\n");
            } else {
                contextBuilder.append("Không tìm thấy tour phù hợp trong hệ thống.\n");
            }
        }

        // Tìm kiếm Địa điểm (Cafe, Nhà hàng, Danh lam thắng cảnh)
        if (lowerMsg.contains("địa điểm") || lowerMsg.contains("chỗ chơi") || lowerMsg.contains("cảnh đẹp")
                || lowerMsg.contains("tham quan") || lowerMsg.contains("ở đâu") || lowerMsg.contains("cà phê")
                || lowerMsg.contains("cafe") || lowerMsg.contains("ăn uống") || lowerMsg.contains("nhà hàng")
                || lowerMsg.contains("quán")) {

            String keyword = extractKeyword(lowerMsg, "địa điểm", "chỗ chơi", "cảnh đẹp", "tham quan", "ở đâu",
                    "cà phê", "cafe", "ăn uống", "nhà hàng", "quán");
            Province finalProvince = province;

            Specification<Location> locSpec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isNull(root.get("deletedAt")));

                if (finalProvince != null) {
                    predicates.add(cb.equal(root.get("province"), finalProvince));
                }

                if (!keyword.isEmpty()) {
                    String pattern = "%" + keyword.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("address")), pattern)));
                }

                // Nếu hỏi cụ thể về cafe/nhà hàng, ưu tiên lọc theo LocType
                if (lowerMsg.contains("cà phê") || lowerMsg.contains("cafe")) {
                    predicates.add(cb.equal(root.get("type"), LocType.CAFE));
                } else if (lowerMsg.contains("ăn uống") || lowerMsg.contains("nhà hàng")
                        || lowerMsg.contains("quán ăn")) {
                    predicates.add(cb.or(cb.equal(root.get("type"), LocType.RESTAURANT),
                            cb.equal(root.get("type"), LocType.FOOD)));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            List<Location> foundLocs = locationRepository.findAll(locSpec, PageRequest.of(0, 5)).getContent();
            
            locations = foundLocs.stream().map(l -> {
                LocationResponse lr = new LocationResponse();
                lr.setId(l.getId());
                lr.setName(l.getName());
                lr.setAddress(l.getAddress());
                lr.setImageUrl(l.getImageUrl());
                lr.setType(l.getType());
                lr.setProvince(l.getProvince());
                if (l.getCoordinates() != null) {
                    lr.setLat(l.getCoordinates().getY());
                    lr.setLng(l.getCoordinates().getX());
                }
                return lr;
            }).collect(Collectors.toList());

            if (!locations.isEmpty()) {
                contextBuilder.append("Thông tin địa điểm gợi ý thực tế:\n")
                        .append(locations.stream()
                                .map(l -> String.format("- %s (Địa chỉ: %s)", l.getName(), l.getAddress()))
                                .collect(Collectors.joining("\n")))
                        .append("\n");
            }
        }

        // 4. Gửi context và history cho AI — tách system vs user message
        String systemPrompt = "Bạn là trợ lý ảo Foxtrip chuyên về tư vấn du lịch Việt Nam. QUY TẮC NGHIÊM NGẶT:\n"
                + "1. Trả lời các vấn đề về du lịch, tour, địa điểm tham quan, ăn uống tại Việt Nam.\n"
                + "2. Khi gợi ý địa điểm, hãy cung cấp địa chỉ cụ thể từ dữ liệu hệ thống (nếu có).\n"
                + "3. Nếu người dùng hỏi về tour họ đã đặt, hãy tra cứu trong phần 'Danh sách các Tour người dùng đã đặt'.\n"
                + "4. Chỉ từ chối khi nội dung hoàn toàn không liên quan đến du lịch.\n"
                + "5. Trả lời ngắn gọn, thân thiện, mang tính chất tư vấn và gợi ý.\n"
                + "6. QUAN TRỌNG: Về giá tour và thông tin tour, CHỈ được sử dụng số liệu trong phần DỮ LIỆU HỆ THỐNG."
                + " Tuyệt đối không tự bịa đặt hay ước tính giá tiền.\n"
                + "7. XỬ LÝ SAI CHÍNH TẢ: Nếu người dùng gõ sai (ví dụ 'hú qu' thay vì 'Phú Quốc') nhưng hệ thống vẫn trả về kết quả,"
                + " hãy hiểu rằng đó là kết quả người dùng đang tìm và trả lời dựa trên kết quả đó.\n"
                + "8. Nếu DỮ LIỆU HỆ THỐNG hoàn toàn không có kết quả nào phù hợp sau khi đã đối chiếu, hãy thông báo lịch sự là không tìm thấy.";

        StringBuilder userMessageBuilder = new StringBuilder();
        userMessageBuilder.append("DỮ LIỆU HỆ THỐNG (ưu tiên tuyệt đối, không được thay đổi):\n")
                .append(contextBuilder.toString()).append("\n");

        if (history != null && !history.isEmpty()) {
            userMessageBuilder.append("LỊCH SỬ TRÒ CHUYỆN:\n");
            for (ChatbotDTOs.ChatRequest.Message msg : history) {
                userMessageBuilder.append(msg.getRole().toUpperCase()).append(": ").append(msg.getContent()).append("\n");
            }
        }

        userMessageBuilder.append("\nCÂU HỎI HIỆN TẠI: ").append(message);

        String aiReply = groqClient.queryGroq(systemPrompt, userMessageBuilder.toString());

        return ChatbotDTOs.ChatResponse.builder()
                .message(aiReply)
                .tours(tours)
                .locations(locations)
                .build();
    }

    // Trả về danh sách TẤT CẢ tỉnh được đề cập trong tin nhắn (hỗ trợ so sánh nhiều tỉnh)
    private List<Province> extractProvinces(String msg) {
        List<Province> result = new ArrayList<>();
        if (msg == null) return result;

        // Miền Bắc
        if (msg.contains("hà nội") || msg.contains("ha noi")) result.add(Province.HA_NOI);
        if (msg.contains("quảng ninh") || msg.contains("quang ninh") || msg.contains("hạ long")) result.add(Province.QUANG_NINH);
        if (msg.contains("lào cai") || msg.contains("sapa") || msg.contains("sa pa")) result.add(Province.LAO_CAI);
        if (msg.contains("ninh bình") || msg.contains("ninh binh")) result.add(Province.NINH_BINH);
        if (msg.contains("cao bằng") || msg.contains("cao bang")) result.add(Province.CAO_BANG);
        if (msg.contains("lạng sơn") || msg.contains("lang son")) result.add(Province.LANG_SON);
        if (msg.contains("lai châu") || msg.contains("lai chau")) result.add(Province.LAI_CHAU);
        if (msg.contains("điện biên") || msg.contains("dien bien")) result.add(Province.DIEN_BIEN);
        if (msg.contains("sơn la") || msg.contains("son la")) result.add(Province.SON_LA);
        if (msg.contains("tuyên quang") || msg.contains("tuyen quang")) result.add(Province.TUYEN_QUANG);
        if (msg.contains("thái nguyên") || msg.contains("thai nguyen")) result.add(Province.THAI_NGUYEN);
        if (msg.contains("phú thọ") || msg.contains("phu tho")) result.add(Province.PHU_THO);
        if (msg.contains("bắc ninh") || msg.contains("bac ninh")) result.add(Province.BAC_NINH);
        if (msg.contains("hưng yên") || msg.contains("hung yen")) result.add(Province.HUNG_YEN);
        if (msg.contains("hải phòng") || msg.contains("hai phong")) result.add(Province.HAI_PHONG);
        // Miền Trung
        if (msg.contains("thanh hóa") || msg.contains("thanh hoa")) result.add(Province.THANH_HOA);
        if (msg.contains("nghệ an") || msg.contains("nghe an") || msg.contains("vinh")) result.add(Province.NGHE_AN);
        if (msg.contains("ha tinh") || msg.contains("hà tĩnh")) result.add(Province.HA_TINH);
        if (msg.contains("quảng trị") || msg.contains("quang tri")) result.add(Province.QUANG_TRI);
        if (msg.contains("huế") || msg.contains("hue")) result.add(Province.HUE);
        if (msg.contains("đà nẵng") || msg.contains("da nang")) result.add(Province.DA_NANG);
        if (msg.contains("quảng ngãi") || msg.contains("quang ngai")) result.add(Province.QUANG_NGAI);
        if (msg.contains("gia lai") || msg.contains("pleiku")) result.add(Province.GIA_LAI);
        if (msg.contains("đắk lắk") || msg.contains("dak lak") || msg.contains("buôn ma thuột")) result.add(Province.DAK_LAK);
        if (msg.contains("khánh hòa") || msg.contains("nha trang")) result.add(Province.KHANH_HOA);
        if (msg.contains("lâm đồng") || msg.contains("đà lạt") || msg.contains("da lat")) result.add(Province.LAM_DONG);
        // Miền Nam
        if (msg.contains("hồ chí minh") || msg.contains("tphcm") || msg.contains("sài gòn")) result.add(Province.TPHCM);
        if (msg.contains("đồng nai") || msg.contains("dong nai") || msg.contains("biên hòa")) result.add(Province.DONG_NAI);
        if (msg.contains("tây ninh") || msg.contains("tay ninh")) result.add(Province.TAY_NINH);
        if (msg.contains("cần thơ") || msg.contains("can tho")) result.add(Province.CAN_THO);
        if (msg.contains("vĩnh long") || msg.contains("vinh long")) result.add(Province.VINH_LONG);
        if (msg.contains("đồng tháp") || msg.contains("dong thap")) result.add(Province.DONG_THAP);
        if (msg.contains("an giang") || msg.contains("long xuyên")) result.add(Province.AN_GIANG);
        if (msg.contains("cà mau") || msg.contains("ca mau")) result.add(Province.CA_MAU);

        return result;
    }

    /** Giữ lại để tương thích với code cũ (dùng cho location search) */
    private Province extractProvince(String msg) {
        if (msg == null)
            return null;
        // Miền Bắc
        if (msg.contains("hà nội") || msg.contains("ha noi"))
            return Province.HA_NOI;
        if (msg.contains("quảng ninh") || msg.contains("quang ninh") || msg.contains("hạ long"))
            return Province.QUANG_NINH;
        if (msg.contains("lào cai") || msg.contains("sapa") || msg.contains("sa pa"))
            return Province.LAO_CAI;
        if (msg.contains("ninh bình") || msg.contains("ninh binh"))
            return Province.NINH_BINH;
        if (msg.contains("cao bằng") || msg.contains("cao bang"))
            return Province.CAO_BANG;
        if (msg.contains("lạng sơn") || msg.contains("lang son"))
            return Province.LANG_SON;
        if (msg.contains("lai châu") || msg.contains("lai chau"))
            return Province.LAI_CHAU;
        if (msg.contains("điện biên") || msg.contains("dien bien"))
            return Province.DIEN_BIEN;
        if (msg.contains("sơn la") || msg.contains("son la"))
            return Province.SON_LA;
        if (msg.contains("tuyên quang") || msg.contains("tuyen quang"))
            return Province.TUYEN_QUANG;
        if (msg.contains("thái nguyên") || msg.contains("thai nguyen"))
            return Province.THAI_NGUYEN;
        if (msg.contains("phú thọ") || msg.contains("phu tho"))
            return Province.PHU_THO;
        if (msg.contains("bắc ninh") || msg.contains("bac ninh"))
            return Province.BAC_NINH;
        if (msg.contains("hưng yên") || msg.contains("hung yen"))
            return Province.HUNG_YEN;
        if (msg.contains("hải phòng") || msg.contains("hai phong"))
            return Province.HAI_PHONG;

        // Miền Trung
        if (msg.contains("thanh hóa") || msg.contains("thanh hoa"))
            return Province.THANH_HOA;
        if (msg.contains("nghệ an") || msg.contains("nghe an") || msg.contains("vinh"))
            return Province.NGHE_AN;
        if (msg.contains("ha tinh") || msg.contains("hà tĩnh"))
            return Province.HA_TINH;
        if (msg.contains("quảng trị") || msg.contains("quang tri"))
            return Province.QUANG_TRI;
        if (msg.contains("huế") || msg.contains("hue"))
            return Province.HUE;
        if (msg.contains("đà nẵng") || msg.contains("da nang"))
            return Province.DA_NANG;
        if (msg.contains("quảng ngãi") || msg.contains("quang ngai"))
            return Province.QUANG_NGAI;
        if (msg.contains("gia lai") || msg.contains("pleiku"))
            return Province.GIA_LAI;
        if (msg.contains("đắk lắk") || msg.contains("dak lak") || msg.contains("buôn ma thuột"))
            return Province.DAK_LAK;
        if (msg.contains("khánh hòa") || msg.contains("nha trang"))
            return Province.KHANH_HOA;
        if (msg.contains("lâm đồng") || msg.contains("đà lạt") || msg.contains("da lat"))
            return Province.LAM_DONG;

        // Miền Nam & Tây Nguyên
        if (msg.contains("hồ chí minh") || msg.contains("tphcm") || msg.contains("sài gòn"))
            return Province.TPHCM;
        if (msg.contains("đồng nai") || msg.contains("dong nai") || msg.contains("biên hòa"))
            return Province.DONG_NAI;
        if (msg.contains("tây ninh") || msg.contains("tay ninh"))
            return Province.TAY_NINH;
        if (msg.contains("cần thơ") || msg.contains("can tho"))
            return Province.CAN_THO;
        if (msg.contains("vĩnh long") || msg.contains("vinh long"))
            return Province.VINH_LONG;
        if (msg.contains("đồng tháp") || msg.contains("dong thap"))
            return Province.DONG_THAP;
        if (msg.contains("an giang") || msg.contains("long xuyên"))
            return Province.AN_GIANG;
        if (msg.contains("cà mau") || msg.contains("ca mau"))
            return Province.CA_MAU;

        return null;
    }

    private Double extractPrice(String msg) {
        String[] words = msg.split(" ");
        for (int i = 0; i < words.length; i++) {
            try {
                if (words[i].contains("triệu") || words[i].equals("tr")) {
                    if (i > 0) {
                        return Double.parseDouble(words[i - 1].replace(",", ".")) * 1_000_000;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String extractKeyword(String msg, String... stopWords) {
        String keyword = msg;
        for (String word : stopWords) {
            keyword = keyword.replace(word, "");
        }
        return keyword.trim();
    }

    private String provinceToVi(Province province) {
        if (province == null) return "";
        return switch (province) {
            case HA_NOI -> "Hà Nội";
            case HUE -> "Huế";
            case QUANG_NINH -> "Quảng Ninh";
            case CAO_BANG -> "Cao Bằng";
            case LANG_SON -> "Lạng Sơn";
            case LAI_CHAU -> "Lai Châu";
            case DIEN_BIEN -> "Điện Biên";
            case SON_LA -> "Sơn La";
            case THANH_HOA -> "Thanh Hóa";
            case NGHE_AN -> "Nghệ An";
            case HA_TINH -> "Hà Tĩnh";
            case TUYEN_QUANG -> "Tuyên Quang";
            case LAO_CAI -> "Lào Cai";
            case THAI_NGUYEN -> "Thái Nguyên";
            case PHU_THO -> "Phú Thọ";
            case BAC_NINH -> "Bắc Ninh";
            case HUNG_YEN -> "Hưng Yên";
            case HAI_PHONG -> "Hải Phòng";
            case NINH_BINH -> "Ninh Bình";
            case QUANG_TRI -> "Quảng Trị";
            case DA_NANG -> "Đà Nẵng";
            case QUANG_NGAI -> "Quảng Ngãi";
            case GIA_LAI -> "Gia Lai";
            case KHANH_HOA -> "Khánh Hòa";
            case LAM_DONG -> "Lâm Đồng";
            case DAK_LAK -> "Đắk Lắk";
            case TPHCM -> "TP. Hồ Chí Minh";
            case DONG_NAI -> "Đồng Nai";
            case TAY_NINH -> "Tây Ninh";
            case CAN_THO -> "Cần Thơ";
            case VINH_LONG -> "Vĩnh Long";
            case DONG_THAP -> "Đồng Tháp";
            case CA_MAU -> "Cà Mau";
            case AN_GIANG -> "An Giang";
        };
    }
}
