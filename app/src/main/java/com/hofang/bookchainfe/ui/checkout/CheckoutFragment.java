package com.hofang.bookchainfe.ui.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.CartResponse;
import com.hofang.bookchainfe.model.CreatePaymentRequest;
import com.hofang.bookchainfe.model.CreatePaymentResponse;
import com.hofang.bookchainfe.network.ApiConfig;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment";

    private MaterialButton btnPay;
    private ImageView btnBack;
    private TextView tvAddress;
    private RadioGroup radioGroupPayment;
    private ProgressBar progressBar;

    private float totalPrice = 0;
    // Danh sách sản phẩm nhận từ CartFragment
    private List<CartResponse.CartItem> cartItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        getBundleData();
        setupUI();
        setupListeners();
    }

    private void initViews(View view) {
        btnPay = view.findViewById(R.id.btn_pay);
        btnBack = view.findViewById(R.id.btn_back);
        tvAddress = view.findViewById(R.id.tv_address);
        radioGroupPayment = view.findViewById(R.id.radio_group_payment);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    @SuppressWarnings("unchecked")
    private void getBundleData() {
        if (getArguments() != null) {
            totalPrice = getArguments().getFloat("totalPrice", 0);

            // Nhận danh sách cartItems (đã được ép kiểu Serializable bên CartFragment)
            try {
                cartItems = (List<CartResponse.CartItem>) getArguments().getSerializable("cartItems");
            } catch (ClassCastException e) {
                Log.e(TAG, "Lỗi ép kiểu cartItems: " + e.getMessage());
            }
        }

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
    }

    private void setupUI() {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        btnPay.setText("Pay " + currencyFormatter.format(totalPrice));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        btnPay.setOnClickListener(v -> {
            int selectedId = radioGroupPayment.getCheckedRadioButtonId();
            if (selectedId == R.id.radio_cash_on_delivery) {
                handleCodPayment();
            } else if (selectedId == R.id.radio_credit_card) {
                handleOnlinePayment();
            }
        });
    }

    private void handleCodPayment() {
        // Demo: Chuyển thẳng đến màn hình thành công
        Toast.makeText(getContext(), "Đặt hàng COD thành công!", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigate(R.id.action_checkout_to_payment_success);
    }

    private void handleOnlinePayment() {
        String address = tvAddress.getText().toString();

        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng rỗng!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // --- 1. CHUẨN BỊ DỮ LIỆU REQUEST ---
        List<CreatePaymentRequest.PaymentItem> paymentItems = new ArrayList<>();
        for (CartResponse.CartItem item : cartItems) {

            String bookId = item.getBook().getId(); // Hoặc getId(), tùy model của bạn
            float price = (float) item.getBook().getPrice(); // Hoặc getPriceValue(), tùy model

            paymentItems.add(new CreatePaymentRequest.PaymentItem(
                    bookId,
                    item.getQuantity(),
                    price
            ));
        }

        CreatePaymentRequest request = new CreatePaymentRequest(paymentItems, totalPrice, address);

        // --- 2. GỌI API ---
        ApiConfig.getPaymentApiService().createPaymentLink(request).enqueue(new Callback<CreatePaymentResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreatePaymentResponse> call, @NonNull Response<CreatePaymentResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    String checkoutUrl = response.body().getCheckoutUrl();
                    if (checkoutUrl != null && !checkoutUrl.isEmpty()) {
                        // Mở trình duyệt để thanh toán
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Lỗi: Không có link thanh toán", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Log lỗi từ server (ví dụ: 400 Bad Request do sai ID sách)
                    Log.e(TAG, "API Error Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "API Error Body: " + response.errorBody().string());
                        }
                    } catch (Exception e) { e.printStackTrace(); }

                    Toast.makeText(getContext(), "Tạo đơn thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreatePaymentResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network failure", t);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnPay.setEnabled(!isLoading);
    }
}