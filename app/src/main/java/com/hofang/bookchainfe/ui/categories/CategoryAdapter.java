// Trong file: com/hofang/bookchainfe/ui/categories/CategoryAdapter.java
package com.hofang.bookchainfe.ui.categories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;

    // --- BƯỚC 1: ĐỊNH NGHĨA INTERFACE ---
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }
    // --- KẾT THÚC BƯỚC 1 ---

    // --- BƯỚC 2: CẬP NHẬT CONSTRUCTOR ---
    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener; // <-- Gán listener
    }
    // --- KẾT THÚC BƯỚC 2 ---

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.categories_item_category_card, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category currentItem = categoryList.get(position);
        holder.tvCategoryName.setText(currentItem.getName());

        Glide.with(context)
                .load(currentItem.getImageUrl())
                .placeholder(R.drawable.ic_book_placeholder)
                .error(R.drawable.ic_book_placeholder)
                .into(holder.ivCategoryImage);

        // --- BƯỚC 3: GỌI LISTENER KHI CLICK ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(currentItem); // <-- Trả về category đã click
            }
        });
        // --- KẾT THÚC BƯỚC 3 ---
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void setData(List<Category> newCategories) {
        if (newCategories != null) {
            this.categoryList.clear();
            this.categoryList.addAll(newCategories);
            notifyDataSetChanged();
        }
    }

    // ViewHolder (Không đổi)
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCategoryImage;
        public TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryImage = itemView.findViewById(R.id.iv_category_image);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}