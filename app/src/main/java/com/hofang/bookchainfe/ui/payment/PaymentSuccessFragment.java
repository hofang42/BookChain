package com.hofang.bookchainfe.ui.payment; // Gói (package) của bạn có thể khác

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;
import com.google.android.material.button.MaterialButton;
import com.hofang.bookchainfe.R;

public class PaymentSuccessFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout đã được cập nhật
        return inflater.inflate(R.layout.fragment_payment_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnViewOrders = view.findViewById(R.id.btn_view_orders);
        MaterialButton btnBackToHome = view.findViewById(R.id.btn_back_to_home);

        NavController navController = Navigation.findNavController(view);

        /**
         * Xử lý nút Xem Lịch sử Đơn hàng
         */
        btnViewOrders.setOnClickListener(v -> {
            // Điều hướng này sẽ pop (xóa) mọi thứ quay về 'nav_home',
            // sau đó mới điều hướng đến 'orderHistoryFragment'.
            // Back stack của bạn sẽ là: Home -> OrderHistory
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_home, false) // Pop về Home, nhưng giữ Home lại
                    .build();

            try {
                // Đảm bảo R.id.orderHistoryFragment tồn tại trong nav_graph.xml
                navController.navigate(R.id.orderHistoryFragment, null, navOptions);
            } catch (Exception e) {
                // Nếu có lỗi (vd: không tìm thấy fragment), chỉ quay về Home
                goHome(navController);
            }
        });

        /**
         * Xử lý nút Quay về Trang chủ
         */
        btnBackToHome.setOnClickListener(v -> {
            goHome(navController);
        });
    }

    /**
     * Hàm trợ giúp để xóa sạch back stack và quay về Home
     */
    private void goHome(NavController navController) {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true) // Xóa sạch toàn bộ back stack
                .build();
        navController.navigate(R.id.nav_home, null, navOptions);
    }
}