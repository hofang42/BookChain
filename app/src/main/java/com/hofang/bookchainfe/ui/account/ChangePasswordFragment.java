package com.hofang.bookchainfe.ui.account;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.ChangePasswordRequest;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.AuthApiService;
import com.hofang.bookchainfe.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {

    private static final String TAG = "ChangePasswordFragment";

    // UI Components
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private TextInputLayout currentPasswordLayout, newPasswordLayout, confirmPasswordLayout;
    private MaterialButton btnChangePassword;
    private View progressOverlay;

    // Data & Network
    private TokenManager tokenManager;
    private AuthApiService authApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        initializeData();
        setupListeners(view);
    }

    private void initializeViews(View view) {
        // TextInputLayouts
        currentPasswordLayout = view.findViewById(R.id.current_password_layout);
        newPasswordLayout = view.findViewById(R.id.new_password_layout);
        confirmPasswordLayout = view.findViewById(R.id.confirm_password_layout);

        // EditTexts
        etCurrentPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);

        // Button
        btnChangePassword = view.findViewById(R.id.btn_change_password);

        // Progress
        progressOverlay = view.findViewById(R.id.progress_overlay);
    }

    private void initializeData() {
        tokenManager = new TokenManager(requireContext());
        authApiService = ApiConfig.getRetrofit().create(AuthApiService.class);
    }

    private void setupListeners(View view) {
        // Toolbar back button
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        // Change Password button
        btnChangePassword.setOnClickListener(v -> {
            if (validateInputs()) {
                changePassword();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Get values
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Clear previous errors
        currentPasswordLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        // Validate Current Password
        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordLayout.setError("Current password is required");
            isValid = false;
        }

        // Validate New Password
        if (TextUtils.isEmpty(newPassword)) {
            newPasswordLayout.setError("New password is required");
            isValid = false;
        } else if (newPassword.length() < 6) {
            newPasswordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (newPassword.equals(currentPassword)) {
            newPasswordLayout.setError("New password must be different from current password");
            isValid = false;
        }

        // Validate Confirm Password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your new password");
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private void changePassword() {
        // Show loading
        showLoading(true);

        // Get values
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Create request
        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword, confirmPassword);

        // Get token
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            showLoading(false);
            Toast.makeText(requireContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call API
        String authHeader = "Bearer " + token;
        Call<ApiResponse<String>> call = authApiService.changePassword(authHeader, request);

        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call,
                                   @NonNull Response<ApiResponse<String>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();

                        // Navigate back
                        if (getView() != null) {
                            Navigation.findNavController(getView()).navigateUp();
                        }
                    } else {
                        String errorMsg = apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : "Failed to change password";
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();

                        // If current password is wrong, show error on the field
                        if (errorMsg.toLowerCase().contains("current password")) {
                            currentPasswordLayout.setError("Incorrect current password");
                        }
                    }
                } else {
                    // Handle HTTP error codes
                    if (response.code() == 401) {
                        currentPasswordLayout.setError("Incorrect current password");
                        Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Change password failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error", t);
            }
        });
    }

    private void showLoading(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnChangePassword.setEnabled(!show);
    }
}
