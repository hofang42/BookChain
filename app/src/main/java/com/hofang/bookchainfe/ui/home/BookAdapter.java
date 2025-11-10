package com.hofang.bookchainfe.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.ui.bookdetail.BookDetailActivity;
import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_CARD = 1;  // item_book_card.xml
    public static final int VIEW_TYPE_DEAL = 2;  // item_book_deal.xml

    private Context context;
    private ArrayList<BookItem> bookList;
    private int viewType;

    public BookAdapter(Context context, ArrayList<BookItem> bookList, int viewType) {
        this.context = context;
        this.bookList = bookList;
        this.viewType = viewType;
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
            return new DealViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BookItem book = bookList.get(position);

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> openBookDetail(book));

        if (holder.getItemViewType() == VIEW_TYPE_CARD) {
            CardViewHolder cardHolder = (CardViewHolder) holder;
            cardHolder.ivBookCover.setImageResource(book.getCoverImageResId());
            cardHolder.tvBookCategory.setText(book.getCategory());
            cardHolder.tvBookTitle.setText(book.getTitle());
            cardHolder.tvBookAuthor.setText(book.getAuthor());

            String price = book.getPrice();
            if (price != null && !price.isEmpty()) {
                cardHolder.tvBookPrice.setText(price);
                cardHolder.tvBookPrice.setVisibility(View.VISIBLE);
            } else {
                cardHolder.tvBookPrice.setVisibility(View.GONE);
            }

        } else {
            DealViewHolder dealHolder = (DealViewHolder) holder;
            dealHolder.ivBookCover.setImageResource(book.getCoverImageResId());
            dealHolder.tvBookCategory.setText(book.getCategory());
            dealHolder.tvBookTitle.setText(book.getTitle());
            dealHolder.tvBookAuthor.setText(book.getAuthor());
            dealHolder.tvBookPrice.setText(book.getPrice());

            String discount = book.getDiscount();
            if (discount != null && !discount.isEmpty() && !discount.equals("0%")) {
                dealHolder.tvBookDiscount.setText(discount);
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
        intent.putExtra("category", book.getCategory());
        intent.putExtra("coverResId", book.getCoverImageResId());
        
        // Parse price
        if (book.getPrice() != null && !book.getPrice().isEmpty()) {
            String priceStr = book.getPrice().replace("$", "").trim();
            try {
                double price = Double.parseDouble(priceStr);
                intent.putExtra("price", price);
            } catch (NumberFormatException e) {
                intent.putExtra("price", 0.0);
            }
        }
        
        // Parse discount
        if (book.getDiscount() != null && !book.getDiscount().isEmpty()) {
            String discountStr = book.getDiscount().replace("% off", "").replace("%", "").trim();
            try {
                int discount = Integer.parseInt(discountStr);
                intent.putExtra("discount", discount);
            } catch (NumberFormatException e) {
                intent.putExtra("discount", 0);
            }
        }
        
        // Default description
        intent.putExtra("description", "This is a great book. More details coming soon.");
        intent.putExtra("rating", 4.11);
        
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}