package com.hofang.bookchainfe.ui.home;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hofang.bookchainfe.R;
// import com.hofang.bookchainfe.network.ApiConfig; // <-- Không cần import này nữa

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_CARD = 1;  // item_book_card.xml
    public static final int VIEW_TYPE_DEAL = 2;  // item_book_deal.xml

    private Context context;
    private ArrayList<BookItem> bookList;
    private int viewType;
    private NumberFormat currencyFormatter;

    public BookAdapter(Context context, ArrayList<BookItem> bookList, int viewType) {
        this.context = context;
        this.bookList = bookList;
        this.viewType = viewType;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @Override
    public int getItemViewType(int position) {
        return this.viewType;
    }

    // (ViewHolder cho CardViewHolder giữ nguyên)
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

    // (ViewHolder cho DealViewHolder giữ nguyên)
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

        String title = book.getTitle();
        String author = book.getAuthor();
        String category = (book.getCategory() != null) ? book.getCategory().getName() : "N/A";
        double price = book.getPrice();

        // --- BẮT ĐẦU SỬA LỖI ---
        if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
            // Data từ API: Dùng Glide để tải URL

            // Xóa bỏ logic nối 'baseUrl'
            String imageUrl = book.getCoverImage(); // <-- SỬA LẠI: Dùng trực tiếp URL

            if (holder.getItemViewType() == VIEW_TYPE_CARD) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_book_placeholder) // Đổi placeholder
                        .error(R.drawable.ic_book_placeholder) // Thêm error
                        .into(((CardViewHolder) holder).ivBookCover);
            } else {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_book_placeholder) // Đổi placeholder
                        .error(R.drawable.ic_book_placeholder) // Thêm error
                        .into(((DealViewHolder) holder).ivBookCover);
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
        // --- KẾT THÚC SỬA LỖI ---

        holder.itemView.setOnClickListener(v -> openBookDetail(book, v));

        if (holder.getItemViewType() == VIEW_TYPE_CARD) {
            // (Logic bind cho CardViewHolder giữ nguyên)
            CardViewHolder cardHolder = (CardViewHolder) holder;
            cardHolder.tvBookCategory.setText(category);
            cardHolder.tvBookTitle.setText(title);
            cardHolder.tvBookAuthor.setText(author);
            if (price > 0) {
                cardHolder.tvBookPrice.setText(currencyFormatter.format(price));
                cardHolder.tvBookPrice.setVisibility(View.VISIBLE);
            } else {
                cardHolder.tvBookPrice.setVisibility(View.GONE);
            }
        } else {
            // (Logic bind cho DealViewHolder giữ nguyên)
            DealViewHolder dealHolder = (DealViewHolder) holder;
            dealHolder.tvBookCategory.setText(category);
            dealHolder.tvBookTitle.setText(title);
            dealHolder.tvBookAuthor.setText(author);
            dealHolder.tvBookPrice.setText(currencyFormatter.format(price));
            double discountPercent = book.getDiscount();
            if (discountPercent > 0) {
                String discountText = String.format(Locale.US, "%.0f%% off", discountPercent);
                dealHolder.tvBookDiscount.setText(discountText);
                dealHolder.tvBookDiscount.setVisibility(View.VISIBLE);
            } else {
                dealHolder.tvBookDiscount.setVisibility(View.GONE);
            }
        }
    }

    // (Hàm openBookDetail giữ nguyên)
    private void openBookDetail(BookItem book, View view) {
        Bundle args = new Bundle();
        args.putString("bookId", book.getId());
        args.putString("title", book.getTitle());
        args.putString("author", book.getAuthor());
        String categoryName = (book.getCategory() != null) ? book.getCategory().getName() : "N/A";
        args.putString("category", categoryName);
        args.putInt("coverResId", book.getCoverImageResId());

        // --- BẮT ĐẦU SỬA LỖI ---
        // Đổi "coverUrl" thành "coverImage" để nó nhất quán
        args.putString("coverImage", book.getCoverImage());
        // --- KẾT THÚC SỬA LỖI ---

        args.putFloat("price", (float) book.getPrice());
        args.putInt("discount", (int) book.getDiscount());

        // Gửi mô tả và rating (Giả sử BookItem đã có)
        args.putString("description", book.getDescription());
        args.putFloat("rating", (float) book.getRating());

        Navigation.findNavController(view).navigate(R.id.action_home_to_book_detail, args);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    // (Hàm updateData giữ nguyên)
    public void updateData(List<BookItem> newBooks) {
        if (newBooks == null) return;
        bookList.clear();
        bookList.addAll(newBooks);
        notifyDataSetChanged();
    }
}