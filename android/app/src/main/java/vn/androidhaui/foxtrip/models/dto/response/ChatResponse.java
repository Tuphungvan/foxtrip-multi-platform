package vn.androidhaui.foxtrip.models.dto.response;

import java.util.List;
import vn.androidhaui.foxtrip.models.dto.response.TourCardResponse;
import vn.androidhaui.foxtrip.models.dto.response.LocationResponse;

public class ChatResponse {
    private String message;
    private List<TourCardResponse> tours;
    private List<LocationResponse> locations;

    public String getMessage() { return message; }
    public List<TourCardResponse> getTours() { return tours; }
    public List<LocationResponse> getLocations() { return locations; }
}
