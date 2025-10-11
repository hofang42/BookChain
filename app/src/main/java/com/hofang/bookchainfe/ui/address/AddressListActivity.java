package com.hofang.bookchainfe.ui.address;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.hofang.bookchainfe.R;

public class AddressListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        // Make status bar white with dark icons
        getWindow().setStatusBarColor(Color.WHITE);
        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(true);

        // Handle window insets properly for edge-to-edge display
        View coordinatorRoot = findViewById(R.id.coordinator_root);
        ViewCompat.setOnApplyWindowInsetsListener(coordinatorRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // CoordinatorLayout with fitsSystemWindows=true will handle insets automatically
            // Just consume the insets to prevent further propagation issues
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
