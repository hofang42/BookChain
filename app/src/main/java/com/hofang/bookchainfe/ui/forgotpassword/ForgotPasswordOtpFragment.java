package com.hofang.bookchainfe.ui.forgotpassword;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.util.Log;
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
import com.hofang.bookchainfe.model.SendOTPRequest;
import com.hofang.bookchainfe.model.PasswordResetOTPResponse;
import com.hofang.bookchainfe.model.VerifyOTPRequest;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.AuthApiService;
import com.hofang.bookchainfe.utils.ErrorMessageParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordOtpFragment extends Fragment {
    private static final String TAG = "ForgotPasswordOtpFragment";
    private static final long COUNTDOWN_TIME = 300000; // 5 minutes in milliseconds

    private EditText[] otpEditTexts = new EditText[6];
    private AppCompatButton btnVerifyOtp;
    private TextView tvResendOtp;
    private TextView tvCountdown;
    private TextView tvEmail;
    private ImageButton btnBack;
    
    private AuthApiService authApiService;
    private String email;
    private CountDownTimer countDownTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service
        authApiService = ApiConfig.getAuthApiService();

        // Get email from arguments
        if (getArguments() != null) {
            email = getArguments().getString("email");
        }

        // Initialize views
        initViews(view);
        setupOtpInputs();
        startCountdown();

        // Display email
        if (email != null) {
            tvEmail.setText("Mã OTP đã được gửi đến " + email);
        }

        // Set click listeners
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp();
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = getOtpFromInputs();
            if (validateOtp(otp)) {
                verifyOtp(otp);
            }
        });

        tvResendOtp.setOnClickListener(v -> resendOtp());
    }

    private void initViews(View view) {
        otpEditTexts[0] = view.findViewById(R.id.et_otp_1);
        otpEditTexts[1] = view.findViewById(R.id.et_otp_2);
        otpEditTexts[2] = view.findViewById(R.id.et_otp_3);
        otpEditTexts[3] = view.findViewById(R.id.et_otp_4);
        otpEditTexts[4] = view.findViewById(R.id.et_otp_5);
        otpEditTexts[5] = view.findViewById(R.id.et_otp_6);
        
        btnVerifyOtp = view.findViewById(R.id.btn_verify_otp);
        tvResendOtp = view.findViewById(R.id.tv_resend_otp);
        tvCountdown = view.findViewById(R.id.tv_countdown);
        tvEmail = view.findViewById(R.id.tv_email);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpEditTexts.length; i++) {
            final int index = i;
            
            otpEditTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpEditTexts.length - 1) {
                        otpEditTexts[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpEditTexts[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpEditTexts[index].getText().toString().isEmpty() && index > 0) {
                        otpEditTexts[index - 1].requestFocus();
                        otpEditTexts[index - 1].setText("");
                    }
                }
                return false;
            });
        }
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvCountdown.setText(String.format("Gửi lại sau %02d:%02d", minutes, seconds));
                tvResendOtp.setEnabled(false);
                tvResendOtp.setTextColor(getResources().getColor(R.color.gray_400));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Mã OTP đã hết hạn");
                tvResendOtp.setEnabled(true);
                tvResendOtp.setTextColor(getResources().getColor(R.color.primary));
            }
        }.start();
    }

    private String getOtpFromInputs() {
        StringBuilder otp = new StringBuilder();
        for (EditText editText : otpEditTexts) {
            otp.append(editText.getText().toString());
        }
        return otp.toString();
    }

    private boolean validateOtp(String otp) {
        if (otp.length() != 6) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void verifyOtp(String otp) {
        // Show loading state
        btnVerifyOtp.setEnabled(false);
        btnVerifyOtp.setText("Đang xác thực...");

        // Create request
        VerifyOTPRequest request = new VerifyOTPRequest(email, otp);

        // Make API call
        Call<ApiResponse<PasswordResetOTPResponse>> call = authApiService.verifyPasswordResetOTP(request);
        call.enqueue(new Callback<ApiResponse<PasswordResetOTPResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PasswordResetOTPResponse>> call, Response<ApiResponse<PasswordResetOTPResponse>> response) {
                // Reset button state
                btnVerifyOtp.setEnabled(true);
                btnVerifyOtp.setText("Xác thực OTP");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PasswordResetOTPResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Toast.makeText(getContext(), "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to reset password with email and otp
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
                        bundle.putString("otp", otp);
                        
                        NavController navController = Navigation.findNavController(getView());
                        navController.navigate(R.id.action_forgotPasswordOtp_to_resetPassword, bundle);
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Mã OTP không chính xác";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        clearOtpInputs();
                    }
                } else {
                    // HTTP error - parse error message from response body
                    String errorMessage = ErrorMessageParser.parseErrorMessage(response, 
                        "Mã OTP không chính xác. Vui lòng thử lại.");
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    clearOtpInputs();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PasswordResetOTPResponse>> call, Throwable t) {
                // Reset button state
                btnVerifyOtp.setEnabled(true);
                btnVerifyOtp.setText("Xác thực OTP");
                
                Toast.makeText(getContext(), "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendOtp() {
        if (email == null) return;

        // Show loading state
        tvResendOtp.setEnabled(false);
        tvResendOtp.setText("Đang gửi...");

        // Create request
        SendOTPRequest request = new SendOTPRequest(email);

        // Make API call
        Call<ApiResponse<String>> call = authApiService.sendPasswordResetOTP(request);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Mã OTP mới đã được gửi", Toast.LENGTH_SHORT).show();
                        clearOtpInputs();
                        startCountdown();
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Gửi lại OTP thất bại";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        tvResendOtp.setEnabled(true);
                        tvResendOtp.setText("Gửi lại mã");
                    }
                } else {
                    // HTTP error - parse error message from response body
                    String errorMessage = ErrorMessageParser.parseErrorMessage(response, 
                        "Gửi lại OTP thất bại. Vui lòng thử lại.");
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    tvResendOtp.setEnabled(true);
                    tvResendOtp.setText("Gửi lại mã");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.", Toast.LENGTH_LONG).show();
                tvResendOtp.setEnabled(true);
                tvResendOtp.setText("Gửi lại mã");
            }
        });
    }

    private void clearOtpInputs() {
        for (EditText editText : otpEditTexts) {
            editText.setText("");
        }
        otpEditTexts[0].requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
