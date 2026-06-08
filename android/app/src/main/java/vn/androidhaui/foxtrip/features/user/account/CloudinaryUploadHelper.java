package vn.androidhaui.foxtrip.features.user.account;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import vn.androidhaui.foxtrip.models.dto.response.CloudinarySignatureResponse;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class CloudinaryUploadHelper {
    private static final String TAG = "CloudinaryUpload";
    private static final String CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/%s/image/upload";
    private static final String CLOUD_NAME = "do1ill8ba";

    private final Context context;
    private final ApiService apiService;

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public CloudinaryUploadHelper(Context context) {
        this.context = context;
        this.apiService = ApiClient.getInstance(context).getApiService();
    }

    public void uploadAvatar(Uri imageUri, UploadCallback callback) {
        // Bước 1: Lấy signature từ backend
        apiService.getCloudinarySignature("foxtrip/avatars").enqueue(new Callback<ApiResponse<CloudinarySignatureResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CloudinarySignatureResponse>> call,
                                   @NonNull retrofit2.Response<ApiResponse<CloudinarySignatureResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    CloudinarySignatureResponse signatureData = response.body().data;
                    // Bước 2: Upload lên Cloudinary
                    uploadToCloudinary(imageUri, signatureData, callback);
                } else {
                    callback.onError("Không thể lấy signature từ server");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CloudinarySignatureResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error getting signature", t);
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void uploadToCloudinary(Uri imageUri, CloudinarySignatureResponse signatureData, UploadCallback callback) {
        new Thread(() -> {
            try {
                // Tạo file tạm từ URI
                File imageFile = createTempFileFromUri(imageUri);

                // Tạo request body
                RequestBody fileBody = RequestBody.create(imageFile, MediaType.parse("image/*"));
                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", imageFile.getName(), fileBody)
                        .addFormDataPart("signature", signatureData.getSignature())
                        .addFormDataPart("timestamp", String.valueOf(signatureData.getTimestamp()))
                        .addFormDataPart("api_key", signatureData.getApiKey())
                        .addFormDataPart("folder", signatureData.getFolder());

                RequestBody requestBody = builder.build();

                // Upload lên Cloudinary
                String uploadUrl = String.format(CLOUDINARY_UPLOAD_URL, CLOUD_NAME);
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String secureUrl = jsonObject.getString("secure_url");
                    
                    // Xóa file tạm
                    imageFile.delete();
                    
                    callback.onSuccess(secureUrl);
                } else {
                    callback.onError("Upload thất bại: " + response.message());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error uploading to Cloudinary", e);
                callback.onError("Lỗi upload: " + e.getMessage());
            }
        }).start();
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = new File(context.getCacheDir(), "temp_upload_" + System.currentTimeMillis() + ".jpg");
        
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        
        if (inputStream != null) {
            inputStream.close();
        }
        
        return tempFile;
    }
}

