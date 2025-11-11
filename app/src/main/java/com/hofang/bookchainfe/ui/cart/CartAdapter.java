package com.hofang.bookchainfe.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.CartItem;

import java.text.NumberFormat; // Import
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems = new ArrayList<>();
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemDeleted(CartItem item, int position);
        void onSelectionChanged(CartItem item, boolean isSelected);
        void onItemClicked(CartItem item);
    }

    public CartAdapter(CartItemListener listener) {
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbSelect;
        private ImageView ivBookCover;
        private TextView tvBookTitle;
        private TextView tvQuantity;
        private TextView tvPrice;
        private TextView btnDecrease;
        private TextView btnIncrease;
        private ImageView btnDelete;

        // Biến định dạng tiền tệ
        private NumberFormat currencyFormatter;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            // Khởi tạo định dạng tiền VN
            Locale localeVN = new Locale("vi", "VN");
            currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        }

        public void bind(CartItem item) {
            // Set checkbox state
            cbSelect.setChecked(item.isSelected());
            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                if (listener != null) {
                    listener.onSelectionChanged(item, isChecked);
                }
            });

            // Load book cover
            if (item.getBook().getCoverImage() != null && !item.getBook().getCoverImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getBook().getCoverImage())
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(ivBookCover);
            } else {
                ivBookCover.setImageResource(R.drawable.ic_book_placeholder);
            }

            // Set book title
            tvBookTitle.setText(item.getBook().getTitle());

            // Set quantity
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Set price (subtotal for this item)
            tvPrice.setText(currencyFormatter.format(item.getSubtotal()));

            // Decrease quantity
            btnDecrease.setOnClickListener(v -> {
                int currentQuantity = item.getQuantity();
                if (currentQuantity > 1) {
                    int newQuantity = currentQuantity - 1;
                    item.setQuantity(newQuantity);
                    tvQuantity.setText(String.valueOf(newQuantity));
                    // Cập nhật giá theo format VN
                    tvPrice.setText(currencyFormatter.format(item.getSubtotal()));
                    if (listener != null) {
                        listener.onQuantityChanged(item, newQuantity);
                    }
                }
            });

            // Increase quantity
            btnIncrease.setOnClickListener(v -> {
                int currentQuantity = item.getQuantity();
                int newQuantity = currentQuantity + 1;
                item.setQuantity(newQuantity);
                tvQuantity.setText(String.valueOf(newQuantity));
                // Cập nhật giá theo format VN
                tvPrice.setText(currencyFormatter.format(item.getSubtotal()));
                if (listener != null) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            // Delete item
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDeleted(item, getAdapterPosition());
                }
            });

            // Click on item to view book details
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClicked(item);
                }
            });
        }
    }
}