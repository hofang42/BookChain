package com.hofang.bookchainfe.ui.welcome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.hofang.bookchainfe.R;

public class WelcomeFragment extends Fragment {

    private Button btnGetStarted;
    private Button btnRegister;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        btnGetStarted = view.findViewById(R.id.btn_get_started);
        btnRegister = view.findViewById(R.id.btn_register);

        // Set click listeners
        btnGetStarted.setOnClickListener(v -> {
            // Navigate to Sign In screen (bottom nav stays hidden)
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_welcome_to_signin);
        });

        btnRegister.setOnClickListener(v -> {
            // Navigate to Register screen (bottom nav stays hidden)
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_welcome_to_register);
        });
    }

}
