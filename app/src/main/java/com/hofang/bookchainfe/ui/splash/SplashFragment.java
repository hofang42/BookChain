package com.hofang.bookchainfe.ui.splash;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.hofang.bookchainfe.R;

public class SplashFragment extends Fragment {

    private static final int SPLASH_DELAY = 5000; // 5 seconds

    public SplashFragment() {
        // Required empty public constructor
    }

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Hide bottom navigation when showing splash
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
        
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Start advanced loading animation
        ImageView loadingIndicator = view.findViewById(R.id.loading_indicator);
        if (loadingIndicator != null) {
            Drawable drawable = loadingIndicator.getDrawable();
            if (drawable instanceof AnimatedVectorDrawable) {
                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
                animatedVectorDrawable.start();
            } else if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.start();
            }
        }

        // Auto navigate to welcome after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() != null && isAdded()) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_splash_to_welcome);
            }
        }, SPLASH_DELAY);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Don't show bottom navigation when going to welcome screen
        // Bottom nav will be shown when user navigates to main app screens
    }
}
