package com.hofang.bookchainfe.ui.home;

import android.content.Context;
import android.graphics.Paint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.network.ApiConfig; // Import ApiConfig

import java.text.NumberFormat;
import com.hofang.bookchainfe.ui.bookdetail.BookDetailActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_CARD = 1;  // item_book_card.xml
    public static final int VIEW_TYPE_DEAL = 2;  // item_book_deal.xml

    private Context context;
    private ArrayList<BookItem> bookList;
    private int viewType;

    // Dùng để định dạng tiền tệ (vd: 350.000 ₫)
    private NumberFormat currencyFormatter;

    public BookAdapter(Context context, ArrayList<BookItem> bookList, int viewType) {
        this.context = context;
        this.bookList = bookList;
        this.viewType = viewType;
        // Khởi tạo formatter cho Tiếng Việt
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @Override
    public int getItemViewType(int position) {
        return this.viewType;
    }

    // ViewHolder cho item_book_card.xml
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookCategory, tvBookTitle, tvBookAuthor, tvBookPrice;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvBookCategory = itemView.findViewById(R.id.tv_book_category);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvBookAuthor = itemView.findViewById(R.id.tv_book_author);
            tvBookPrice = itemView.findViewById(R.id.tv_book_price);
        }
    }

    // ViewHolder cho item_book_deal.xml
    public static class DealViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookCategory, tvBookTitle, tvBookAuthor, tvBookPrice, tvBookDiscount;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvBookCategory = itemView.findViewById(R.id.tv_book_category);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvBookAuthor = itemView.findViewById(R.id.tv_book_author);
            tvBookPrice = itemView.findViewById(R.id.tv_book_price);
            tvBookDiscount = itemView.findViewById(R.id.tv_book_discount);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_CARD) {
            view = LayoutInflater.from(context).inflate(R.layout.home_item_book_card, parent, false);
            return new CardViewHolder(view);
        } else { // (viewType == VIEW_TYPE_DEAL)
            view = LayoutInflater.from(context).inflate(R.layout.home_item_book_deal, parent, false);

            // Thêm: Tự động gạch ngang giá cho item deal
            TextView priceTextView = view.findViewById(R.id.tv_book_price);
            if (priceTextView != null) {
                priceTextView.setPaintFlags(priceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            return new DealViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BookItem book = bookList.get(position);

        // Lấy thông tin cơ bản
        String title = book.getTitle();
        String author = book.getAuthor();
        String category = (book.getCategory() != null) ? book.getCategory().getName() : "N/A";
        double price = book.getPrice();

        // --- Xử lý tải ảnh ---
        // Kiểm tra xem đây là data hard-code (Upcoming) hay data API
        if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
            // Data từ API: Dùng Glide để tải URL

            // Xử lý BASE_URL để tránh lỗi double slash "//"
            String baseUrl = ApiConfig.BASE_URL;
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            String imageUrl = baseUrl + book.getCoverImage();

            if (holder.getItemViewType() == VIEW_TYPE_CARD) {
                Glide.with(context).load(imageUrl).placeholder(R.drawable.discount_badge_bg).into(((CardViewHolder) holder).ivBookCover);
            } else {
                Glide.with(context).load(imageUrl).placeholder(R.drawable.discount_badge_bg).into(((DealViewHolder) holder).ivBookCover);
            }
        } else if (book.getCoverImageResId() != 0) {
            // Data hard-code (Upcoming): Dùng ResId
            int imageResId = book.getCoverImageResId();
            if (holder.getItemViewType() == VIEW_TYPE_CARD) {
                ((CardViewHolder) holder).ivBookCover.setImageResource(imageResId);
            } else {
                ((DealViewHolder) holder).ivBookCover.setImageResource(imageResId);
            }
        }
        // --- Hết xử lý ảnh ---
        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> openBookDetail(book));

        if (holder.getItemViewType() == VIEW_TYPE_CARD) {
            CardViewHolder cardHolder = (CardViewHolder) holder;
            cardHolder.tvBookCategory.setText(category);
            cardHolder.tvBookTitle.setText(title);
            cardHolder.tvBookAuthor.setText(author);

            // Định dạng giá tiền
            if (price > 0) {
                cardHolder.tvBookPrice.setText(currencyFormatter.format(price));
                cardHolder.tvBookPrice.setVisibility(View.VISIBLE);
            } else {
                cardHolder.tvBookPrice.setVisibility(View.GONE);
            }

        } else {
            DealViewHolder dealHolder = (DealViewHolder) holder;
            dealHolder.tvBookCategory.setText(category);
            dealHolder.tvBookTitle.setText(title);
            dealHolder.tvBookAuthor.setText(author);

            // Hiển thị giá gốc (bị gạch)
            dealHolder.tvBookPrice.setText(currencyFormatter.format(price));

            // Tính toán và hiển thị % giảm giá
            double discountPercent = book.getDiscount();
            if (discountPercent > 0) {
                // Làm tròn và hiển thị (vd: 10% off)
                String discountText = String.format(Locale.US, "%.0f%% off", discountPercent);
                dealHolder.tvBookDiscount.setText(discountText);
                dealHolder.tvBookDiscount.setVisibility(View.VISIBLE);
            } else {
                dealHolder.tvBookDiscount.setVisibility(View.GONE);
            }
        }
    }

    private void openBookDetail(BookItem book) {
        Intent intent = new Intent(context, BookDetailActivity.class);

        // Pass book data to detail activity
        intent.putExtra("title", book.getTitle());
        intent.putExtra("author", book.getAuthor());

        // --- SỬA 1: Truyền tên category (String), không phải đối tượng Category ---
        String categoryName = (book.getCategory() != null) ? book.getCategory().getName() : "N/A";
        intent.putExtra("category", categoryName);

        // --- SỬA 3: Thêm logic truyền cả URL ảnh (cho sách từ API) ---
        intent.putExtra("coverResId", book.getCoverImageResId()); // Cho sách hard-code
        intent.putExtra("coverUrl", book.getCoverImage());       // Cho sách từ API

        // --- SỬA 2: Truyền giá (price) và giảm giá (discount) dưới dạng số (double/int) ---
        // (Giả định getPrice() trả về double, dựa trên onBindViewHolder)
        intent.putExtra("price", book.getPrice());

        // (Giả định getDiscount() trả về double, dựa trên onBindViewHolder)
        // Ép kiểu thành int nếu BookDetailActivity mong đợi số nguyên % (vd: 10)
        intent.putExtra("discount", (int) book.getDiscount());

        // Default description
        intent.putExtra("description", "This is a great book. More details coming soon.");
        intent.putExtra("rating", 4.11);

        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    /**
     * Hàm mới: Cập nhật dữ liệu cho adapter khi API gọi thành công
     */
    public void updateData(List<BookItem> newBooks) {
        if (newBooks == null) return;
        bookList.clear();
        bookList.addAll(newBooks);
        notifyDataSetChanged();
    }
}