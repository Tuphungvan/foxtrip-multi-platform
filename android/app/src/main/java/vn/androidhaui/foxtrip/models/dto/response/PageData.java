package vn.androidhaui.foxtrip.models.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class PageData<T> implements Serializable {

    @SerializedName("items")
    public List<T> items;

    @SerializedName("page")
    public Integer page;

    @SerializedName("size")
    public Integer size;

    @SerializedName("totalItems")
    public Long totalItems;

    @SerializedName("totalPages")
    public Integer totalPages;
}
