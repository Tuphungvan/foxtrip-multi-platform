package vn.androidhaui.foxtrip.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProvinceUtils {
    private static final Map<String, String> PROVINCE_MAP = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_MAP = new LinkedHashMap<>();

    static {
        // Provinces
        PROVINCE_MAP.put("HA_NOI", "Hà Nội");
        PROVINCE_MAP.put("HUE", "Huế");
        PROVINCE_MAP.put("QUANG_NINH", "Quảng Ninh");
        PROVINCE_MAP.put("CAO_BANG", "Cao Bằng");
        PROVINCE_MAP.put("LANG_SON", "Lạng Sơn");
        PROVINCE_MAP.put("LAI_CHAU", "Lai Châu");
        PROVINCE_MAP.put("DIEN_BIEN", "Điện Biên");
        PROVINCE_MAP.put("SON_LA", "Sơn La");
        PROVINCE_MAP.put("THANH_HOA", "Thanh Hóa");
        PROVINCE_MAP.put("NGHE_AN", "Nghệ An");
        PROVINCE_MAP.put("HA_TINH", "Hà Tĩnh");
        PROVINCE_MAP.put("TUYEN_QUANG", "Tuyên Quang");
        PROVINCE_MAP.put("LAO_CAI", "Lào Cai");
        PROVINCE_MAP.put("THAI_NGUYEN", "Thái Nguyên");
        PROVINCE_MAP.put("PHU_THO", "Phú Thọ");
        PROVINCE_MAP.put("BAC_NINH", "Bắc Ninh");
        PROVINCE_MAP.put("HUNG_YEN", "Hưng Yên");
        PROVINCE_MAP.put("HAI_PHONG", "Hải Phòng");
        PROVINCE_MAP.put("NINH_BINH", "Ninh Bình");
        PROVINCE_MAP.put("QUANG_TRI", "Quảng Trị");
        PROVINCE_MAP.put("DA_NANG", "Đà Nẵng");
        PROVINCE_MAP.put("QUANG_NGAI", "Quảng Ngãi");
        PROVINCE_MAP.put("GIA_LAI", "Gia Lai");
        PROVINCE_MAP.put("KHANH_HOA", "Khánh Hòa");
        PROVINCE_MAP.put("LAM_DONG", "Lâm Đồng");
        PROVINCE_MAP.put("DAK_LAK", "Đắk Lắk");
        PROVINCE_MAP.put("TPHCM", "TP. Hồ Chí Minh");
        PROVINCE_MAP.put("DONG_NAI", "Đồng Nai");
        PROVINCE_MAP.put("TAY_NINH", "Tây Ninh");
        PROVINCE_MAP.put("CAN_THO", "Cần Thơ");
        PROVINCE_MAP.put("VINH_LONG", "Vĩnh Long");
        PROVINCE_MAP.put("DONG_THAP", "Đồng Tháp");
        PROVINCE_MAP.put("CA_MAU", "Cà Mau");
        PROVINCE_MAP.put("AN_GIANG", "An Giang");

        // Categories
        CATEGORY_MAP.put("SEA", "Biển");
        CATEGORY_MAP.put("CULTURE", "Văn hóa");
        CATEGORY_MAP.put("NATURE", "Thiên nhiên");
        CATEGORY_MAP.put("RELAX", "Nghỉ dưỡng");
    }

    public static String getProvinceDisplay(String enumName) {
        if (enumName == null) return "N/A";
        String display = PROVINCE_MAP.get(enumName);
        return display != null ? display : enumName;
    }

    public static String getCategoryDisplay(String enumName) {
        if (enumName == null) return "N/A";
        String display = CATEGORY_MAP.get(enumName);
        return display != null ? display : enumName;
    }

    public static List<String> getAllProvinceDisplays() {
        return new ArrayList<>(PROVINCE_MAP.values());
    }

    public static String getProvinceEnum(String display) {
        for (Map.Entry<String, String> entry : PROVINCE_MAP.entrySet()) {
            if (entry.getValue().equals(display)) return entry.getKey();
        }
        return null;
    }

    public static List<String> getAllCategoryDisplays() {
        return new ArrayList<>(CATEGORY_MAP.values());
    }

    public static String getCategoryEnum(String display) {
        for (Map.Entry<String, String> entry : CATEGORY_MAP.entrySet()) {
            if (entry.getValue().equals(display)) return entry.getKey();
        }
        return null;
    }
}
