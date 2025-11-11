// Trong file: com/hofang/bookchainfe/ui/categories/CategoriesFragment.java
package com.hofang.bookchainfe.ui.categories;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation; // <-- THÊM IMPORT
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Category;
import com.hofang.bookchainfe.model.ChatContext;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.BookApiService;
import com.hofang.bookchainfe.ui.chatbot.FloatingChatButtonHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// --- BƯỚC 1: IMPLEMENT INTERFACE ---
public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "CategoriesFragment";

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private ImageButton btnFilter;
    private ProgressBar progressBar;
    private BookApiService apiService;

    // (onCreateView giữ nguyên)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiConfig.getBookApiService();

        recyclerView = view.findViewById(R.id.categories_recycler_view);
        btnFilter = view.findViewById(R.id.btn_filter);
        progressBar = view.findViewById(R.id.progress_bar_categories);

        setupRecyclerView(); // <-- Sửa đổi trong hàm này
        fetchCategories();

        btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        ViewGroup parentView = (ViewGroup) requireActivity().findViewById(android.R.id.content);
        ChatContext chatContext = new ChatContext("Danh mục sách", null, null);
        FloatingChatButtonHelper.addFloatingChatButton(requireActivity(), parentView, chatContext);
    }

    private void setupRecyclerView() {
        // --- BƯỚC 2: TRUYỀN 'this' (FRAGMENT) VÀO ADAPTER ---
        adapter = new CategoryAdapter(getContext(), new ArrayList<>(), this);
        // --- KẾT THÚC BƯỚC 2 ---

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    // (fetchCategories giữ nguyên)
    private void fetchCategories() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                progressBar.setVisibility(View.GONE);
                if (getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setData(response.body());
                } else {
                    Log.e(TAG, "Lỗi khi tải Categories: " + response.message());
                    Toast.makeText(getContext(), "Không thể tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (getContext() == null) return;
                Log.e(TAG, "Lỗi mạng khi tải Categories: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // (showFilterBottomSheet giữ nguyên)
    private void showFilterBottomSheet() {
        FilterBottomSheetFragment bottomSheet = new FilterBottomSheetFragment();
        bottomSheet.show(getChildFragmentManager(), "FilterBottomSheetTag");
    }

    // --- BƯỚC 3: IMPLEMENT HÀM CLICK ---
    @Override
    public void onCategoryClick(Category category) {
        // Tạo Bundle để gửi dữ liệu
        Bundle args = new Bundle();
        args.putString("categoryId", category.getId());
        args.putString("categoryName", category.getName());

        try {
            // Điều hướng đến AllBooksFragment
            // !! QUAN TRỌNG: Bạn cần tạo action này trong file nav_graph.xml
            // (Từ CategoriesFragment -> AllBooksFragment)
            // Hoặc nếu bạn đã có, hãy đảm bảo ID là đúng
            int actionId = R.id.action_categoriesFragment_to_allBooksFragment; // <-- Tự định nghĩa ID này

            Navigation.findNavController(requireView()).navigate(actionId, args);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi điều hướng: " + e.getMessage());
            Toast.makeText(getContext(), "Lỗi điều hướng", Toast.LENGTH_SHORT).show();
            // Thử điều hướng bằng ID của fragment (cách dự phòng)
            // Navigation.findNavController(requireView()).navigate(R.id.allBooksFragment, args);
        }
    }
}