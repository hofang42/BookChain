package com.hofang.bookchainfe.ui.book;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hofang.bookchainfe.R;
// Bỏ import ApiConfig vì không cần dùng BASE_URL ở đây nữa
// import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.ui.home.BookItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AllBooksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private List<BookItem> bookList = new ArrayList<>();
    private Context context;
    private NumberFormat currencyFormatter;
    private OnBookClickListener listener;

    // Interface for click events
    public interface OnBookClickListener {
        void onBookClick(BookItem book);
        void onCartClick(BookItem book);
    }

    public AllBooksAdapter(Context context, OnBookClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @Override
    public int getItemViewType(int position) {
        return bookList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.book_item_book_list, parent, false);
            return new BookViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.book_item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ((BookViewHolder) holder).bind(bookList.get(position), context, currencyFormatter, listener);
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    // (Các hàm setData, addData, addLoadingFooter, removeLoadingFooter giữ nguyên)
    public void setData(List<BookItem> newBooks) {
        bookList.clear();
        if (newBooks != null) {
            bookList.addAll(newBooks);
        }
        notifyDataSetChanged();
    }

    public void addData(List<BookItem> moreBooks) {
        if (moreBooks != null) {
            bookList.addAll(moreBooks);
            notifyDataSetChanged();
        }
    }

    public void addLoadingFooter() {
        bookList.add(null);
        notifyItemInserted(bookList.size() - 1);
    }

    public void removeLoadingFooter() {
        if (bookList.isEmpty()) return;
        int position = bookList.size() - 1;
        if (bookList.get(position) == null) {
            bookList.remove(position);
            notifyItemRemoved(position);
        }
    }


    // --- ViewHolders ---

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle, tvBookAuthor, tvBookPrice;
        ImageButton btnAddToCart;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvBookAuthor = itemView.findViewById(R.id.tv_book_author);
            tvBookPrice = itemView.findViewById(R.id.tv_book_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }

        void bind(BookItem book, Context context, NumberFormat formatter, OnBookClickListener listener) {
            tvBookTitle.setText(book.getTitle());
            tvBookAuthor.setText(book.getAuthor());
            tvBookPrice.setText(formatter.format(book.getPrice()));

            // --- BẮT ĐẦU SỬA LỖI ---
            // Đường dẫn 'book.getCoverImage()' đã là URL đầy đủ từ Cloudinary
            String imageUrl = book.getCoverImage();

            Glide.with(context)
                    .load(imageUrl) // Load trực tiếp URL đầy đủ
                    .placeholder(R.drawable.discount_badge_bg) // (Bạn nên đổi placeholder này)
                    .error(R.drawable.ic_book_placeholder) // Thêm error placeholder
                    .into(ivBookCover);
            // --- KẾT THÚC SỬA LỖI ---

            // Click listeners
            itemView.setOnClickListener(v -> listener.onBookClick(book));
            btnAddToCart.setOnClickListener(v -> listener.onCartClick(book));
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}