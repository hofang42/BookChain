package com.hofang.bookchainfe.ui.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.CartResponse.CartItem; // (model bạn sẽ tạo)
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.ui.home.BookItem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private Context context;
    private NumberFormat currencyFormatter;

    // --- BƯỚC 1: THÊM LISTENER INTERFACE ---
    private OnCartItemInteractionListener listener;

    public interface OnCartItemInteractionListener {
        void onRemoveItemClicked(CartItem cartItem);
    }
    // ----------------------------------------

    // --- BƯỚC 2: CẬP NHẬT CONSTRUCTOR ---
    public CartAdapter(Context context, OnCartItemInteractionListener listener) {
        this.context = context;
        this.listener = listener; // Gán listener
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }
    // ----------------------------------------

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, context, currencyFormatter);

        // --- BƯỚC 3: GÁN SỰ KIỆN CLICK CHO NÚT XÓA ---
        holder.btnRemoveItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItemClicked(item);
            }
        });
        // ---------------------------------------------
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setData(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    // --- ViewHolder ---
    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle, tvBookPrice, tvQuantity;
        ImageButton btnRemoveItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvBookPrice = itemView.findViewById(R.id.tv_book_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnRemoveItem = itemView.findViewById(R.id.btn_remove_item);
        }

        void bind(CartItem cartItem, Context context, NumberFormat formatter) {
            BookItem book = cartItem.getBook();
            if (book == null) return;

            tvBookTitle.setText(book.getTitle());
            tvBookPrice.setText(formatter.format(book.getPrice()));

            // Sửa lại logic hiển thị số lượng cho đúng với layout
            tvQuantity.setText(String.valueOf(cartItem.getQuantity()));

            // Tải ảnh
            String baseUrl = ApiConfig.BASE_URL;
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            String imageUrl = baseUrl + book.getCoverImage();
            Glide.with(context).load(imageUrl).placeholder(R.drawable.discount_badge_bg).into(ivBookCover);

            // Listener sẽ được gán ở onBindViewHolder
        }
    }
}