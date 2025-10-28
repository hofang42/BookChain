package com.hofang.bookchainfe.ui.forgotpassword;

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
import com.hofang.bookchainfe.model.ResetPasswordRequest;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.AuthApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordFragment extends Fragment {
    private static final String TAG = "ResetPasswordFragment";

    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private AppCompatButton btnResetPassword;
    private ImageButton btnBack;
    private TextView tvBackToSignIn;
    
    private AuthApiService authApiService;
    private String email;
    private String otp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service
        authApiService = ApiConfig.getAuthApiService();

        // Get email and otp from arguments
        if (getArguments() != null) {
            email = getArguments().getString("email");
            otp = getArguments().getString("otp");
        }

        // Initialize views
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnResetPassword = view.findViewById(R.id.btn_reset_password);
        btnBack = view.findViewById(R.id.btn_back);
        tvBackToSignIn = view.findViewById(R.id.tv_back_to_signin);

        // Set click listeners
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            
            if (validatePasswords(newPassword, confirmPassword)) {
                resetPassword(newPassword);
            }
        });

        tvBackToSignIn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_resetPassword_to_signin);
        });
    }

    private boolean validatePasswords(String newPassword, String confirmPassword) {
        if (newPassword.isEmpty()) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới");
            etNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etNewPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void resetPassword(String newPassword) {
        // Show loading state
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Đang đặt lại...");

        // Create request
        ResetPasswordRequest request = new ResetPasswordRequest(email, otp, newPassword);

        // Make API call
        Call<ApiResponse<AuthResponse>> call = authApiService.resetPassword(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                // Reset button state
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Đặt lại mật khẩu");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Toast.makeText(getContext(), "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate back to sign in
                        NavController navController = Navigation.findNavController(getView());
                        navController.navigate(R.id.action_resetPassword_to_signin);
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Đặt lại mật khẩu thất bại";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Đặt lại mật khẩu thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                // Reset button state
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Đặt lại mật khẩu");
                
                Toast.makeText(getContext(), "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
