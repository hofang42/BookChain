package com.hofang.bookchainfe.ui.signin;

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
import com.hofang.bookchainfe.model.LoginRequest;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.AuthApiService;
import com.hofang.bookchainfe.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInFragment extends Fragment {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private AppCompatButton btnSignIn;
    private TextView tvForgotPassword;
    private TextView tvRegister;
    private ImageButton btnBack;
    
    private AuthApiService authApiService;
    private TokenManager tokenManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service and token manager
        authApiService = ApiConfig.getAuthApiService();
        tokenManager = new TokenManager(requireContext());

        // Initialize views
        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        btnSignIn = view.findViewById(R.id.btn_sign_in);
        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
        tvRegister = view.findViewById(R.id.tv_register);
        btnBack = view.findViewById(R.id.btn_back);

        // Set click listeners
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        btnSignIn.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(username, password)) {
                performSignIn(username, password);
            }
        });

        tvForgotPassword.setOnClickListener(v -> {
            // TODO: Navigate to forgot password screen
            Toast.makeText(getContext(), "Tính năng quên mật khẩu sẽ có sớm", Toast.LENGTH_SHORT).show();
        });

        tvRegister.setOnClickListener(v -> {
            // Navigate to Register screen
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_signin_to_register);
        });
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập hoặc email");
            etUsername.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performSignIn(String username, String password) {
        // Show loading state
        btnSignIn.setEnabled(false);
        btnSignIn.setText("Đang đăng nhập...");

        // Create login request
        LoginRequest request = new LoginRequest(username, password);

        // Make API call
        Call<ApiResponse<AuthResponse>> call = authApiService.login(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                // Reset button state
                btnSignIn.setEnabled(true);
                btnSignIn.setText("Đăng nhập");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authResponse = apiResponse.getData();
                        
                        // Save user session
                        tokenManager.saveUserSession(
                            authResponse.getToken(),
                            authResponse.getUser().getId(),
                            authResponse.getUser().getUsername(),
                            authResponse.getUser().getEmail(),
                            authResponse.getUser().getFullName()
                        );

                        Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        // Show bottom navigation when entering main app
                        if (getActivity() != null) {
                            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                            if (bottomNav != null) {
                                bottomNav.setVisibility(View.VISIBLE);
                            }
                        }

                        // Navigate to home
                        NavController navController = Navigation.findNavController(getView());
                        navController.navigate(R.id.action_signin_to_home);
                    } else {
                        // API returned error
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Đăng nhập thất bại";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error
                    Toast.makeText(getContext(), "Đăng nhập thất bại. Vui lòng kiểm tra thông tin đăng nhập.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                // Reset button state
                btnSignIn.setEnabled(true);
                btnSignIn.setText("Đăng nhập");
                
                // Network error
                Toast.makeText(getContext(), "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
