package com.hofang.bookchainfe.ui.forgotpassword;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.SendOTPRequest;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.AuthApiService;
import com.hofang.bookchainfe.utils.ErrorMessageParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordFragment extends Fragment {
    private static final String TAG = "ForgotPasswordFragment";

    private TextInputEditText etEmail;
    private AppCompatButton btnSendOtp;
    private ImageButton btnBack;
    private TextView tvBackToSignIn;
    
    private AuthApiService authApiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service
        authApiService = ApiConfig.getAuthApiService();

        // Initialize views
        etEmail = view.findViewById(R.id.et_email);
        btnSendOtp = view.findViewById(R.id.btn_send_otp);
        btnBack = view.findViewById(R.id.btn_back);
        tvBackToSignIn = view.findViewById(R.id.tv_back_to_signin);

        // Set click listeners
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (validateEmail(email)) {
                sendOtpToEmail(email);
            }
        });

        tvBackToSignIn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void sendOtpToEmail(String email) {
        // Show loading state
        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Đang gửi...");

        // Create request
        SendOTPRequest request = new SendOTPRequest(email);

        // Make API call
        Call<ApiResponse<String>> call = authApiService.sendPasswordResetOTP(request);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                // Reset button state
                btnSendOtp.setEnabled(true);
                btnSendOtp.setText("Gửi mã OTP");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Mã OTP đã được gửi đến email của bạn", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to OTP verification with email parameter
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
                        bundle.putString("type", "forgot_password");
                        
                        NavController navController = Navigation.findNavController(getView());
                        navController.navigate(R.id.action_forgotPassword_to_forgotPasswordOtp, bundle);
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Gửi OTP thất bại";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Handle HTTP error codes - parse error message from response body
                    String defaultMessage = "Gửi OTP thất bại. Vui lòng kiểm tra email và thử lại.";
                    
                    if (response.code() == 404) {
                        defaultMessage = "Email không tồn tại hoặc chưa được xác thực trong hệ thống";
                    }
                    
                    String errorMessage = ErrorMessageParser.parseErrorMessage(response, defaultMessage);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // Reset button state
                btnSendOtp.setEnabled(true);
                btnSendOtp.setText("Gửi mã OTP");
                
                Toast.makeText(getContext(), "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
