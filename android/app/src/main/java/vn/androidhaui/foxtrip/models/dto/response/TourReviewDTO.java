package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TourReviewDTO implements Serializable {
    @SerializedName("reviewId")
    public String reviewId;

    @SerializedName("rating")
    public Integer rating;

    @SerializedName("content")
    public String content;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("user")
    public UserInfoDTO user;

    public static class UserInfoDTO implements Serializable {
        @SerializedName("username")
        public String username;

        @SerializedName("avatarUrl")
        public String avatarUrl;
    }
}
