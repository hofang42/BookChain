package com.hofang.bookchainfe.ui.bookdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Book;

import java.util.Locale;

public class AddToCartBottomSheet extends BottomSheetDialogFragment {
    private ImageView btnClose;
    private ImageView ivBookImage;
    private TextView tvDiscountedPrice;
    private TextView tvOriginalPrice;
    private TextView tvStock;
    private LinearLayout llVariantSection;
    private TextView btnDecreaseQty;
    private TextView tvQuantity;
    private TextView btnIncreaseQty;
    private Button btnAddToCart;

    private Book book;
    private int quantity = 1;
    private int maxStock = 999;
    private AddToCartListener listener;

    public interface AddToCartListener {
        void onAddToCart(Book book, int quantity);
    }

    public static AddToCartBottomSheet newInstance(Book book, AddToCartListener listener) {
        AddToCartBottomSheet fragment = new AddToCartBottomSheet();
        fragment.book = book;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_to_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupData();
        setupListeners();
        
        // Expand bottom sheet fully to show button
        view.post(() -> {
            android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params != null) {
                params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        
        // Expand bottom sheet to show all content including button
        if (getDialog() != null && getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }

    private void initViews(View view) {
        btnClose = view.findViewById(R.id.btn_close);
        ivBookImage = view.findViewById(R.id.iv_book_image);
        tvDiscountedPrice = view.findViewById(R.id.tv_discounted_price);
        tvOriginalPrice = view.findViewById(R.id.tv_original_price);
        tvStock = view.findViewById(R.id.tv_stock);
        llVariantSection = view.findViewById(R.id.ll_variant_section);
        btnDecreaseQty = view.findViewById(R.id.btn_decrease_qty);
        tvQuantity = view.findViewById(R.id.tv_quantity);
        btnIncreaseQty = view.findViewById(R.id.btn_increase_qty);
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart);
    }

    private void setupData() {
        if (book == null) {
            // Set default placeholder if book is null
            ivBookImage.setImageResource(R.drawable.ic_book_placeholder);
            tvDiscountedPrice.setText("$0.00");
            tvStock.setText("Kho: 0");
            return;
        }

        // Load book image
        if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
            Glide.with(requireContext())
                    .load(book.getCoverImage())
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(ivBookImage);
        } else {
            // Set placeholder if no cover image
            ivBookImage.setImageResource(R.drawable.ic_book_placeholder);
        }

        // Set prices
        double finalPrice = book.getFinalPrice() != null ? book.getFinalPrice() : 
                           (book.getPrice() != null ? book.getPrice() : 0.0);
        tvDiscountedPrice.setText(String.format(Locale.US, "$%.2f", finalPrice));

        if (book.getDiscount() != null && book.getDiscount() > 0 && book.getPrice() != null) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setText(String.format(Locale.US, "$%.2f", book.getPrice()));
            // Add strikethrough
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
        }

        // Set stock (mock data - TODO: get from backend)
        maxStock = 100; // Default stock
        tvStock.setText(String.format(Locale.US, "Kho: %d", maxStock));

        // Hide variant section for books (books typically don't have variants)
        llVariantSection.setVisibility(View.GONE);

        // Set initial quantity
        updateQuantityDisplay();
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnDecreaseQty.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityDisplay();
            }
        });

        btnIncreaseQty.setOnClickListener(v -> {
            if (quantity < maxStock) {
                quantity++;
                updateQuantityDisplay();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(book, quantity);
            }
            dismiss();
        });
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(quantity));
        
        // Disable/enable buttons based on quantity
        btnDecreaseQty.setEnabled(quantity > 1);
        btnDecreaseQty.setAlpha(quantity > 1 ? 1.0f : 0.3f);
        
        btnIncreaseQty.setEnabled(quantity < maxStock);
        btnIncreaseQty.setAlpha(quantity < maxStock ? 1.0f : 0.3f);
    }
}
