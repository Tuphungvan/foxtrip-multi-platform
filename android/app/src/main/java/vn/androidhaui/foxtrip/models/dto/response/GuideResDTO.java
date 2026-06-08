package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class GuideResDTO implements Serializable {
    @SerializedName("id")
    public String id;

    @SerializedName("username")
    public String username;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("email")
    public String email;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("avatarUrl")
    public String avatarUrl;
}
