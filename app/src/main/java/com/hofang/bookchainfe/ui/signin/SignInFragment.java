package com.hofang.bookchainfe.ui.signin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.AuthResponse;
import com.hofang.bookchainfe.model.GoogleSignInRequest;
import com.hofang.bookchainfe.model.LoginRequest;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.AuthApiService;
import com.hofang.bookchainfe.utils.ErrorMessageParser;
import com.hofang.bookchainfe.utils.GoogleSignInHelper;
import com.hofang.bookchainfe.utils.LoadingOverlay;
import com.hofang.bookchainfe.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Handler;
import android.os.Looper;

public class SignInFragment extends Fragment implements GoogleSignInHelper.GoogleSignInListener {
    private static final String TAG = "SignInFragment";

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private AppCompatButton btnSignIn;
    private AppCompatButton btnGoogleSignIn;
    private TextView tvForgotPassword;
    private TextView tvRegister;
    private ImageButton btnBack;
    
    private AuthApiService authApiService;
    private TokenManager tokenManager;
    private GoogleSignInHelper googleSignInHelper;
    private LoadingOverlay loadingOverlay;
    
    // Activity result launcher for Google Sign-In
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (googleSignInHelper != null) {
                    googleSignInHelper.handleSignInResult(result.getData());
                } else {
                    // Reset loading state if helper is null
                    loadingOverlay.hide();
                    setGoogleSignInLoading(false);
                }
            }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service and token manager
        authApiService = ApiConfig.getAuthApiService();
        tokenManager = new TokenManager(requireContext());
        
        // Initialize Google Sign-In helper
        googleSignInHelper = new GoogleSignInHelper(requireContext());
        googleSignInHelper.setListener(this);
        
        // Initialize loading overlay
        loadingOverlay = new LoadingOverlay(requireActivity());

        // Initialize views
        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        btnSignIn = view.findViewById(R.id.btn_sign_in);
        btnGoogleSignIn = view.findViewById(R.id.btn_google_sign_in);
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
            // Navigate to forgot password screen
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_signin_to_forgotPassword);
        });

        tvRegister.setOnClickListener(v -> {
            // Navigate to Register screen
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_signin_to_register);
        });
        
        // Google Sign-In button click listener
        btnGoogleSignIn.setOnClickListener(v -> {
            Log.d(TAG, "Google Sign-In button clicked");
            // Show loading overlay
            loadingOverlay.show("Đang đăng nhập...", "Đang mở Google Sign-In");
            setGoogleSignInLoading(true);
            googleSignInHelper.signIn(googleSignInLauncher);
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
                    
                    // Debug logs
                    Log.d("SignIn", "Response successful, apiResponse.isSuccess(): " + apiResponse.isSuccess());
                    if (apiResponse.getData() != null) {
                        AuthResponse authResponse = apiResponse.getData();
                        Log.d("SignIn", "AuthResponse - requiresVerification: " + authResponse.isRequiresVerification());
                        Log.d("SignIn", "AuthResponse - email: " + authResponse.getEmail());
                        Log.d("SignIn", "AuthResponse - user: " + (authResponse.getUser() != null));
                        Log.d("SignIn", "AuthResponse - token: " + (authResponse.getToken() != null));
                    } else {
                        Log.d("SignIn", "AuthResponse data is null");
                    }
                    
                    if (apiResponse.isSuccess()) {
                        AuthResponse authResponse = apiResponse.getData();
                        
                        // Check if requires verification (pending account)
                        if (authResponse != null && authResponse.isRequiresVerification()) {
                            String responseEmail = authResponse.getEmail();
                            if (responseEmail != null && !responseEmail.isEmpty()) {
                                // Navigate to OTP screen for pending account
                                Toast.makeText(getContext(), "Tài khoản chưa được xác thực. Mã OTP đã được gửi đến email của bạn.", Toast.LENGTH_LONG).show();
                                navigateToOTPVerification(responseEmail);
                            } else {
                                Toast.makeText(getContext(), "Tài khoản chưa được xác thực. Vui lòng kiểm tra email.", Toast.LENGTH_LONG).show();
                            }
                            return; // Exit early to prevent showing error message
                        } else if (authResponse != null && authResponse.getUser() != null && authResponse.getToken() != null) {
                            // Normal login flow - save session and navigate to home
                            tokenManager.saveUserSession(
                                authResponse.getToken(),
                                authResponse.getUser().getId(),
                                authResponse.getUser().getUsername(),
                                authResponse.getUser().getEmail(),
                                authResponse.getUser().getFullName()
                            );

                            // Navigate immediately with strong, visible animation
                            NavController navController = Navigation.findNavController(getView());
                            androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                                .setEnterAnim(R.anim.slide_up_fade_in)
                                .setExitAnim(R.anim.slide_down_fade_out)
                                .setPopEnterAnim(R.anim.slide_in_left)
                                .setPopExitAnim(R.anim.slide_out_right)
                                .build();
                            navController.navigate(R.id.action_signin_to_home, null, navOptions);

                            // Show bottom navigation when entering main app (after navigation starts)
                            if (getActivity() != null) {
                                View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                                if (bottomNav != null) {
                                    bottomNav.setVisibility(View.VISIBLE);
                                }
                            }

                            // Show success message after navigation starts
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            }, 100);
                        } else {
                            Toast.makeText(getContext(), "Đăng nhập thất bại. Dữ liệu không hợp lệ.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // API returned error
                        Log.d("SignIn", "API returned error: " + apiResponse.getError());
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Đăng nhập thất bại";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error - parse error message from response body
                    String errorMessage = ErrorMessageParser.parseErrorMessage(response, 
                        "Đăng nhập thất bại. Vui lòng kiểm tra thông tin đăng nhập.");
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
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
    
    private void navigateToOTPVerification(String email) {
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.otpVerificationFragment, bundle);
    }
    
    private void setGoogleSignInLoading(boolean isLoading) {
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setEnabled(!isLoading);
            if (isLoading) {
                btnGoogleSignIn.setText("Đang đăng nhập...");
            } else {
                btnGoogleSignIn.setText("Đăng nhập với Google");
            }
        }
    }
    
    // Google Sign-In callback methods
    @Override
    public void onSignInSuccess(FirebaseUser user, String idToken) {
        Log.d(TAG, "Google Sign-In successful: " + user.getEmail());
        
        // Update loading overlay to show processing
        loadingOverlay.updateText("Đang xử lý thông tin...", "Đang xác thực với server");
        
        // Send Firebase ID token to backend for verification and get JWT token immediately
        GoogleSignInRequest request = new GoogleSignInRequest(
            idToken,
            user.getEmail(),
            user.getDisplayName(),
            user.getUid()
        );
        
        Call<ApiResponse<AuthResponse>> call = authApiService.googleSignIn(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authResponse = apiResponse.getData();
                        
                        // Save user session with backend JWT token
                        tokenManager.saveUserSession(
                            authResponse.getToken(),
                            authResponse.getUser().getId(),
                            authResponse.getUser().getUsername(),
                            authResponse.getUser().getEmail(),
                            authResponse.getUser().getFullName()
                        );
                        
                        // Show success message briefly
                        loadingOverlay.updateText("Đăng nhập thành công!", "Đang chuyển hướng...");
                        
                        // Quick transition for smooth experience
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Hide loading overlay first for smooth transition
                            loadingOverlay.hide();
                            setGoogleSignInLoading(false);
                            
                            // Show bottom navigation when entering main app
                            if (getActivity() != null) {
                                View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                                if (bottomNav != null) {
                                    bottomNav.setVisibility(View.VISIBLE);
                                }
                            }
                            
                            // Navigate immediately after overlay hides with smooth animation
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                NavController navController = Navigation.findNavController(getView());
                                
                                // Create smooth navigation options
                                androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_smooth)
                                    .setExitAnim(R.anim.slide_out_smooth)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build();
                                
                                navController.navigate(R.id.action_signin_to_home, null, navOptions);
                                
                                // Show welcome toast after navigation
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    Toast.makeText(getContext(), "Chào mừng bạn đến với BookChain!", Toast.LENGTH_SHORT).show();
                                }, 150);
                            }, 100); // Very brief delay for smooth overlay hide
                        }, 400); // Quick success message display
                        
                    } else {
                        // Hide loading overlay and reset state
                        loadingOverlay.hide();
                        setGoogleSignInLoading(false);
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Đăng nhập Google thất bại";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Hide loading overlay and reset state
                    loadingOverlay.hide();
                    setGoogleSignInLoading(false);
                    Toast.makeText(getContext(), "Đăng nhập Google thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                // Hide loading overlay and reset state
                loadingOverlay.hide();
                setGoogleSignInLoading(false);
                Log.e(TAG, "Google Sign-In API call failed", t);
                Toast.makeText(getContext(), "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    public void onSignInFailure(String error) {
        // Hide loading overlay and reset state
        loadingOverlay.hide();
        setGoogleSignInLoading(false);
        Log.e(TAG, "Google Sign-In failed: " + error);
        Toast.makeText(getContext(), "Đăng nhập Google thất bại: " + error, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onSignInCancelled() {
        // Hide loading overlay and reset state
        loadingOverlay.hide();
        setGoogleSignInLoading(false);
        Log.d(TAG, "Google Sign-In cancelled by user");
        Toast.makeText(getContext(), "Đăng nhập Google đã bị hủy", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up loading overlay
        if (loadingOverlay != null) {
            loadingOverlay.destroy();
        }
    }
}
