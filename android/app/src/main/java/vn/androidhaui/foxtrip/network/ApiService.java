package vn.androidhaui.foxtrip.network;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vn.androidhaui.foxtrip.models.dto.request.*;
import vn.androidhaui.foxtrip.models.dto.response.*;

public interface ApiService {
        @POST("/api/auth/register")
        Call<ApiResponse<Void>> register(@Body RegisterRequest body);

        @POST("/api/auth/forgot-password")
        Call<ApiResponse<Void>> forgotPassword(@Body ForgotPasswordRequest body);

        @POST("/api/auth/verify-email")
        Call<ApiResponse<Void>> verifyEmail(@Body VerifyEmailRequest request);

        @POST("/api/auth/resend-otp")
        Call<ApiResponse<Void>> resendOTP(@Body ResendOtpRequest request);

        @POST("/api/auth/login")
        Call<ApiResponse<AuthResponse>> login(@Body LoginRequest body);

        @POST("/api/auth/login/google")
        Call<ApiResponse<AuthResponse>> loginWithGoogle(@Body Map<String, String> body);

        @POST("/api/auth/refresh")
        Call<ApiResponse<AuthResponse>> refreshToken(@Body Map<String, String> body);

        @POST("/api/auth/logout")
        Call<ApiResponse<Void>> logout(@Body Map<String, String> body);

        @GET("/api/tours/search")
        Call<ApiResponse<PageData<TourCardResponse>>> searchTours(
                        @Query("q") String keyword,
                        @Query("province") String province,
                        @Query("category") String category,
                        @Query("priceFrom") Double priceFrom,
                        @Query("priceTo") Double priceTo,
                        @Query("startDate") String startDate,
                        @Query("endDate") String endDate,
                        @Query("page") Integer page,
                        @Query("size") Integer size);

        @GET("/api/tours/upcoming")
        Call<ApiResponse<List<TourCardResponse>>> getUpcomingTours(
                        @Query("limit") Integer limit);

        @GET("/api/tours/discounted")
        Call<ApiResponse<List<TourCardResponse>>> getDiscountedTours(
                        @Query("limit") Integer limit);

        @GET("/api/tours/discovery-map")
        Call<ApiResponse<List<MarkerResponse>>> getDiscoveryMap();

        @GET("/api/tours/{slug}")
        Call<ApiResponse<TourDetailResDTO>> getTourDetail(
                        @Path("slug") String slug);

        @GET("/api/tours/{tourId}/reviews")
        Call<ApiResponse<PageData<TourReviewDTO>>> getTourReviews(
                        @Path("tourId") String tourId,
                        @Query("page") Integer page,
                        @Query("size") Integer size);

        @GET("/api/locations/{locationId}")
        Call<ApiResponse<LocationResponse>> getLocationDetail(
                        @Path("locationId") String locationId);

        @GET("/api/users/me")
        Call<ApiResponse<UserDetailResponse>> getUserProfile();

        @PATCH("/api/users/me/profile")
        Call<ApiResponse<Void>> updateUserProfile(@Body UpdateUserProfileRequest request);

        @POST("/api/users/me/verify-email")
        Call<ApiResponse<Void>> verifyEmailLoggedIn(@Body VerifyOtpRequest request);

        @POST("/api/users/me/resend-otp")
        Call<ApiResponse<Void>> resendOtpLoggedIn();

        @GET("/api/guide/me")
        Call<ApiResponse<UserDetailResponse>> getGuideProfile();

        @PATCH("/api/guide/me/profile")
        Call<ApiResponse<Void>> updateGuideProfile(@Body UpdateStaffProfileRequest request);

        @GET("/api/cloudinary/signature")
        Call<ApiResponse<CloudinarySignatureResponse>> getCloudinarySignature(@Query("folder") String folder);

        @GET("/api/cart")
        Call<ApiResponse<CartResponseDTO>> getCart();

        @PUT("/api/cart/item")
        Call<ApiResponse<CartResponseDTO>> upsertCartItem(@Body UpsertCartItemRequest request);

        @DELETE("/api/cart")
        Call<ApiResponse<Void>> clearCart();

        @POST("/api/checkout/preview")
        Call<ApiResponse<CheckoutPreviewResponse>> checkoutPreview(@Body CheckoutPreviewRequest request);

        @POST("/api/orders")
        Call<ApiResponse<CreateOrderResDTO>> createOrder(@Body CreateOrderReqDTO request);

        @POST("/api/orders/{orderId}/payment")
        Call<ApiResponse<PaymentInitResDTO>> initPayment(@Path("orderId") String orderId);

        @POST("/api/orders/{orderId}/cancel-request")
        Call<ApiResponse<Void>> cancelOrder(@Path("orderId") String orderId, @Body CancelOrderReqDTO request);

        @GET("/api/my/orders")
        Call<ApiResponse<PageData<MyOrderListItemDTO>>> getMyOrders(
                        @Query("status") String status,
                        @Query("page") int page,
                        @Query("size") int size);

        @GET("/api/orders/{orderId}")
        Call<ApiResponse<OrderDetailResDTO>> getOrderDetail(@Path("orderId") String orderId);

        @GET("/api/reviews/check/{tourId}")
        Call<ApiResponse<Boolean>> checkReviewExists(@Path("tourId") String tourId);

        @POST("/api/reviews")
        Call<ApiResponse<Void>> submitReview(@Body Map<String, Object> body);

        @POST("/api/chatbot/chat")
        Call<ChatResponse> chat(@Body Map<String, Object> body);

        @GET("/api/tours/short-videos")
        Call<ApiResponse<List<TourVideoCardResponse>>> getShortVideos();

        @GET("/api/guide/tours")
        Call<ApiResponse<PageData<TourListResDTO>>> getGuideTours(
                        @Query("page") Integer page,
                        @Query("size") Integer size);

        @GET("/api/guide/tours/{tourId}")
        Call<ApiResponse<TourDetailResDTO>> getGuideTourDetail(
                        @Path("tourId") String tourId);

        @GET("/api/guide/tours/{tourId}/passengers")
        Call<ApiResponse<List<PassengerResDTO>>> getGuidePassengers(
                        @Path("tourId") String tourId);

        @POST("/api/guide/tours/{tourId}/check-in")
        Call<ApiResponse<Void>> checkIn(
                        @Path("tourId") String tourId,
                        @Query("orderCode") String orderCode);
}
