package com.hofang.bookchainfe.ui.home; // Hoặc package adapter của bạn

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hofang.bookchainfe.R;
import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    private List<BookItem> suggestionList = new ArrayList<>();
    private final OnSuggestionClickListener listener;

    // Interface để xử lý click
    public interface OnSuggestionClickListener {
        void onBookClick(BookItem book);
        void onSeeAllClick(String query);
    }

    public SearchSuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == suggestionList.size()) {
            return VIEW_TYPE_FOOTER; // Vị trí cuối cùng là footer
        }
        return VIEW_TYPE_ITEM; // Còn lại là item
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ITEM) {
            View view = inflater.inflate(R.layout.home_search_suggestion_item, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.home_search_suggestion_footer, parent, false);
            return new FooterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            BookItem book = suggestionList.get(position);
            itemViewHolder.bind(book, listener);
        } else {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.bind(listener);
        }
    }

    @Override
    public int getItemCount() {
        if (suggestionList.isEmpty()) {
            return 0; // Không hiển thị gì nếu list rỗng
        }
        return suggestionList.size() + 1; // +1 cho footer "Xem tất cả"
    }

    // Cập nhật dữ liệu
    public void updateData(List<BookItem> newSuggestions) {
        suggestionList.clear();
        suggestionList.addAll(newSuggestions);
        notifyDataSetChanged();
    }

    // Xóa dữ liệu
    public void clearData() {
        suggestionList.clear();
        notifyDataSetChanged();
    }

    // --- ViewHolders ---

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_suggestion_name);
        }

        void bind(BookItem book, OnSuggestionClickListener listener) {
            tvName.setText(book.getTitle()); // Giả sử BookItem có getTitle()
            itemView.setOnClickListener(v -> listener.onBookClick(book));
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(OnSuggestionClickListener listener) {
            itemView.setOnClickListener(v -> {
                // Lấy query từ adapter (cần cải tiến nếu muốn)
                // Hiện tại chúng ta chỉ cần biết là bấm xem tất cả
                listener.onSeeAllClick(""); // Bạn có thể truyền query vào đây
            });
        }
    }
}