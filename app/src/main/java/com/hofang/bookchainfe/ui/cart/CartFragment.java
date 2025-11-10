//package com.hofang.bookchainfe.ui.cart;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.navigation.fragment.NavHostFragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.button.MaterialButton;
//import com.hofang.bookchainfe.R;
//import com.hofang.bookchainfe.model.CartResponse;
//import com.hofang.bookchainfe.model.UploadResponse; // Import model
//import com.hofang.bookchainfe.network.ApiConfig;
//import com.hofang.bookchainfe.network.CartApiService;
//
//import java.io.Serializable;
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//// Implement interface của Adapter
//public class CartFragment extends Fragment implements CartAdapter.OnCartItemInteractionListener {
//
//    private static final String TAG = "CartFragment";
//
//    private RecyclerView rvCartItems;
//    private CartAdapter adapter;
//    private CartApiService cartApiService;
//
//    // UI Views
//    private ProgressBar progressBar;
//    private TextView tvEmptyCart;
//    private TextView tvTotalPrice;
//    private MaterialButton btnGoToCheckout;
//    private View checkoutBar;
//
//    private NumberFormat currencyFormatter;
//    private double currentTotalPrice = 0.0;
//    private List<CartResponse.CartItem> currentCartItems = new ArrayList<>();
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_cart, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        initViews(view);
//        cartApiService = ApiConfig.getCartApiService();
//        // Thống nhất dùng formatter tiếng Việt
//        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//
//        setupRecyclerView();
//
//        btnGoToCheckout.setOnClickListener(v -> {
//            if (currentCartItems == null || currentCartItems.isEmpty()) {
//                Toast.makeText(getContext(), "Giỏ hàng rỗng", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Bundle args = new Bundle();
//            args.putFloat("totalPrice", (float) currentTotalPrice);
//            args.putSerializable("cartItems", (Serializable) currentCartItems);
//
//            NavHostFragment.findNavController(this)
//                    .navigate(R.id.action_cart_to_checkout, args);
//        });
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        fetchCart();
//    }
//
//    private void initViews(View view) {
//        rvCartItems = view.findViewById(R.id.rv_cart_items);
//        progressBar = view.findViewById(R.id.progress_bar);
//        tvEmptyCart = view.findViewById(R.id.tv_empty_cart);
//        tvTotalPrice = view.findViewById(R.id.tv_total_price);
//        btnGoToCheckout = view.findViewById(R.id.btn_go_to_checkout);
//        checkoutBar = view.findViewById(R.id.checkout_bar);
//    }
//
//    private void setupRecyclerView() {
//        // Truyền "this" (chính là Fragment này) vào Adapter
//        adapter = new CartAdapter(getContext(), this);
//        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
//        rvCartItems.setAdapter(adapter);
//    }
//
//    private void fetchCart() {
//        showLoading(true);
//        cartApiService.getCart().enqueue(new Callback<CartResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
//                if (getContext() == null) return;
//
//                if (response.isSuccessful() && response.body() != null) {
//                    CartResponse cart = response.body();
//
//                    if (cart.getItems() == null || cart.getItems().isEmpty()) {
//                        showEmpty(true);
//                        currentCartItems.clear();
//                        currentTotalPrice = 0.0; // Reset giá khi rỗng
//                    } else {
//                        showEmpty(false);
//                        adapter.setData(cart.getItems());
//                        currentCartItems = cart.getItems();
//                        currentTotalPrice = cart.getTotalPrice();
//                    }
//                    // Luôn cập nhật giá (kể cả khi rỗng)
//                    tvTotalPrice.setText(currencyFormatter.format(currentTotalPrice));
//                } else {
//                    Log.e(TAG, "fetchCart error: " + response.code());
//                    showEmpty(true);
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
//                if (getContext() == null) return;
//                Log.e(TAG, "fetchCart failure: " + t.getMessage());
//                showEmpty(true);
//            }
//        });
//    }
//
//    private void showLoading(boolean isLoading) {
//        if (isLoading) {
//            progressBar.setVisibility(View.VISIBLE);
//            rvCartItems.setVisibility(View.GONE);
//            tvEmptyCart.setVisibility(View.GONE);
//            checkoutBar.setVisibility(View.GONE);
//        } else {
//            progressBar.setVisibility(View.GONE);
//        }
//    }
//
//    private void showEmpty(boolean isEmpty) {
//        showLoading(false);
//        if (isEmpty) {
//            tvEmptyCart.setVisibility(View.VISIBLE);
//            rvCartItems.setVisibility(View.GONE);
//            checkoutBar.setVisibility(View.GONE);
//        } else {
//            tvEmptyCart.setVisibility(View.GONE);
//            rvCartItems.setVisibility(View.VISIBLE);
//            checkoutBar.setVisibility(View.VISIBLE);
//        }
//    }
//
//    /**
//     * Hàm này được gọi từ CartAdapter khi nhấn nút Xóa
//     */
//    @Override
//    public void onRemoveItemClicked(CartResponse.CartItem cartItem) {
//        if (cartItem == null || cartItem.getBook() == null) {
//            Log.e(TAG, "onRemoveItemClicked: CartItem hoặc Book null");
//            return;
//        }
//
//        // Đảm bảo bạn có hàm get_id() hoặc getId() trả về String trong model BookItem
//        String bookId = cartItem.getBook().getId();
//
//        if (bookId == null || bookId.isEmpty()) {
//            Log.e(TAG, "onRemoveItemClicked: Book ID null hoặc rỗng");
//            return;
//        }
//
//        callRemoveApi(bookId);
//    }
//
//    /**
//     * Hàm gọi API xóa item khỏi giỏ hàng
//     */
//    private void callRemoveApi(String bookId) {
//        if (cartApiService == null) return;
//
//        // Tùy chọn: Hiển thị một loading nhỏ hoặc thông báo "Đang xóa..."
//
//        cartApiService.removeFromCart(bookId).enqueue(new Callback<UploadResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<UploadResponse> call, @NonNull Response<UploadResponse> response) {
//                if (getContext() == null) return;
//
//                if (response.isSuccessful() && response.body() != null) {
//                    // Dùng message từ API nếu có, nếu không thì dùng mặc định
//                    String message = response.body().getMessage() != null ?
//                            response.body().getMessage() : "Đã xóa sản phẩm";
//                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
//
//                    // QUAN TRỌNG: Tải lại toàn bộ giỏ hàng để cập nhật list và tổng tiền
//                    fetchCart();
//                } else {
//                    Log.e(TAG, "removeFromCart error: " + response.code());
//                    Toast.makeText(getContext(), "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
//                if (getContext() == null) return;
//                Log.e(TAG, "removeFromCart failure: " + t.getMessage());
//                Toast.makeText(getContext(), "Lỗi mạng, không thể xóa", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}