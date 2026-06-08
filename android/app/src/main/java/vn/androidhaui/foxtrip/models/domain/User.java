package vn.androidhaui.foxtrip.models.domain;

import com.google.gson.annotations.SerializedName;

public class User {
    public String id;
    public String username;
    public String email;

    @SerializedName("phone_number")
    public String phoneNumber;

    public String province;
    public String role;

    @SerializedName("is_verified")
    public boolean isVerified;

    @SerializedName("avatar_url")
    public String avatarUrl;

    @SerializedName("google_id")
    public String googleId;

    public boolean isGuide() {
        return "GUIDE".equals(role);
    }
}