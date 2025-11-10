package com.hofang.bookchainfe.ui.cart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Book;
import com.hofang.bookchainfe.model.CartItem;
import com.hofang.bookchainfe.model.CartResponse;
import com.hofang.bookchainfe.model.Category;
import com.hofang.bookchainfe.model.UploadResponse;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.CartApiService;
import com.hofang.bookchainfe.ui.home.BookItem;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implement interface của Adapter
public class CartFragment extends Fragment implements CartAdapter.CartItemListener {

    private static final String TAG = "CartFragment";

    private RecyclerView rvCartItems;
    private CartAdapter adapter;
    private CartApiService cartApiService;

    // UI Views
    private ProgressBar progressBar;
    private TextView tvEmptyCart;
    private TextView tvTotalPrice;
    private MaterialButton btnGoToCheckout;
    private View checkoutBar;

    private NumberFormat currencyFormatter;
    private double currentTotalPrice = 0.0;
    private List<CartItem> currentCartItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        cartApiService = ApiConfig.getCartApiService();
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        setupRecyclerView();

        btnGoToCheckout.setOnClickListener(v -> {
            if (currentCartItems == null || currentCartItems.isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng rỗng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate total from selected items
            double total = 0;
            for (CartItem item : currentCartItems) {
                if (item.isSelected()) {
                    total += item.getSubtotal();
                }
            }

            Bundle args = new Bundle();
            args.putFloat("totalPrice", (float) total);
            args.putSerializable("cartItems", (Serializable) currentCartItems);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.checkoutFragment, args);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCart();
    }

    private void initViews(View view) {
        rvCartItems = view.findViewById(R.id.rv_cart_items);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyCart = view.findViewById(R.id.tv_empty_cart);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnGoToCheckout = view.findViewById(R.id.btn_go_to_checkout);
        checkoutBar = view.findViewById(R.id.checkout_bar);
        
        // Ẩn checkout bar ngay từ đầu để tránh nháy khi load
        checkoutBar.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(adapter);
    }

    private void fetchCart() {
        showLoading(true);
        
        cartApiService.getCart().enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    CartResponse cart = response.body();

                    if (cart.getItems() == null || cart.getItems().isEmpty()) {
                        showEmpty(true);
                        currentCartItems.clear();
                        currentTotalPrice = 0.0;
                    } else {
                        showEmpty(false);
                        // Convert CartResponse.CartItem to CartItem
                        List<CartItem> items = convertToCartItems(cart.getItems());
                        adapter.setCartItems(items);
                        currentCartItems = items;
                        currentTotalPrice = cart.getTotalPrice();
                    }
                    updateTotalPrice();
                } else {
                    Log.e(TAG, "fetchCart error: " + response.code());
                    showEmpty(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                if (getContext() == null) return;
                Log.e(TAG, "fetchCart failure: " + t.getMessage());
                showEmpty(true);
            }
        });
    }

    private List<CartItem> convertToCartItems(List<CartResponse.CartItem> responseItems) {
        List<CartItem> items = new ArrayList<>();
        for (CartResponse.CartItem responseItem : responseItems) {
            CartItem item = new CartItem();
            // Convert BookItem to Book
            Book book = convertBookItemToBook(responseItem.getBook());
            item.setBook(book);
            item.setQuantity(responseItem.getQuantity());
            item.setSelected(true);
            items.add(item);
        }
        return items;
    }

    private Book convertBookItemToBook(BookItem bookItem) {
        if (bookItem == null) return null;
        
        Book book = new Book();
        book.setId(bookItem.getId());
        book.setTitle(bookItem.getTitle());
        book.setAuthor(bookItem.getAuthor());
        book.setPrice(bookItem.getPrice());
        book.setDiscount((int) bookItem.getDiscount());
        book.setCoverImage(bookItem.getCoverImage());
        
        // Set category if available
        if (bookItem.getCategory() != null) {
            Category category = new Category();
            category.setName(bookItem.getCategory().getName());
            book.setCategory(category);
        }
        
        return book;
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : currentCartItems) {
            if (item.isSelected()) {
                total += item.getSubtotal();
            }
        }
        tvTotalPrice.setText(currencyFormatter.format(total));
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            tvEmptyCart.setVisibility(View.GONE);
            checkoutBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmpty(boolean isEmpty) {
        showLoading(false);
        if (isEmpty) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            checkoutBar.setVisibility(View.GONE);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            checkoutBar.setVisibility(View.VISIBLE);
        }
    }

    // Implement CartAdapter.CartItemListener methods
    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        if (item == null || item.getBook() == null) return;
        
        String bookId = item.getBook().getId();
        if (bookId == null || bookId.isEmpty()) return;

        // Update quantity on server
        // TODO: Implement update quantity API call
        updateTotalPrice();
    }

    @Override
    public void onItemDeleted(CartItem item, int position) {
        if (item == null || item.getBook() == null) {
            Log.e(TAG, "onItemDeleted: CartItem hoặc Book null");
            return;
        }

        String bookId = item.getBook().getId();
        if (bookId == null || bookId.isEmpty()) {
            Log.e(TAG, "onItemDeleted: Book ID null hoặc rỗng");
            return;
        }

        callRemoveApi(bookId);
    }

    @Override
    public void onSelectionChanged(CartItem item, boolean isSelected) {
        updateTotalPrice();
    }

    @Override
    public void onItemClicked(CartItem item) {
        // Navigate to book detail if needed
        if (item != null && item.getBook() != null && getView() != null) {
            Bundle args = new Bundle();
            Book book = item.getBook();
            
            args.putString("bookId", book.getId());
            args.putString("title", book.getTitle());
            args.putString("author", book.getAuthor());
            
            String categoryName = (book.getCategory() != null) ? book.getCategory().getName() : "N/A";
            args.putString("category", categoryName);
            
            args.putFloat("price", book.getPrice() != null ? book.getPrice().floatValue() : 0.0f);
            args.putInt("discount", book.getDiscount() != null ? book.getDiscount() : 0);
            args.putString("description", book.getDescription());
            args.putString("coverImage", book.getCoverImage());
            args.putString("coverUrl", book.getCoverImage());
            args.putInt("coverResId", 0);
            args.putFloat("rating", book.getRating() != null ? book.getRating().floatValue() : 4.0f);
            
            Navigation.findNavController(getView()).navigate(R.id.action_cart_to_book_detail, args);
        }
    }

    /**
     * Hàm gọi API xóa item khỏi giỏ hàng
     */
    private void callRemoveApi(String bookId) {
        if (cartApiService == null) return;

        cartApiService.removeFromCart(bookId).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<UploadResponse> call, @NonNull Response<UploadResponse> response) {
                if (getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage() != null ?
                            response.body().getMessage() : "Đã xóa sản phẩm";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                    // Tải lại toàn bộ giỏ hàng để cập nhật list và tổng tiền
                    fetchCart();
                } else {
                    Log.e(TAG, "removeFromCart error: " + response.code());
                    Toast.makeText(getContext(), "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                if (getContext() == null) return;
                Log.e(TAG, "removeFromCart failure: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi mạng, không thể xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }
}