package com.hofang.bookchainfe.ui.bookdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // <-- THÊM IMPORT

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Book;

import java.io.Serializable; // <-- THÊM IMPORT
import java.text.NumberFormat; // <-- THÊM IMPORT
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
    private TextView tvBranchName; // <-- THÊM VIEW NÀY

    private Book book;
    private int quantity = 1;
    private int maxStock; // Sẽ nhận từ newInstance
    private String branchName; // Sẽ nhận từ newInstance
    private AddToCartListener listener;
    private NumberFormat currencyFormatter; // Để format tiền VN

    public interface AddToCartListener {
        void onAddToCart(Book book, int quantity);
    }

    // --- THAY ĐỔI: Nhận thêm stock và branchName ---
    public static AddToCartBottomSheet newInstance(Book book, int stock, String branchName, AddToCartListener listener) {
        AddToCartBottomSheet fragment = new AddToCartBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("book", (Serializable) book); // Cast book cho an toàn
        args.putInt("stock", stock);
        args.putString("branchName", branchName);
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = (Book) getArguments().getSerializable("book");
            maxStock = getArguments().getInt("stock");
            branchName = getArguments().getString("branchName");
        }
        // Khởi tạo định dạng tiền VN
        Locale localeVN = new Locale("vi", "VN");
        currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
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

        // --- THÊM MỚI: Ánh xạ TextView cho tên chi nhánh ---
        // (Bạn cần thêm TextView này vào file bottom_sheet_add_to_cart.xml)
        tvBranchName = view.findViewById(R.id.tv_branch_name);
    }

    private void setupData() {
        if (book == null) {
            ivBookImage.setImageResource(R.drawable.ic_book_placeholder);
            tvDiscountedPrice.setText(currencyFormatter.format(0));
            tvStock.setText("Kho: 0");
            return;
        }

        if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
            Glide.with(requireContext())
                    .load(book.getCoverImage())
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(ivBookImage);
        } else {
            ivBookImage.setImageResource(R.drawable.ic_book_placeholder);
        }

        // --- THAY ĐỔI: Dùng currencyFormatter ---
        double finalPrice = book.getFinalPrice() != null ? book.getFinalPrice() :
                (book.getPrice() != null ? book.getPrice() : 0.0);
        tvDiscountedPrice.setText(currencyFormatter.format(finalPrice));

        if (book.getDiscount() != null && book.getDiscount() > 0 && book.getPrice() != null) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setText(currencyFormatter.format(book.getPrice()));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
        }
        // --- KẾT THÚC THAY ĐỔI ---

        // --- THAY ĐỔI: Sử dụng stock và branchName thật ---
        tvStock.setText(String.format(Locale.US, "Kho: %d", maxStock));

        if (tvBranchName != null && branchName != null) {
            tvBranchName.setText("Tại chi nhánh: " + branchName);
            tvBranchName.setVisibility(View.VISIBLE);
        } else if (tvBranchName != null) {
            tvBranchName.setVisibility(View.GONE);
        }
        // --- KẾT THÚC THAY ĐỔI ---

        llVariantSection.setVisibility(View.GONE);
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
            // --- THAY ĐỔI: Kiểm tra tồn kho thật ---
            if (quantity < maxStock) {
                quantity++;
                updateQuantityDisplay();
            } else {
                Toast.makeText(getContext(), "Đã đạt số lượng tồn kho tối đa", Toast.LENGTH_SHORT).show();
            }
            // --- KẾT THÚC THAY ĐỔI ---
        });

        btnAddToCart.setOnClickListener(v -> {
            // --- THAY ĐỔI: Kiểm tra tồn kho trước khi thêm ---
            if (maxStock <= 0) {
                Toast.makeText(getContext(), "Sản phẩm đã hết hàng tại chi nhánh này", Toast.LENGTH_SHORT).show();
                return;
            }
            if (quantity > maxStock) {
                Toast.makeText(getContext(), "Số lượng vượt quá tồn kho", Toast.LENGTH_SHORT).show();
                return;
            }
            // --- KẾT THÚC THAY ĐỔI ---

            if (listener != null) {
                listener.onAddToCart(book, quantity);
            }
            dismiss();
        });
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(quantity));

        btnDecreaseQty.setEnabled(quantity > 1);
        btnDecreaseQty.setAlpha(quantity > 1 ? 1.0f : 0.3f);

        btnIncreaseQty.setEnabled(quantity < maxStock);
        btnIncreaseQty.setAlpha(quantity < maxStock ? 1.0f : 0.3f);
    }
}