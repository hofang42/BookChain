package com.hofang.bookchainfe.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hofang.bookchainfe.model.Book;
import com.hofang.bookchainfe.model.CartItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartManager {
    private static final String PREFS_NAME = "CartPrefs";
    private static final String KEY_CART_ITEMS_PREFIX = "cart_items_";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private TokenManager tokenManager;

    public CartManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.tokenManager = new TokenManager(context);
    }
    
    // Get cart key for current user
    private String getCartKey() {
        String userId = tokenManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            userId = "guest"; // For users not logged in
        }
        String key = KEY_CART_ITEMS_PREFIX + userId;
        android.util.Log.d("CartManager", "Using cart key: " + key + " (userId: " + userId + ")");
        return key;
    }

    // Add item to cart
    public void addToCart(Book book, int quantity) {
        List<CartItem> cartItems = getCartItems();
        
        // Check if book already exists in cart
        CartItem existingItem = findCartItem(cartItems, book.getId());
        
        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setId(UUID.randomUUID().toString());
            newItem.setBook(book);
            newItem.setQuantity(quantity);
            newItem.setSelected(true);
            cartItems.add(newItem);
        }
        
        saveCartItems(cartItems);
    }

    // Get all cart items
    public List<CartItem> getCartItems() {
        String json = prefs.getString(getCartKey(), null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<CartItem>>() {}.getType();
        List<CartItem> items = gson.fromJson(json, type);
        return items != null ? items : new ArrayList<>();
    }

    // Update cart item quantity
    public void updateQuantity(String cartItemId, int newQuantity) {
        List<CartItem> cartItems = getCartItems();
        CartItem item = findCartItem(cartItems, cartItemId);
        
        if (item != null) {
            if (newQuantity <= 0) {
                cartItems.remove(item);
            } else {
                item.setQuantity(newQuantity);
            }
            saveCartItems(cartItems);
        }
    }

    // Remove item from cart
    public void removeFromCart(String cartItemId) {
        List<CartItem> cartItems = getCartItems();
        CartItem item = findCartItem(cartItems, cartItemId);
        
        if (item != null) {
            cartItems.remove(item);
            saveCartItems(cartItems);
        }
    }

    // Update item selection
    public void updateSelection(String cartItemId, boolean isSelected) {
        List<CartItem> cartItems = getCartItems();
        CartItem item = findCartItem(cartItems, cartItemId);
        
        if (item != null) {
            item.setSelected(isSelected);
            saveCartItems(cartItems);
        }
    }

    // Clear all cart
    public void clearCart() {
        prefs.edit().remove(getCartKey()).apply();
    }

    // Get cart items count
    public int getCartItemsCount() {
        return getCartItems().size();
    }

    // Get selected items count
    public int getSelectedItemsCount() {
        List<CartItem> items = getCartItems();
        int count = 0;
        for (CartItem item : items) {
            if (item.isSelected()) {
                count++;
            }
        }
        return count;
    }

    // Get total price for selected items
    public double getTotalPrice() {
        List<CartItem> items = getCartItems();
        double total = 0.0;
        for (CartItem item : items) {
            if (item.isSelected()) {
                total += item.getSubtotal();
            }
        }
        return total;
    }

    // Private helper methods
    private void saveCartItems(List<CartItem> cartItems) {
        String json = gson.toJson(cartItems);
        prefs.edit().putString(getCartKey(), json).apply();
    }

    private CartItem findCartItem(List<CartItem> cartItems, String identifier) {
        for (CartItem item : cartItems) {
            if (item.getId().equals(identifier) || 
                (item.getBook() != null && item.getBook().getId().equals(identifier))) {
                return item;
            }
        }
        return null;
    }
}
