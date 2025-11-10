package com.hofang.bookchainfe.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Book;
import com.hofang.bookchainfe.model.CartItem;
import com.hofang.bookchainfe.model.Category;
import com.hofang.bookchainfe.ui.bookdetail.BookDetailActivity;
import com.hofang.bookchainfe.utils.CartManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {
    private RecyclerView rvCartItems;
    private TextView tvEmptyCart;
    private TextView tvSubtotal;
    private TextView tvShipping;
    private TextView tvTotal;
    private Button btnCheckout;
    private BottomNavigationView bottomNavigation;
    private View llOrderSummary;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();

    private static final double SHIPPING_COST = 10.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupBottomNavigation();
        loadCartItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload cart items when returning to this activity
        loadCartItems();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rv_cart_items);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShipping = findViewById(R.id.tv_shipping);
        tvTotal = findViewById(R.id.tv_total);
        btnCheckout = findViewById(R.id.btn_checkout);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        llOrderSummary = findViewById(R.id.ll_order_summary);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_cart);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                // Navigate back to MainActivity (Home)
                finish();
                return true;
            } else if (itemId == R.id.nav_categories) {
                // Navigate back to MainActivity and switch to Categories
                finish();
                return true;
            } else if (itemId == R.id.nav_cart) {
                // Already on Cart
                return true;
            } else if (itemId == R.id.nav_account) {
                // Navigate back to MainActivity and switch to Account
                finish();
                return true;
            }
            
            return false;
        });

        btnCheckout.setOnClickListener(v -> handleCheckout());
    }

    private void loadCartItems() {
        // Load from CartManager
        CartManager cartManager = new CartManager(this);
        cartItems = cartManager.getCartItems();
        updateUI();
    }

    private List<CartItem> createMockCartItems() {
        List<CartItem> items = new ArrayList<>();

        // Mock Book 1
        Category fiction = new Category("cat1", "Fiction", "Fiction books");
        Book book1 = new Book();
        book1.setId("book1");
        book1.setTitle("Sorrow and Starlight");
        book1.setAuthor("Lisa Maxwell");
        book1.setCoverImage("https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1693857729i/123149209.jpg");
        book1.setPrice(30.00);
        book1.setCategory(fiction);

        CartItem item1 = new CartItem();
        item1.setId("cart1");
        item1.setBook(book1);
        item1.setQuantity(1);
        item1.setSelected(false);
        items.add(item1);

        // Mock Book 2
        Book book2 = new Book();
        book2.setId("book2");
        book2.setTitle("The Catcher in the Rye");
        book2.setAuthor("J.D. Salinger");
        book2.setCoverImage("https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1398034300i/5107.jpg");
        book2.setPrice(30.00);
        book2.setCategory(fiction);

        CartItem item2 = new CartItem();
        item2.setId("cart2");
        item2.setBook(book2);
        item2.setQuantity(1);
        item2.setSelected(true);
        items.add(item2);

        return items;
    }

    private void updateUI() {
        if (cartItems.isEmpty()) {
            showEmptyCart();
        } else {
            showCartItems();
            cartAdapter.setCartItems(cartItems);
            updateOrderSummary();
        }
    }

    private void showEmptyCart() {
        rvCartItems.setVisibility(View.GONE);
        llOrderSummary.setVisibility(View.GONE);
        tvEmptyCart.setVisibility(View.VISIBLE);
    }

    private void showCartItems() {
        rvCartItems.setVisibility(View.VISIBLE);
        llOrderSummary.setVisibility(View.VISIBLE);
        tvEmptyCart.setVisibility(View.GONE);
    }

    private void updateOrderSummary() {
        double subtotal = 0.0;
        
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                subtotal += item.getSubtotal();
            }
        }

        double shipping = subtotal > 0 ? SHIPPING_COST : 0.0;
        double total = subtotal + shipping;

        tvSubtotal.setText(String.format(Locale.US, "$%.2f", subtotal));
        tvShipping.setText(String.format(Locale.US, "$%.2f", shipping));
        tvTotal.setText(String.format(Locale.US, "$%.2f", total));

        // Enable/disable checkout button based on selection
        btnCheckout.setEnabled(subtotal > 0);
        btnCheckout.setAlpha(subtotal > 0 ? 1.0f : 0.5f);
    }

    private void handleCheckout() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please select items to checkout", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total for selected items
        double subtotal = 0.0;
        for (CartItem item : selectedItems) {
            subtotal += item.getSubtotal();
        }
        double total = subtotal + SHIPPING_COST;

        // Navigate to checkout screen
        Intent intent = new Intent(this, com.hofang.bookchainfe.ui.checkout.CheckoutActivity.class);
        intent.putExtra("TOTAL_AMOUNT", total);
        // TODO: Pass selected items to checkout
        // intent.putExtra("SELECTED_ITEMS", (Serializable) selectedItems);
        startActivity(intent);
    }

    // CartAdapter.CartItemListener implementations
    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        // Update in CartManager
        CartManager cartManager = new CartManager(this);
        cartManager.updateQuantity(item.getId(), newQuantity);
        updateOrderSummary();
    }

    @Override
    public void onItemDeleted(CartItem item, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Item")
                .setMessage("Remove this item from your cart?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    // Remove from CartManager
                    CartManager cartManager = new CartManager(this);
                    cartManager.removeFromCart(item.getId());
                    
                    // Update UI
                    cartItems.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                    updateUI();
                    Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSelectionChanged(CartItem item, boolean isSelected) {
        // Update in CartManager
        CartManager cartManager = new CartManager(this);
        cartManager.updateSelection(item.getId(), isSelected);
        updateOrderSummary();
    }

    @Override
    public void onItemClicked(CartItem item) {
        // Navigate to book detail
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("BOOK_ID", item.getBook().getId());
        startActivity(intent);
    }
}
