package vn.androidhaui.foxtrip.features.user.order;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.databinding.DialogReviewBinding;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;
import java.util.HashMap;
import java.util.Map;

public class ReviewDialog extends Dialog {
    private final String tourId;
    private final Runnable onSuccess;
    private DialogReviewBinding binding;

    public ReviewDialog(@NonNull Context context, String tourId, Runnable onSuccess) {
        super(context);
        this.tourId = tourId;
        this.onSuccess = onSuccess;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = DialogReviewBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        
        // Làm cho background trong suốt để bo góc của layout hoạt động
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        
        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.btnSubmit.setOnClickListener(v -> {
            float rating = binding.ratingBar.getRating();
            String content = binding.etContent.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            if (content.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnSubmit.setEnabled(false);
            submitReview(rating, content, binding.btnSubmit);
        });
    }

    private void submitReview(float rating, String content, Button btnSubmit) {
        ApiService api = ApiClient.getInstance(getContext()).getApiService();
        Map<String, Object> body = new HashMap<>();
        body.put("tourId", tourId);
        body.put("rating", (int) rating);
        body.put("content", content);

        api.submitReview(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                btnSubmit.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    dismiss();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    try {
                        JSONObject err = new JSONObject(response.errorBody().string());
                        Toast.makeText(getContext(), err.optString("message", "Lỗi gửi đánh giá"), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Lỗi gửi đánh giá", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                btnSubmit.setEnabled(true);
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
