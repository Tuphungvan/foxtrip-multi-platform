package vn.androidhaui.foxtrip.features.user.chatbot;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.domain.ChatMessage;
import vn.androidhaui.foxtrip.models.dto.response.ChatResponse;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiService;

public class ChatRepository {
    private final ApiService apiService;

    public ChatRepository(Context context) {
        apiService = ApiClient.getInstance(context).getApiService();
    }

    public void sendMessage(String message, List<ChatMessage> history, ChatCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        
        List<Map<String, String>> historyList = new ArrayList<>();
        if (history != null) {
            // Chỉ gửi tối đa 10 câu gần nhất để tiết kiệm token và context
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                ChatMessage m = history.get(i);
                Map<String, String> item = new HashMap<>();
                item.put("role", m.isUser() ? "user" : "assistant");
                item.put("content", m.getMessage());
                historyList.add(item);
            }
        }
        body.put("history", historyList);
        
        apiService.chat(body).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi phản hồi từ server");
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface ChatCallback {
        void onSuccess(ChatResponse response);
        void onError(String error);
    }
}
