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

public class SignInFragment extends Fragment {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private AppCompatButton btnSignIn;
    private TextView tvForgotPassword;
    private TextView tvRegister;
    private ImageButton btnBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
            Toast.makeText(getContext(), "Forgot password functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        tvRegister.setOnClickListener(v -> {
            // Navigate to Register screen
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_signin_to_register);
        });
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            etUsername.setError("Username/email is required");
            etUsername.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performSignIn(String username, String password) {
        // TODO: Implement actual sign in logic with API
        // For now, just simulate successful login
        
        // Show loading state
        btnSignIn.setEnabled(false);
        btnSignIn.setText("Signing In...");

        // Simulate API call delay
        btnSignIn.postDelayed(() -> {
            // Reset button state
            btnSignIn.setEnabled(true);
            btnSignIn.setText("Sign In");

            // Show success message
            Toast.makeText(getContext(), "Sign in successful!", Toast.LENGTH_SHORT).show();

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
        }, 1500);
    }
}
