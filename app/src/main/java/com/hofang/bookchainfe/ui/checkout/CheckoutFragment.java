package com.hofang.bookchainfe.ui.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Address;
import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.CartItem;
import com.hofang.bookchainfe.model.CreatePaymentRequest;
import com.hofang.bookchainfe.model.CreatePaymentResponse;
import com.hofang.bookchainfe.network.AddressApiService;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.ui.address.AddAddressBottomSheet;
import com.hofang.bookchainfe.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment";

    // Views
    private TextView tvRecipientName;
    private TextView tvPhone;
    private TextView tvFullAddress;
    private TextView tvPostalCode;
    private MaterialCardView cardAddress;
    private MaterialButton btnChangeAddress;
    private MaterialButton btnAddAddress;
    private ProgressBar progressBar;

    // API and utilities
    private AddressApiService addressApiService;
    private TokenManager tokenManager;

    // Data
    private Address defaultAddress;
    private List<CartItem> cartItems = new ArrayList<>();
    private float totalPrice = 0f;

    public CheckoutFragment() {
        // Required empty public constructor
    }

    public static CheckoutFragment newInstance() {
        return new CheckoutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvRecipientName = view.findViewById(R.id.tv_recipient_name);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvFullAddress = view.findViewById(R.id.tv_full_address);
        tvPostalCode = view.findViewById(R.id.tv_postal_code);
        cardAddress = view.findViewById(R.id.card_address);
        btnChangeAddress = view.findViewById(R.id.btn_change_address);
        btnAddAddress = view.findViewById(R.id.btn_add_new_delivery_address);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize API service and token manager
        addressApiService = ApiConfig.getRetrofit().create(AddressApiService.class);
        tokenManager = new TokenManager(requireContext());

        // Setup back button
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Setup Change address button: open bottom sheet to edit/select address
        if (btnChangeAddress != null) {
            btnChangeAddress.setOnClickListener(v -> {
                // Show Add/Edit Address bottom sheet prefilled with current default address
                AddAddressBottomSheet bottomSheet = AddAddressBottomSheet.newInstance(defaultAddress);
                bottomSheet.setOnAddressSavedListener(() -> loadDefaultAddress());
                bottomSheet.show(getParentFragmentManager(), "add_address_sheet");
            });
        }

        // Setup Add New Delivery Address button
        if (btnAddAddress != null) {
            btnAddAddress.setOnClickListener(v -> {
                // Show bottom sheet to add new address
                AddAddressBottomSheet bottomSheet = new AddAddressBottomSheet();
                bottomSheet.setOnAddressSavedListener(() -> loadDefaultAddress());
                bottomSheet.show(getParentFragmentManager(), "add_address_sheet");
            });
        }

        // Setup Pay button
        MaterialButton btnPay = view.findViewById(R.id.btn_pay);
        if (btnPay != null) {
            btnPay.setOnClickListener(v -> {
                if (defaultAddress == null) {
                    Toast.makeText(requireContext(), "Please select a delivery address", Toast.LENGTH_SHORT).show();
                    return;
                }
                handleOnlinePayment();
            });
        }

        // Load default address
        loadDefaultAddress();
    }

    private void loadDefaultAddress() {
        showLoading(true);

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            showLoading(false);
            showNoAddressState();
            Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;
        addressApiService.getDefaultAddress(authHeader).enqueue(new Callback<ApiResponse<AddressApiService.AddressResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AddressApiService.AddressResponse>> call,
                                   Response<ApiResponse<AddressApiService.AddressResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AddressApiService.AddressResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        defaultAddress = apiResponse.getData().getAddress();
                        if (defaultAddress != null) {
                            displayAddress(defaultAddress);
                        } else {
                            showNoAddressState();
                        }
                    } else {
                        showNoAddressState();
                        Log.w(TAG, "No default address found: " + apiResponse.getMessage());
                    }
                } else {
                    showNoAddressState();
                    Log.e(TAG, "Response unsuccessful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AddressApiService.AddressResponse>> call, Throwable t) {
                showLoading(false);
                showNoAddressState();
                Toast.makeText(requireContext(), "Failed to load address: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load default address", t);
            }
        });
    }

    private void displayAddress(Address address) {
        if (cardAddress != null) {
            cardAddress.setVisibility(View.VISIBLE);
        }
        if (btnChangeAddress != null) {
            btnChangeAddress.setVisibility(View.VISIBLE);
        }
        if (btnAddAddress != null) {
            btnAddAddress.setVisibility(View.GONE);
        }

        // Set recipient name
        if (tvRecipientName != null) {
            tvRecipientName.setText(address.getRecipientName());
        }

        // Set phone number
        if (tvPhone != null) {
            tvPhone.setText("Phone: " + address.getPhoneNumber());
        }

        // Set full address
        if (tvFullAddress != null) {
            StringBuilder fullAddress = new StringBuilder();
            if (address.getStreet() != null && !address.getStreet().isEmpty()) {
                fullAddress.append(address.getStreet());
            }
            if (address.getWard() != null && !address.getWard().isEmpty()) {
                if (fullAddress.length() > 0) fullAddress.append(", ");
                fullAddress.append(address.getWard());
            }
            if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
                if (fullAddress.length() > 0) fullAddress.append(", ");
                fullAddress.append(address.getDistrict());
            }
            if (address.getCity() != null && !address.getCity().isEmpty()) {
                if (fullAddress.length() > 0) fullAddress.append(", ");
                fullAddress.append(address.getCity());
            }
            tvFullAddress.setText(fullAddress.toString());
        }

        // Set postal code
        if (tvPostalCode != null) {
            if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) {
                tvPostalCode.setVisibility(View.VISIBLE);
                tvPostalCode.setText("Postal Code: " + address.getPostalCode());
            } else {
                tvPostalCode.setVisibility(View.GONE);
            }
        }
    }

    private void showNoAddressState() {
        defaultAddress = null;
        
        if (cardAddress != null) {
            cardAddress.setVisibility(View.GONE);
        }
        if (btnChangeAddress != null) {
            btnChangeAddress.setVisibility(View.GONE);
        }
        if (btnAddAddress != null) {
            btnAddAddress.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void handleOnlinePayment() {
        // Build full address string
        String address = tvFullAddress.getText().toString();

        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng rỗng!", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // --- 1. CHUẨN BỊ DỮ LIỆU REQUEST ---
        List<CreatePaymentRequest.PaymentItem> paymentItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            String bookId = item.getBook().getId();
            float price = (float) item.getBook().getFinalPrice();

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
                showLoading(false);
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
                    } catch (Exception e) { 
                        e.printStackTrace(); 
                    }

                    Toast.makeText(getContext(), "Tạo đơn thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreatePaymentResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network failure", t);
            }
        });
    }
}
