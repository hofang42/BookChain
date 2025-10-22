package com.hofang.bookchainfe.ui.categories; // Đảm bảo đúng package

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // <-- THÊM IMPORT NÀY
import com.hofang.bookchainfe.R; // Đảm bảo R được import đúng

import java.util.ArrayList;

public class CategoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private ArrayList<CategoryItem> categoryList;

    private ImageButton btnFilter; // <-- 1. KHAI BÁO NÚT FILTER

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Tải layout fragment_categories.xml
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Phần code RecyclerView của bạn (Giữ nguyên) ---
        recyclerView = view.findViewById(R.id.categories_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        categoryList = new ArrayList<>();
        categoryList.add(new CategoryItem("Non-fiction", R.drawable.non_fiction));
        categoryList.add(new CategoryItem("Classics", R.drawable.classic_book));
        categoryList.add(new CategoryItem("Fantasy", R.drawable.fantasy_books));
        categoryList.add(new CategoryItem("Young Adult", R.drawable.young_adults));
        categoryList.add(new CategoryItem("Crime", R.drawable.crime));
        categoryList.add(new CategoryItem("Horror", R.drawable.horror));
        categoryList.add(new CategoryItem("Sci-fi", R.drawable.sci_fi));
        categoryList.add(new CategoryItem("Drama", R.drawable.drama));

        adapter = new CategoryAdapter(getContext(), categoryList);
        recyclerView.setAdapter(adapter);
        // --- Hết phần code RecyclerView ---


        // --- 2. CODE MỚI ĐỂ XỬ LÝ NÚT FILTER ---

        // Ánh xạ nút filter từ layout
        btnFilter = view.findViewById(R.id.btn_filter);

        // Gán sự kiện click
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi hàm hiển thị Bottom Sheet
                showFilterBottomSheet();
            }
        });
    }

    // 3. HÀM MỚI ĐỂ HIỂN THỊ BOTTOM SHEET
    private void showFilterBottomSheet() {
        // Tạo một instance của FilterBottomSheetFragment (bạn đã tạo ở bước trước)
        FilterBottomSheetFragment bottomSheet = new FilterBottomSheetFragment();

        // Hiển thị nó. Dùng getChildFragmentManager() vì bạn đang gọi từ bên trong một Fragment
        bottomSheet.show(getChildFragmentManager(), "FilterBottomSheetTag");
    }
}