package com.hofang.bookchainfe;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hofang.bookchainfe.ui.payment.PaymentDeepLinkHandler; // <-- Import lớp mới
import com.hofang.bookchainfe.network.ApiConfig;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private PaymentDeepLinkHandler paymentHandler; // <-- Khai báo biến
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiConfig.init(getApplicationContext());

        setContentView(R.layout.activity_main);

        // --- Khởi tạo Payment Handler ---
        paymentHandler = new PaymentDeepLinkHandler(this);

        getWindow().setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(true);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
                bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
                NavigationUI.setupWithNavController(bottomNav, navController);
                ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                    return insets;
                });
            }
        }

        // Giao việc xử lý Intent cho lớp helper
        if (navController != null) {
            paymentHandler.handleIntent(getIntent(), navController);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, "onNewIntent called with data: " + intent.getDataString());

        // Giao việc xử lý Intent mới cho lớp helper
        if (navController != null) {
            paymentHandler.handleIntent(intent, navController);
        }
    }
}