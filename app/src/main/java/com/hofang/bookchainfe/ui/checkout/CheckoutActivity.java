package com.hofang.bookchainfe.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Address;
import com.hofang.bookchainfe.model.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView tvAddress;
    private TextView btnChangeAddress;
    private Button btnAddAddress;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCreditCard;
    private RadioButton rbCashOnDelivery;
    private Button btnPay;

    private Address currentAddress;
    private List<CartItem> selectedCartItems = new ArrayList<>();
    private double totalAmount = 0.0;
    private String selectedPaymentMethod = "Cash on Delivery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupToolbar();
        loadDataFromIntent();
        setupListeners();
        loadDefaultAddress();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvAddress = findViewById(R.id.tv_address);
        btnChangeAddress = findViewById(R.id.btn_change_address);
        btnAddAddress = findViewById(R.id.btn_add_address);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        rbCreditCard = findViewById(R.id.rb_credit_card);
        rbCashOnDelivery = findViewById(R.id.rb_cash_on_delivery);
        btnPay = findViewById(R.id.btn_pay);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void loadDataFromIntent() {
        // Get total amount from intent
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        updatePayButton();

        // TODO: Get selected cart items from intent
        // selectedCartItems = (List<CartItem>) getIntent().getSerializableExtra("SELECTED_ITEMS");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChangeAddress.setOnClickListener(v -> {
            // TODO: Open address selection dialog or activity
            Toast.makeText(this, "Change address - Coming soon", Toast.LENGTH_SHORT).show();
        });

        btnAddAddress.setOnClickListener(v -> {
            // TODO: Open add address screen
            Toast.makeText(this, "Add new address - Coming soon", Toast.LENGTH_SHORT).show();
        });

        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_credit_card) {
                selectedPaymentMethod = "Credit Card";
            } else if (checkedId == R.id.rb_cash_on_delivery) {
                selectedPaymentMethod = "Cash on Delivery";
            }
        });

        btnPay.setOnClickListener(v -> handlePayment());
    }

    private void loadDefaultAddress() {
        // TODO: Load from backend API or local database
        // For now, using mock data
        currentAddress = createMockAddress();
        displayAddress();
    }

    private Address createMockAddress() {
        Address address = new Address();
        address.setId("addr1");
        address.setCity("Da Nang");
        address.setDistrict("Da Nang");
        address.setPostalCode("50000");
        address.setDefault(true);
        return address;
    }

    private void displayAddress() {
        if (currentAddress != null) {
            tvAddress.setText(currentAddress.getFormattedAddress());
        } else {
            tvAddress.setText("No address selected");
        }
    }

    private void updatePayButton() {
        btnPay.setText(String.format(Locale.US, "Pay $%.2f", totalAmount));
    }

    private void handlePayment() {
        // Validate address
        if (currentAddress == null) {
            Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate payment method
        if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        // Process payment
        if (selectedPaymentMethod.equals("Credit Card")) {
            // Open credit card form dialog
            showCreditCardDialog();
        } else {
            // Cash on Delivery - Create order directly
            createOrder();
        }
    }

    private void showCreditCardDialog() {
        CreditCardBottomSheet bottomSheet = CreditCardBottomSheet.newInstance(card -> {
            // Credit card info submitted
            Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();
            // TODO: Process credit card payment with backend
            createOrder();
        });
        bottomSheet.show(getSupportFragmentManager(), "CreditCardBottomSheet");
    }

    private void createOrder() {
        // TODO: Call backend API to create order
        // For now, just show success message
        Toast.makeText(this, "Order created successfully!", Toast.LENGTH_SHORT).show();
        
        // Navigate back to home or order confirmation
        // TODO: Navigate to order confirmation screen
        finish();
    }

    // Method to update address when changed
    public void updateAddress(Address newAddress) {
        this.currentAddress = newAddress;
        displayAddress();
    }
}
