package com.hofang.bookchainfe.ui.payment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.CancelOrderRequest;
import com.hofang.bookchainfe.network.ApiConfig;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.navigation.NavOptions;

/**
 * Lớp hỗ trợ xử lý Deep Link trả về từ cổng thanh toán (PayOS).
 */
public class PaymentDeepLinkHandler {

    private static final String TAG = "PaymentDeepLinkHandler";
    private final Activity activity;

    public PaymentDeepLinkHandler(Activity activity) {
        this.activity = activity;
    }

    /**
     * Phân tích Intent và điều hướng nếu đó là Deep Link thanh toán.
     *
     * @param intent        Intent nhận được từ MainActivity.
     * @param navController NavController để thực hiện điều hướng.
     * @return true nếu đã xử lý deep link thanh toán, false nếu không phải.
     */
    public boolean handleIntent(Intent intent, NavController navController) {
        Uri data = intent.getData();

        // Kiểm tra xem đây có phải là Deep Link "mybookapp" không
        if (data != null && "mybookapp".equals(data.getScheme())) {
            String status = data.getQueryParameter("status");
            String orderCodeStr = data.getQueryParameter("orderCode");

            Log.i(TAG, "🔥 Đã nhận Deep Link! Status=" + status + ", OrderCode=" + orderCodeStr);

            if (navController != null) {

                // --- BẠN CHỈ CẦN SỬA DÒNG NÀY ---
                if ("success".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
                    Log.i(TAG, "Phát hiện trạng thái SUCCESS/PAID. Đang gọi handleSuccess...");
                    handleSuccess(navController);
                } else if ("cancelled".equalsIgnoreCase(status)) {
                    Log.i(TAG, "Phát hiện trạng thái CANCELLED. Đang gọi handleCancellation...");
                    handleCancellation(navController, orderCodeStr);
                } else {
                    Log.w(TAG, "Không nhận diện được status: " + status);
                }
            }

            // Xóa data để tránh xử lý lặp lại
            intent.setData(null);
            return true;
        }
        return false;
    }

    private void handleSuccess(NavController navController) {
        Log.d(TAG, "handleSuccess: Bắt đầu xử lý success.");

        try {
            // ƯU TIÊN 1: Thử chạy action_checkout_to_payment_success
            // Action này đã chứa logic "popUpTo" checkoutFragment mà chúng ta đã setup.
            navController.navigate(R.id.action_checkout_to_payment_success);

            Log.i(TAG, "handleSuccess: Đã navigate dùng action_checkout_to_payment_success (thành công).");

        } catch (Exception e) {
            // LỖI: Rất có thể là do 'currentDestination' không phải là 'checkoutFragment'.
            Log.e(TAG, "handleSuccess: Lỗi khi chạy action (lý do: " + e.getMessage() + "). Thử fallback.");

            // FALLBACK: Dùng navigate 'toàn cục' (global) tới fragment
            // và CHỈ ĐỊNH RÕ RÀNG logic popUpTo.
            try {
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.checkoutFragment, true) // Xóa checkoutFragment khỏi stack
                        .build();

                navController.navigate(R.id.paymentSuccessFragment, null, navOptions);
                Log.i(TAG, "handleSuccess: Đã navigate dùng fallback (global) + popUpTo checkoutFragment.");

            } catch (Exception e2) {
                // Lỗi cả fallback.
                Log.e(TAG, "handleSuccess: Lỗi cả fallback navigate. " + e2.getMessage());
                // (Tùy chọn) Nếu vẫn lỗi, hãy pop về Home
                // navController.popBackStack(R.id.nav_home, false);
            }
        }
    }

    private void handleCancellation(NavController navController, String orderCodeStr) {
        Toast.makeText(activity, "Đang hủy đơn hàng...", Toast.LENGTH_SHORT).show();

        // 1. Gọi API hủy đơn
        if (orderCodeStr != null && !orderCodeStr.isEmpty()) {
            try {
                long orderCode = Long.parseLong(orderCodeStr);
                callCancelOrderApi(orderCode);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Lỗi format orderCode: " + e.getMessage());
            }
        }

        // --- BẮT ĐẦU SỬA ĐỔI ---
        // 2. Điều hướng về Giỏ hàng (theo cách an toàn)

        // Logic cũ (bị lỗi khi app bị kill):
        // boolean popped = navController.popBackStack(R.id.nav_cart, false);
        // if (!popped) {
        //     navController.navigate(R.id.nav_cart); // Lệnh này thất bại
        // }

        // Logic mới: Xóa toàn bộ stack và điều hướng đến nav_cart
        // Đây là cách đảm bảo bạn luôn "hạ cánh" đúng tab
        try {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true) // Xóa toàn bộ back stack
                    .build();

            navController.navigate(R.id.nav_cart, null, navOptions);
            Log.i(TAG, "Đã điều hướng về nav_cart và xóa back stack.");

        } catch (Exception e) {
            Log.e(TAG, "Không thể điều hướng về nav_cart. Lỗi: " + e.getMessage());
            // Fallback cuối cùng: Thử pop về home
            // navController.popBackStack(R.id.nav_home, false);
        }
        // --- KẾT THÚC SỬA ĐỔI ---
    }

    private void callCancelOrderApi(long orderCode) {
        CancelOrderRequest request = new CancelOrderRequest(orderCode);
        ApiConfig.getPaymentApiService().cancelOrder(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "✅ Đã hủy đơn " + orderCode + " trên server.");
                    // Dùng activity.runOnUiThread để đảm bảo Toast hiện đúng thread nếu cần (Retrofit thường đã tự handle, nhưng cho chắc)
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity, "Đã hủy thanh toán.", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Log.e(TAG, "❌ Lỗi hủy đơn trên server: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Lỗi mạng khi hủy đơn: " + t.getMessage());
            }
        });
    }
}