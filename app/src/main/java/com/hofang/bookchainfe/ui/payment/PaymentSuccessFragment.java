// PaymentSuccessFragment.java
package com.hofang.bookchainfe.ui.payment; // Thay bằng package của bạn

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.hofang.bookchainfe.R;

public class PaymentSuccessFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnClose = view.findViewById(R.id.btn_close);
        NavController navController = Navigation.findNavController(view);

        btnClose.setOnClickListener(v -> {
            // TODO: Điều hướng về màn hình Home (thay vì back lại checkout)
            // Ví dụ: navController.navigate(R.id.action_global_to_homeFragment);

            // Tạm thời là back (nếu không có action global)
            navController.popBackStack(R.id.checkoutFragment, true); // Xóa checkout khỏi backstack
        });
    }
}