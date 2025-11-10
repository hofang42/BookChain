package com.hofang.bookchainfe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.services.ChatNotificationService;
import com.hofang.bookchainfe.utils.TokenManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ApiConfig
        ApiConfig.init(this);
        
        // Request notification permission (Android 13+)
        checkAndRequestNotificationPermission();
        
        // Start notification service if user is logged in
        startNotificationService();
        
        setContentView(R.layout.activity_main);

        // Make status bar white with dark icons
        getWindow().setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(true);

        // Setup Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // Setup Bottom Navigation with Navigation Component
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                // Hide bottom navigation initially (will be shown when user enters main app)
                bottomNav.setVisibility(View.GONE);
                
                // Force labels to show
                bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);

                // Connect Bottom Navigation with NavController - auto handles navigation
                NavigationUI.setupWithNavController(bottomNav, navController);

                // Handle window insets for bottom navigation
                ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // Apply bottom padding to avoid system nav bar overlap
                    v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        systemBars.bottom
                    );

                    return insets;
                });
            }
        }
    }

    /**
     * Start chat notification service if user is logged in
     */
    private void startNotificationService() {
        TokenManager tokenManager = new TokenManager(this);
        if (tokenManager.isLoggedIn()) {
            Intent serviceIntent = new Intent(this, ChatNotificationService.class);
            startService(serviceIntent);
        }
    }

    /**
     * Check and request notification permission for Android 13+ (API 33+)
     */
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, notification service can now show notifications
            } else {
                // Permission denied, user won't receive notifications
                // You can show a message explaining why notification permission is needed
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Service will continue running in background
    }
}