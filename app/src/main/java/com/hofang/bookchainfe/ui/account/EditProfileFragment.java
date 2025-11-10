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
import com.hofang.bookchainfe.model.UpdateProfileRequest;
import com.hofang.bookchainfe.model.UserProfileResponse;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.AuthApiService;
import com.hofang.bookchainfe.utils.TokenManager;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    // UI Components
    private TextInputEditText etUsername, etEmail, etFullName, etPhone;
    private TextInputLayout usernameLayout, emailLayout, fullNameLayout, phoneLayout;
    private MaterialButton btnSave;
    private View progressOverlay;

    // Data & Network
    private TokenManager tokenManager;
    private AuthApiService authApiService;

    // Validation patterns
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s()]{10,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ỹ\\s]{2,100}$");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        initializeData();
        loadCurrentProfile();
        setupListeners(view);
    }

    private void initializeViews(View view) {
        // TextInputLayouts
        usernameLayout = view.findViewById(R.id.username_layout);
        emailLayout = view.findViewById(R.id.email_layout);
        fullNameLayout = view.findViewById(R.id.fullname_layout);
        phoneLayout = view.findViewById(R.id.phone_layout);

        // EditTexts
        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etFullName = view.findViewById(R.id.et_fullname);
        etPhone = view.findViewById(R.id.et_phone);

        // Button
        btnSave = view.findViewById(R.id.btn_save);

        // Progress
        progressOverlay = view.findViewById(R.id.progress_overlay);
    }

    private void initializeData() {
        tokenManager = new TokenManager(requireContext());
        authApiService = ApiConfig.getRetrofit().create(AuthApiService.class);
    }

    private void loadCurrentProfile() {
        // Load data from TokenManager
        String username = tokenManager.getUsername();
        String email = tokenManager.getEmail();
        String fullName = tokenManager.getFullName();
        String phone = tokenManager.getPhone();

        // Set to UI
        etUsername.setText(username != null ? username : "");
        etEmail.setText(email != null ? email : "");
        etFullName.setText(fullName != null ? fullName : "");
        etPhone.setText(phone != null ? phone : "");
    }

    private void setupListeners(View view) {
        // Toolbar back button
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        // Save button
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfile();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Get values
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Clear previous errors
        fullNameLayout.setError(null);
        phoneLayout.setError(null);

        // Validate Full Name
        if (TextUtils.isEmpty(fullName)) {
            fullNameLayout.setError("Full name is required");
            isValid = false;
        } else if (!NAME_PATTERN.matcher(fullName).matches()) {
            fullNameLayout.setError("Full name must be 2-100 characters, letters and spaces only");
            isValid = false;
        }

        // Validate Phone (optional but must be valid if provided)
        if (!TextUtils.isEmpty(phone) && !PHONE_PATTERN.matcher(phone).matches()) {
            phoneLayout.setError("Phone must be 10-15 digits");
            isValid = false;
        }

        return isValid;
    }

    private void updateProfile() {
        // Show loading
        showLoading(true);

        // Get values
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Create request - send null instead of empty string for phone
        UpdateProfileRequest request = new UpdateProfileRequest(
                fullName,
                phone.isEmpty() ? null : phone
        );

        // Get token
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            showLoading(false);
            Toast.makeText(requireContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call API
        String authHeader = "Bearer " + token;
        Call<ApiResponse<UserProfileResponse>> call = authApiService.updateProfile(authHeader, request);

        call.enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserProfileResponse>> call,
                                   @NonNull Response<ApiResponse<UserProfileResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserProfileResponse> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        // Update TokenManager with new data
                        UserProfileResponse profileResponse = apiResponse.getData();
                        if (profileResponse.getUser() != null) {
                            tokenManager.updateProfile(
                                    profileResponse.getUser().getFullName(),
                                    profileResponse.getUser().getPhone()
                            );
                        }

                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                        // Navigate back
                        if (getView() != null) {
                            Navigation.findNavController(getView()).navigateUp();
                        }
                    } else {
                        String errorMsg = apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : "Failed to update profile";
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserProfileResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error", t);
            }
        });
    }

    private void showLoading(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}
