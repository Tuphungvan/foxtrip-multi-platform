package haui.foxtrip.location.service.goong;

import lombok.Data;
import java.util.List;

public class GoongDtos {

    @Data
    public static class AutoCompleteResponse {
        private List<Prediction> predictions;
        private String status;
    }

    @Data
    public static class Prediction {
        private String description;
        private String place_id;
        private StructuredFormatting structured_formatting;
    }

    @Data
    public static class StructuredFormatting {
        private String main_text;
        private String secondary_text;
    }

    @Data
    public static class PlaceDetailResponse {
        private PlaceResult result;
        private String status;
    }

    @Data
    public static class PlaceResult {
        private String place_id;
        private String formatted_address;
        private Geometry geometry;
        private String name;
        private List<Photo> photos;
        private String first_photo_url;
    }

    @Data
    public static class Geometry {
        private Location location;
    }

    @Data
    public static class Location {
        private Double lat;
        private Double lng;
    }

    @Data
    public static class Photo {
        private String photo_reference;
    }
}
