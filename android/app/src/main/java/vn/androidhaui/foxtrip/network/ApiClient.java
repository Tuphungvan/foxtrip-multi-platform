package vn.androidhaui.foxtrip.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.androidhaui.foxtrip.R;

import vn.androidhaui.foxtrip.models.dto.response.AuthResponse;

/**
 * Singleton — Retrofit + OkHttpClient chỉ khởi tạo MỘT LẦN cho toàn app.
 *
 * Dùng:
 * ApiService api = ApiClient.getInstance(context).getApiService();
 */
public class ApiClient {

    private static ApiClient instance;
    private final ApiService apiService;
    private final ApiService refreshApi; // Dedicated API for token refresh

    private ApiClient(Context context) {
        String baseUrl = context.getString(R.string.base_url);
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);

        // Standardized GSON with ISO-8601 UTC support
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        // 1. Primitive client for token refresh (no auth interceptor to avoid loops)
        OkHttpClient refreshHttpClient = new OkHttpClient.Builder().build();
        refreshApi = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(refreshHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);

        // 2. Main client with auto-refresh and auth header
        OkHttpClient mainClient = new OkHttpClient.Builder()
                // Tự động đính kèm accessToken vào mọi request
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();
                    String token = prefs.getString("accessToken", null);
                    if (token != null) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                // Tự động refresh token khi nhận 401
                .authenticator(new okhttp3.Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        // Prevent infinite loops (max 2 attempts for the same request)
                        if (responseCount(response) >= 2) {
                            return null;
                        }

                        if (response.request().url().toString().contains("/api/auth/refresh")) {
                            return null;
                        }

                        String refreshToken = prefs.getString("refreshToken", null);
                        if (refreshToken == null)
                            return null;

                        Map<String, String> body = new HashMap<>();
                        body.put("refreshToken", refreshToken);

                        // Use the pre-initialized refreshApi
                        retrofit2.Response<ApiResponse<AuthResponse>> refreshResponse = refreshApi.refreshToken(body)
                                .execute();

                        if (refreshResponse.isSuccessful()
                                && refreshResponse.body() != null
                                && refreshResponse.body().data != null) {

                            AuthResponse data = refreshResponse.body().data;
                            String newAccess = data.accessToken;
                            String newRefresh = data.refreshToken;
                            Long expires = data.expiresIn;

                            if (newAccess == null)
                                return null;

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("accessToken", newAccess);
                            if (newRefresh != null)
                                editor.putString("refreshToken", newRefresh);
                            if (expires != null)
                                editor.putLong("expiresIn", expires);
                            editor.apply();

                            return response.request().newBuilder()
                                    .header("Authorization", "Bearer " + newAccess)
                                    .build();
                        }

                        // Refresh failed or no refresh token - clear everything!
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.remove("accessToken")
                                .remove("refreshToken")
                                .remove("expiresIn")
                                .remove("userRole")
                                .apply();

                        return null;
                    }
                })
                .build();

        apiService = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(mainClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);
    }

    /** Helper to count responses for a request to prevent infinite retry loops. */
    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    /**
     * Lấy instance singleton. Luôn truyền applicationContext để tránh memory leak.
     */
    public static ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }
}
