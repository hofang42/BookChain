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
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1001;

    private NavController navController;
    private PaymentDeepLinkHandler paymentHandler;

    /**
     * Khởi tạo Activity, thiết lập Navigation và xử lý deep link.
     */
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
                bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
                NavigationUI.setupWithNavController(bottomNav, navController);

                // Định nghĩa các màn hình "cấp 1" (nơi nav bar NÊN hiển thị)
                Set<Integer> topLevelDestinations = new HashSet<>();
                topLevelDestinations.add(R.id.nav_home);
                topLevelDestinations.add(R.id.nav_categories);
                topLevelDestinations.add(R.id.nav_cart);
                topLevelDestinations.add(R.id.nav_account);

                // Thêm một listener để tự động ẩn/hiện nav bar
                navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    if (topLevelDestinations.contains(destination.getId())) {
                        bottomNav.setVisibility(View.VISIBLE);
                    } else {
                        bottomNav.setVisibility(View.GONE);
                    }
                });

                // Xử lý deep link (nếu có) SAU KHI view đã sẵn sàng
                // (Sửa lỗi NullPointerException)
                bottomNav.post(() -> {
                    if (navController != null) {
                        paymentHandler.handleIntent(getIntent(), navController);
                    }
                });

                // Xử lý padding cho nav bar
                ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                            v.getPaddingRight(), systemBars.bottom);
                    return insets;
                });
            }
        }
    }

    /**
     * Khởi động service thông báo chat nếu người dùng đã đăng nhập.
     */
    private void startNotificationService() {
        TokenManager tokenManager = new TokenManager(this);
        if (tokenManager.isLoggedIn()) {
            Intent serviceIntent = new Intent(this, ChatNotificationService.class);
            // Sửa lỗi crash service bằng cách dùng ContextCompat
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    /**
     * Kiểm tra và yêu cầu quyền gửi thông báo (cho Android 13+).
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

    /**
     * Xử lý deep link (như link thanh toán) khi Activity đã chạy.
     */
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

    /**
     * Xử lý kết quả sau khi người dùng cho phép/từ chối quyền.
     */
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
            }
        }
    }

    /**
     * Dọn dẹp khi Activity bị hủy.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}