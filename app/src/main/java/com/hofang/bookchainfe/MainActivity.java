package com.hofang.bookchainfe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.hofang.bookchainfe.ui.payment.PaymentDeepLinkHandler;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1001;

    private NavController navController;
    private PaymentDeepLinkHandler paymentHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Initialize ApiConfig ---
        ApiConfig.init(getApplicationContext());

        // --- Initialize Payment Handler ---
        paymentHandler = new PaymentDeepLinkHandler(this);

        // --- Request notification permission (Android 13+) ---
        checkAndRequestNotificationPermission();

        // --- Start notification service if user is logged in ---
        startNotificationService();

        // --- UI setup ---
        getWindow().setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(true);

        // --- Setup NavHostFragment and BottomNavigationView ---
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
                bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
                NavigationUI.setupWithNavController(bottomNav, navController);

                ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                            v.getPaddingRight(), systemBars.bottom);
                    return insets;
                });
            }
        }

        // --- Handle payment deep link if present ---
        if (navController != null) {
            paymentHandler.handleIntent(getIntent(), navController);
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
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION_PERMISSION);
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, "onNewIntent called with data: " + intent.getDataString());

        // Handle new deep link intents
        if (navController != null) {
            paymentHandler.handleIntent(intent, navController);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.w(TAG, "Notification permission denied");
                // Optionally: show explanation dialog here
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Service continues running in background, no need to stop explicitly
    }
}
