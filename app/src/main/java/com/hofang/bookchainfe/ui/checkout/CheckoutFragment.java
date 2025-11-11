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

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private NumberFormat currencyFormatter;

    // Data
    private Address defaultAddress;
    private List<CartItem> cartItems = new ArrayList<>();
    private float totalPrice = 0f;

    // --- 1. THÊM BIẾN ĐỂ LƯU branchId ---
    private String branchId;

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

        // Đọc dữ liệu được truyền từ CartFragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.totalPrice = arguments.getFloat("totalPrice", 0f);

            // --- 2. LẤY branchId TỪ BUNDLE ---
            this.branchId = arguments.getString("branchId");

            Serializable itemsSerializable = arguments.getSerializable("cartItems");
            if (itemsSerializable instanceof List) {
                try {
                    this.cartItems = (List<CartItem>) itemsSerializable;
                } catch (ClassCastException e) {
                    Log.e(TAG, "Lỗi khi cast cartItems", e);
                    this.cartItems = new ArrayList<>();
                }
            }
            Log.d(TAG, "Nhận được " + this.cartItems.size() + " items, Tổng tiền: " + this.totalPrice + ", BranchID: " + this.branchId);
        } else {
            Log.e(TAG, "Arguments bị null, không nhận được dữ liệu giỏ hàng!");
        }

        Locale localeVN = new Locale("vi", "VN");
        currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

        // Initialize views
        tvRecipientName = view.findViewById(R.id.tv_recipient_name);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvFullAddress = view.findViewById(R.id.tv_full_address);
        tvPostalCode = view.findViewById(R.id.tv_postal_code);
        cardAddress = view.findViewById(R.id.card_address);
        btnChangeAddress = view.findViewById(R.id.btn_change_address);
        btnAddAddress = view.findViewById(R.id.btn_add_new_delivery_address);
        progressBar = view.findViewById(R.id.progress_bar);

        // ... (code init ApiConfig, TokenManager, các nút... giữ nguyên) ...
        addressApiService = ApiConfig.getRetrofit().create(AddressApiService.class);
        tokenManager = new TokenManager(requireContext());
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
        if (btnChangeAddress != null) {
            btnChangeAddress.setOnClickListener(v -> {
                AddAddressBottomSheet bottomSheet = AddAddressBottomSheet.newInstance(defaultAddress);
                bottomSheet.setOnAddressSavedListener(() -> loadDefaultAddress());
                bottomSheet.show(getParentFragmentManager(), "add_address_sheet");
            });
        }
        if (btnAddAddress != null) {
            btnAddAddress.setOnClickListener(v -> {
                AddAddressBottomSheet bottomSheet = new AddAddressBottomSheet();
                bottomSheet.setOnAddressSavedListener(() -> loadDefaultAddress());
                bottomSheet.show(getParentFragmentManager(), "add_address_sheet");
            });
        }
        MaterialButton btnPay = view.findViewById(R.id.btn_pay);
        if (btnPay != null) {
            String buttonText = "Thanh toán " + currencyFormatter.format(this.totalPrice);
            btnPay.setText(buttonText);
            btnPay.setOnClickListener(v -> {
                if (defaultAddress == null) {
                    Toast.makeText(requireContext(), "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                handleOnlinePayment();
            });
        }
        loadDefaultAddress();
    }

    private void loadDefaultAddress() {
        // ... (hàm này giữ nguyên) ...
        showLoading(true);
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            showLoading(false);
            showNoAddressState();
            Toast.makeText(requireContext(), "Chưa xác thực", Toast.LENGTH_SHORT).show();
            return;
        }
        String authHeader = "Bearer " + token;
        addressApiService.getDefaultAddress(authHeader).enqueue(new Callback<ApiResponse<AddressApiService.AddressResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AddressApiService.AddressResponse>> call, Response<ApiResponse<AddressApiService.AddressResponse>> response) {
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
                        Log.w(TAG, "Không tìm thấy địa chỉ mặc định: " + apiResponse.getMessage());
                    }
                } else {
                    showNoAddressState();
                    Log.e(TAG, "Phản hồi không thành công: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<AddressApiService.AddressResponse>> call, Throwable t) {
                showLoading(false);
                showNoAddressState();
                Toast.makeText(requireContext(), "Tải địa chỉ thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Tải địa chỉ mặc định thất bại", t);
            }
        });
    }

    private void displayAddress(Address address) {
        // ... (hàm này giữ nguyên) ...
        if (cardAddress != null) cardAddress.setVisibility(View.VISIBLE);
        if (btnChangeAddress != null) btnChangeAddress.setVisibility(View.VISIBLE);
        if (btnAddAddress != null) btnAddAddress.setVisibility(View.GONE);
        if (tvRecipientName != null) tvRecipientName.setText(address.getRecipientName());
        if (tvPhone != null) tvPhone.setText("SĐT: " + address.getPhoneNumber());
        if (tvFullAddress != null) {
            StringBuilder fullAddress = new StringBuilder();
            if (address.getStreet() != null && !address.getStreet().isEmpty()) fullAddress.append(address.getStreet());
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
        if (tvPostalCode != null) {
            if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) {
                tvPostalCode.setVisibility(View.VISIBLE);
                tvPostalCode.setText("Mã bưu điện: " + address.getPostalCode());
            } else {
                tvPostalCode.setVisibility(View.GONE);
            }
        }
    }

    private void showNoAddressState() {
        // ... (hàm này giữ nguyên) ...
        defaultAddress = null;
        if (cardAddress != null) cardAddress.setVisibility(View.GONE);
        if (btnChangeAddress != null) btnChangeAddress.setVisibility(View.GONE);
        if (btnAddAddress != null) btnAddAddress.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void handleOnlinePayment() {
        String address = tvFullAddress.getText().toString();

        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng rỗng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 3. THÊM KIỂM TRA branchId TRƯỚC KHI GỌI API ---
        if (this.branchId == null || this.branchId.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Không có ID chi nhánh!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "handleOnlinePayment: branchId is null or empty!");
            return;
        }
        // --- KẾT THÚC THÊM MỚI ---

        showLoading(true);

        // --- 1. CHUẨN BỊ DỮ LIỆU REQUEST ---
        List<CreatePaymentRequest.PaymentItem> paymentItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            String bookId = item.getBook().getId();
            // Lấy giá cuối cùng (đã bao gồm discount)
            float price = item.getBook().getFinalPrice().floatValue();

            paymentItems.add(new CreatePaymentRequest.PaymentItem(
                    bookId,
                    item.getQuantity(),
                    price
            ));
        }

        // --- 4. CẬP NHẬT CONSTRUCTOR, THÊM branchId ---
        CreatePaymentRequest request = new CreatePaymentRequest(
                paymentItems,
                totalPrice,
                address,
                this.branchId // <-- Gửi branchId đi
        );

        // --- 2. GỌI API ---
        ApiConfig.getPaymentApiService().createPaymentLink(request).enqueue(new Callback<CreatePaymentResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreatePaymentResponse> call, @NonNull Response<CreatePaymentResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    String checkoutUrl = response.body().getCheckoutUrl();
                    if (checkoutUrl != null && !checkoutUrl.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Lỗi: Không có link thanh toán", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "API Error Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "API Error Body: " + errorBody);
                            // Hiển thị lỗi từ server cho người dùng
                            Toast.makeText(getContext(), errorBody, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Tạo đơn thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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