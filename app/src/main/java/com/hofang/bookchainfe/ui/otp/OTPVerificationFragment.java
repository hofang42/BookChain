package com.hofang.bookchainfe.ui.otp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.AuthResponse;
import com.hofang.bookchainfe.model.SendOTPRequest;
import com.hofang.bookchainfe.model.VerifyOTPRequest;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.AuthApiService;
import com.hofang.bookchainfe.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPVerificationFragment extends Fragment {

    private TextView tvEmail;
    private TextView tvTimer;
    private TextView tvResend;
    private EditText[] otpInputs = new EditText[6];
    private AppCompatButton btnVerify;
    private ImageButton btnBack;
    
    private AuthApiService authApiService;
    private TokenManager tokenManager;
    
    private String email;
    private CountDownTimer countDownTimer;
    private static final int TIMER_DURATION = 60000; // 60 seconds
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service and token manager
        authApiService = ApiConfig.getAuthApiService();
        tokenManager = new TokenManager(requireContext());

        // Get email from arguments
        if (getArguments() != null) {
            email = getArguments().getString("email", "");
        }

        // Initialize views
        initViews(view);
        
        // Setup OTP inputs
        setupOTPInputs();
        
        // Set email display
        tvEmail.setText(email);
        
        // Start timer
        startTimer();
        
        // Set click listeners
        setupClickListeners();
    }

    private void initViews(View view) {
        tvEmail = view.findViewById(R.id.tv_email);
        tvTimer = view.findViewById(R.id.tv_timer);
        tvResend = view.findViewById(R.id.tv_resend);
        btnVerify = view.findViewById(R.id.btn_verify);
        btnBack = view.findViewById(R.id.btn_back);
        
        // Initialize OTP input fields
        otpInputs[0] = view.findViewById(R.id.et_otp_1);
        otpInputs[1] = view.findViewById(R.id.et_otp_2);
        otpInputs[2] = view.findViewById(R.id.et_otp_3);
        otpInputs[3] = view.findViewById(R.id.et_otp_4);
        otpInputs[4] = view.findViewById(R.id.et_otp_5);
        otpInputs[5] = view.findViewById(R.id.et_otp_6);
    }

    private void setupOTPInputs() {
        for (int i = 0; i < otpInputs.length; i++) {
            final int index = i;
            
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpInputs.length - 1) {
                        // Move to next input
                        otpInputs[index + 1].requestFocus();
                    }
                    
                    // Check if all fields are filled
                    checkOTPComplete();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            
            // Handle backspace
            otpInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpInputs[index].getText().toString().isEmpty() && index > 0) {
                        // Move to previous input
                        otpInputs[index - 1].requestFocus();
                        otpInputs[index - 1].setText("");
                    }
                }
                return false;
            });
        }
    }

    private void checkOTPComplete() {
        boolean allFilled = true;
        for (EditText input : otpInputs) {
            if (input.getText().toString().trim().isEmpty()) {
                allFilled = false;
                break;
            }
        }
        
        btnVerify.setEnabled(allFilled);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        btnVerify.setOnClickListener(v -> {
            String otp = getOTPFromInputs();
            if (otp.length() == 6) {
                verifyOTP(otp);
            }
        });

        tvResend.setOnClickListener(v -> {
            resendOTP();
        });
    }

    private String getOTPFromInputs() {
        StringBuilder otp = new StringBuilder();
        for (EditText input : otpInputs) {
            otp.append(input.getText().toString().trim());
        }
        return otp.toString();
    }

    private void startTimer() {
        tvResend.setVisibility(View.GONE);
        tvTimer.setVisibility(View.VISIBLE);
        
        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText("Gửi lại mã sau " + seconds + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                tvResend.setVisibility(View.VISIBLE);
            }
        };
        
        countDownTimer.start();
    }

    private void resendOTP() {
        // Show loading state
        tvResend.setEnabled(false);
        tvResend.setText("Đang gửi...");
        
        SendOTPRequest request = new SendOTPRequest(email);
        
        Call<ApiResponse<String>> call = authApiService.sendVerificationOTP(request);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                tvResend.setEnabled(true);
                tvResend.setText("Gửi lại mã");
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Mã OTP đã được gửi thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Clear current OTP inputs
                        for (EditText input : otpInputs) {
                            input.setText("");
                        }
                        otpInputs[0].requestFocus();
                        
                        // Restart timer
                        startTimer();
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Gửi mã OTP thất bại";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Gửi mã OTP thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                tvResend.setEnabled(true);
                tvResend.setText("Gửi lại mã");
                Toast.makeText(getContext(), "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verifyOTP(String otp) {
        // Show loading state
        btnVerify.setEnabled(false);
        btnVerify.setText("Đang xác thực...");
        
        VerifyOTPRequest request = new VerifyOTPRequest(email, otp);
        
        Call<ApiResponse<AuthResponse>> call = authApiService.verifyEmail(request);
        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Xác thực Email");
                
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

                        Toast.makeText(getContext(), "Xác thực email thành công! Chào mừng bạn!", Toast.LENGTH_SHORT).show();
                        
                        // Show bottom navigation when entering main app
                        if (getActivity() != null) {
                            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                            if (bottomNav != null) {
                                bottomNav.setVisibility(View.VISIBLE);
                            }
                        }
                        
                        // Navigate to home after successful verification
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_otp_to_home);
                    } else {
                        // API returned error
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Mã OTP không hợp lệ";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        
                        // Clear OTP inputs on error
                        for (EditText input : otpInputs) {
                            input.setText("");
                        }
                        otpInputs[0].requestFocus();
                    }
                } else {
                    // HTTP error
                    Toast.makeText(getContext(), "Xác thực thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    
                    // Clear OTP inputs on error
                    for (EditText input : otpInputs) {
                        input.setText("");
                    }
                    otpInputs[0].requestFocus();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Xác thực Email");
                
                Toast.makeText(getContext(), "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.", Toast.LENGTH_LONG).show();
                
                // Clear OTP inputs on error
                for (EditText input : otpInputs) {
                    input.setText("");
                }
                otpInputs[0].requestFocus();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
