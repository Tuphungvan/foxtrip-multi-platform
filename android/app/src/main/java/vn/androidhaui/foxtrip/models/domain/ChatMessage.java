package vn.androidhaui.foxtrip.models.domain;

import java.util.List;
import vn.androidhaui.foxtrip.models.dto.response.TourCardResponse;
import vn.androidhaui.foxtrip.models.dto.response.LocationResponse;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private long timestamp;
    private List<TourCardResponse> tours;
    private List<LocationResponse> locations;

    public ChatMessage(String message, boolean isUser, long timestamp) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    public ChatMessage(String message, boolean isUser, long timestamp, List<TourCardResponse> tours) {
        this(message, isUser, timestamp);
        this.tours = tours;
    }

    public ChatMessage(String message, boolean isUser, long timestamp, List<TourCardResponse> tours, List<LocationResponse> locations) {
        this(message, isUser, timestamp, tours);
        this.locations = locations;
    }

    public String getMessage() { return message; }
    public boolean isUser() { return isUser; }
    public long getTimestamp() { return timestamp; }
    public List<TourCardResponse> getTours() { return tours; }
    public List<LocationResponse> getLocations() { return locations; }
}
