package com.hofang.bookchainfe.ui.categories; // Đảm bảo đúng package

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hofang.bookchainfe.R; // Đảm bảo R được import đúng

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private ArrayList<CategoryItem> categoryList;

    // Constructor
    public CategoryAdapter(Context context, ArrayList<CategoryItem> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate (tải) layout của một item
        View view = LayoutInflater.from(context).inflate(R.layout.categories_item_category_card, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // Lấy dữ liệu từ list tại vị trí 'position'
        CategoryItem currentItem = categoryList.get(position);

        // Gán dữ liệu vào các view
        holder.tvCategoryName.setText(currentItem.getName());
        holder.ivCategoryImage.setImageResource(currentItem.getImageResId());

        // Bạn có thể set sự kiện click ở đây
        // holder.itemView.setOnClickListener(v -> {
        //     // Xử lý khi nhấn vào category currentItem.getName()
        // });
    }

    @Override
    public int getItemCount() {
        return categoryList.size(); // Trả về số lượng item trong list
    }

    // Lớp ViewHolder để giữ các tham chiếu đến View
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