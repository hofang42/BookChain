package com.hofang.bookchainfe.ui.register;

import android.os.Bundle;
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
import com.hofang.bookchainfe.model.AuthResponse;
import com.hofang.bookchainfe.model.RegisterRequest;
import com.hofang.bookchainfe.model.SendOTPRequest;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.AuthApiService;
import com.hofang.bookchainfe.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private TextInputEditText etFullName;
    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private AppCompatButton btnRegister;
    private TextView tvSignIn;
    private ImageButton btnBack;
    
    private AuthApiService authApiService;
    private TokenManager tokenManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service and token manager
        authApiService = ApiConfig.getAuthApiService();
        tokenManager = new TokenManager(requireContext());

        // Initialize views
        etFullName = view.findViewById(R.id.et_full_name);
        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnRegister = view.findViewById(R.id.btn_register);
        tvSignIn = view.findViewById(R.id.tv_sign_in);
        btnBack = view.findViewById(R.id.btn_back);

        // Set click listeners
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInput(fullName, username, email, phone, password, confirmPassword)) {
                performRegister(fullName, username, email, phone, password);
            }
        });

        tvSignIn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_register_to_signin);
        });
    }

    private boolean validateInput(String fullName, String username, String email, String phone, String password, String confirmPassword) {
        // Validate full name
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return false;
        }
        
        if (fullName.length() < 2) {
            etFullName.setError("Họ và tên phải có ít nhất 2 ký tự");
            etFullName.requestFocus();
            return false;
        }
        
        if (!fullName.matches("^[a-zA-ZÀ-ỹ\\s]+$")) {
            etFullName.setError("Họ và tên chỉ được chứa chữ cái và khoảng trắng");
            etFullName.requestFocus();
            return false;
        }

        // Validate username
        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return false;
        }
        
        if (username.length() < 3 || username.length() > 30) {
            etUsername.setError("Tên đăng nhập phải có từ 3-30 ký tự");
            etUsername.requestFocus();
            return false;
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            etUsername.setError("Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới (_)");
            etUsername.requestFocus();
            return false;
        }

        // Validate email
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập địa chỉ email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Vui lòng nhập địa chỉ email hợp lệ (ví dụ: user@example.com)");
            etEmail.requestFocus();
            return false;
        }

        // Validate phone
        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return false;
        }

        if (!phone.matches("^[0-9+\\-\\s()]+$")) {
            etPhone.setError("Số điện thoại không hợp lệ (chỉ chứa số, +, -, khoảng trắng, dấu ngoặc)");
            etPhone.requestFocus();
            return false;
        }
        
        if (phone.length() < 10 || phone.length() > 15) {
            etPhone.setError("Số điện thoại phải có từ 10-15 ký tự");
            etPhone.requestFocus();
            return false;
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            etPassword.setError("Mật khẩu phải có ít nhất 8 ký tự");
            etPassword.requestFocus();
            return false;
        }
        
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")) {
            etPassword.setError("Mật khẩu phải chứa ít nhất: 1 chữ thường (a-z), 1 chữ hoa (A-Z), và 1 số (0-9)");
            etPassword.requestFocus();
            return false;
        }
        
        // Check for weak passwords
        String[] weakPasswords = {"12345678", "password", "Password1", "abcd1234", "Abcd1234"};
        for (String weak : weakPasswords) {
            if (password.equals(weak)) {
                etPassword.setError("Mật khẩu này quá đơn giản, vui lòng chọn mật khẩu khác");
                etPassword.requestFocus();
                return false;
            }
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performRegister(String fullName, String username, String email, String phone, String password) {
        // Show loading state
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang tạo tài khoản...");

        // Create register request
        RegisterRequest request = new RegisterRequest(username, email, password, fullName, phone);

        // Make API call
        Call<ApiResponse<AuthResponse>> call = authApiService.register(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                // Reset button state
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Đăng ký thành công! Vui lòng kiểm tra email để nhận mã xác thực.", Toast.LENGTH_LONG).show();
                        
                        // Send OTP automatically after successful registration
                        sendVerificationOTP(email);
                    } else {
                        // API returned error
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Đăng ký thất bại";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error
                    Toast.makeText(getContext(), "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                // Reset button state
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");
                
                // Network error
                Toast.makeText(getContext(), "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendVerificationOTP(String email) {
        SendOTPRequest request = new SendOTPRequest(email);
        
        Call<ApiResponse<String>> call = authApiService.sendVerificationOTP(request);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        // Navigate to OTP verification screen
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
                        
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_register_to_otp, bundle);
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Failed to send verification code";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to send verification code. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
