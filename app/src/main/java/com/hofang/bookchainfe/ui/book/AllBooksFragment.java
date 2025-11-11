package com.hofang.bookchainfe.ui.book;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.BookListResponse;
import com.hofang.bookchainfe.model.CartAddRequest;
import com.hofang.bookchainfe.model.Category; // <-- SỬA Ở ĐÂY: Import model Category
import com.hofang.bookchainfe.model.UploadResponse;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.BookApiService;
import com.hofang.bookchainfe.network.CartApiService;
import com.hofang.bookchainfe.ui.bookdetail.BranchesMapBottomSheet;
import com.hofang.bookchainfe.ui.home.BookItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllBooksFragment extends Fragment implements AllBooksAdapter.OnBookClickListener {

    private static final String TAG = "AllBooksFragment";
    private RecyclerView rvAllBooks;
    private AllBooksAdapter adapter;
    private BookApiService apiService;
    private CartApiService cartApiService;

    // UI
    private Toolbar toolbar;
    private SearchView searchView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    // Logic Phân trang & Sort
    private boolean isLoading = false;
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private String currentQuery = "";
    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int selectedSortIndex = 0;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // --- Logic Lọc Category ---
    private String currentCategoryId = null;
    private int selectedCategoryIndex = 0;
    private List<Category> categoryList = new ArrayList<>(); // <-- SỬA Ở ĐÂY: Dùng model Category
    // --- KẾT THÚC SỬA ĐỔI ---

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_books, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        apiService = ApiConfig.getBookApiService();
        cartApiService = ApiConfig.getCartApiService();

        setupToolbarAndMenu();

        if (getArguments() != null) {
            currentQuery = getArguments().getString("searchQuery", "");

            // (Code xử lý categoryId giữ nguyên)
            if (getArguments().containsKey("categoryId")) {
                currentCategoryId = getArguments().getString("categoryId");
                String categoryName = getArguments().getString("categoryName", "");
                if (!categoryName.isEmpty() && toolbar != null) {
                    toolbar.setSubtitle("Lọc theo: " + categoryName);
                }
            }

            // --- BẮT ĐẦU THÊM MỚI ---
            // Đọc tham số Sắp xếp (Sort)
            if (getArguments().containsKey("sortBy") && getArguments().getString("sortBy") != null) {
                sortBy = getArguments().getString("sortBy");
                sortOrder = getArguments().getString("sortOrder", "desc"); // Mặc định là "desc"

                // Cập nhật lại index để Dialog sắp xếp hiển thị đúng
                if (sortBy.equals("salesCount")) {
                    selectedSortIndex = 1; // "Bán chạy nhất"
                } else if (sortBy.equals("rating")) {
                    selectedSortIndex = 2; // "Đánh giá cao nhất"
                } else if (sortBy.equals("price") && sortOrder.equals("asc")) {
                    selectedSortIndex = 3; // "Giá: Thấp đến Cao"
                } else if (sortBy.equals("price") && sortOrder.equals("desc")) {
                    selectedSortIndex = 4; // "Giá: Cao đến Thấp"
                } else {
                    selectedSortIndex = 0; // "Mới nhất"
                }
            }
            // --- KẾT THÚC THÊM MỚI ---
        }

        setupRecyclerView();
        fetchBooks(currentQuery, true); // Gọi fetchBooks (lúc này đã có sortBy/sortOrder)
        fetchCategories();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        rvAllBooks = view.findViewById(R.id.rv_all_books);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
    }

    private void setupToolbarAndMenu() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.all_books_menu, menu);
                MenuItem searchItem = menu.findItem(R.id.action_search);
                searchView = (SearchView) searchItem.getActionView();
                setupSearchViewListeners();

                if (!currentQuery.isEmpty()) {
                    searchView.setIconified(false);
                    searchView.setQuery(currentQuery, false);
                    searchView.clearFocus();
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == android.R.id.home) {
                    Navigation.findNavController(requireView()).popBackStack();
                    return true;
                }
                // (ID này phải khớp với menu/all_books_menu.xml)
                else if (itemId == R.id.action_filter_sort) {
                    showSortDialog();
                    return true;
                }
                // (ID này phải khớp với menu/all_books_menu.xml)
                else if (itemId == R.id.action_filter_category) {
                    showCategoryFilterDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void setupSearchViewListeners() {
        // (Không thay đổi)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> performSearch(newText);
                searchHandler.postDelayed(searchRunnable, 500);
                return true;
            }
        });
    }

    private void showSortDialog() {
        // (Không thay đổi so với lần trước)
        if (getContext() == null) return;
        String[] options = {
                "Mới nhất",
                "Bán chạy nhất",
                "Đánh giá cao nhất",
                "Giá: Thấp đến Cao",
                "Giá: Cao đến Tháp"
        };

        new AlertDialog.Builder(getContext())
                .setTitle("Sắp xếp theo")
                .setSingleChoiceItems(options, selectedSortIndex, (dialog, which) -> {
                    selectedSortIndex = which;
                    switch (which) {
                        case 0: sortBy = "createdAt"; sortOrder = "desc"; break;
                        case 1: sortBy = "salesCount"; sortOrder = "desc"; break;
                        case 2: sortBy = "rating"; sortOrder = "desc"; break;
                        case 3: sortBy = "price"; sortOrder = "asc"; break;
                        case 4: sortBy = "price"; sortOrder = "desc"; break;
                    }
                    fetchBooks(currentQuery, true);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- Tải Categories về Cache ---
    private void fetchCategories() {
        if (!categoryList.isEmpty()) {
            return;
        }
        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body());
                    Log.d(TAG, "Tải thành công " + categoryList.size() + " danh mục.");

                    // --- THÊM MỚI ---
                    // Sau khi tải xong, kiểm tra xem có cần set index mặc định không
                    if (currentCategoryId != null) {
                        for (int i = 0; i < categoryList.size(); i++) {
                            if (categoryList.get(i).getId().equals(currentCategoryId)) {
                                selectedCategoryIndex = i + 1; // +1 vì index 0 là "Tất cả"
                                break;
                            }
                        }
                    }
                    // --- KẾT THÚC THÊM MỚI ---
                } else {
                    Log.e(TAG, "Lỗi khi tải danh mục: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi mạng khi tải danh mục: " + t.getMessage());
            }
        });
    }

    // --- Hiển thị Dialog Lọc Category ---
    private void showCategoryFilterDialog() {
        if (getContext() == null) return;

        if (categoryList.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải danh mục, vui lòng thử lại...", Toast.LENGTH_SHORT).show();
            fetchCategories();
            return;
        }

        String[] categoryNames = new String[categoryList.size() + 1];
        categoryNames[0] = "Tất cả danh mục";
        for (int i = 0; i < categoryList.size(); i++) {
            categoryNames[i + 1] = categoryList.get(i).getName(); // Dùng model Category
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Lọc theo danh mục")
                .setSingleChoiceItems(categoryNames, selectedCategoryIndex, (dialog, which) -> {
                    selectedCategoryIndex = which;

                    if (which == 0) {
                        currentCategoryId = null;
                    } else {
                        currentCategoryId = categoryList.get(which - 1).getId(); // Dùng model Category
                    }

                    fetchBooks(currentQuery, true);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    private void performSearch(String query) {
        // (Không thay đổi)
        currentQuery = query.trim();
        fetchBooks(currentQuery, true);
    }

    private void setupRecyclerView() {
        // (Không thay đổi)
        adapter = new AllBooksAdapter(getContext(), this);
        rvAllBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAllBooks.setAdapter(adapter);
        rvAllBooks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (currentPage < totalPages)) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        currentPage++;
                        fetchBooks(currentQuery, false);
                    }
                }
            }
        });
    }

    private void fetchBooks(String query, boolean isReset) {
        // (Không thay đổi)
        if (isLoading) return;
        isLoading = true;
        if (isReset) {
            currentPage = 1;
            totalPages = 1;
            progressBar.setVisibility(View.VISIBLE);
            rvAllBooks.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.GONE);
        } else {
            adapter.addLoadingFooter();
        }

        // Gọi API với currentCategoryId
        apiService.getAllBooks(currentPage, PAGE_SIZE, query, sortBy, sortOrder, currentCategoryId)
                .enqueue(new Callback<BookListResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BookListResponse> call, @NonNull Response<BookListResponse> response) {
                        if (getContext() == null) return;
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<BookItem> books = response.body().getData();

                            totalPages = response.body().getTotalPages();

                            if (currentPage == 1) {
                                adapter.setData(books);
                                rvAllBooks.scrollToPosition(0);
                                if (books.isEmpty()) {
                                    tvEmptyState.setVisibility(View.VISIBLE);
                                    rvAllBooks.setVisibility(View.GONE);
                                } else {
                                    tvEmptyState.setVisibility(View.GONE);
                                    rvAllBooks.setVisibility(View.VISIBLE);
                                }
                            } else {
                                adapter.removeLoadingFooter();
                                adapter.addData(books);
                            }
                        } else {
                            if (currentPage > 1) adapter.removeLoadingFooter();
                            Log.e(TAG, "fetchBooks error: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BookListResponse> call, @NonNull Throwable t) {
                        if (getContext() == null) return;
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                        if (currentPage > 1) adapter.removeLoadingFooter();
                        Log.e(TAG, "fetchBooks failure: " + t.getMessage());
                        tvEmptyState.setText("Lỗi kết nối.");
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvAllBooks.setVisibility(View.GONE);
                    }
                });
    }

    // --- Các hàm OnBookClickListener (Không thay đổi) ---

    @Override
    public void onBookClick(BookItem book) {
        // (Không thay đổi)
        Bundle args = new Bundle();
        args.putString("bookId", book.getId());
        args.putString("title", book.getTitle());
        args.putString("author", book.getAuthor());
        args.putString("category", book.getCategory() != null ? book.getCategory().getName() : "N/A");
        args.putFloat("price", (float) book.getPrice());
        args.putInt("discount", (int) book.getDiscount());
        args.putString("description", book.getDescription());
        args.putFloat("rating", (float) book.getRating());
        args.putString("coverImage", book.getCoverImage());

        try {
            Navigation.findNavController(requireView()).navigate(R.id.action_allBooks_to_book_detail, args);
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed", e);
        }
    }

    @Override
    public void onCartClick(BookItem book) {
        // (Không thay đổi)
        if (getContext() == null) return;
        showBranchesMap(book);
    }

    private void showBranchesMap(BookItem book) {
        // (Không thay đổi)
        BranchesMapBottomSheet bottomSheet = BranchesMapBottomSheet.newInstance(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCoverImage()
        );

        bottomSheet.setOnBranchSelectedListener((branch, stock) -> {
            if (stock <= 0) {
                Toast.makeText(getContext(), "Hết hàng tại chi nhánh " + branch.getName(), Toast.LENGTH_SHORT).show();
                return;
            }

            CartAddRequest request = new CartAddRequest(book.getId(), 1, branch.getId());
            cartApiService.addToCart(request).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(@NonNull Call<UploadResponse> call, @NonNull Response<UploadResponse> response) {
                    if (getContext() == null) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = "Failed to add to cart";
                        if (response.code() == 401) {
                            errorMessage = "Please login to add items";
                        } else if (response.errorBody() != null) {
                            try {
                                String errorString = response.errorBody().string();
                                JsonObject jsonObject = com.google.gson.JsonParser.parseString(errorString).getAsJsonObject();
                                if (jsonObject.has("message")) {
                                    errorMessage = jsonObject.get("message").getAsString();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
                            }
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<UploadResponse> call, @NonNull Throwable t) {
                    if (getContext() == null) return;
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheet.show(getParentFragmentManager(), "BranchesMapBottomSheet_AllBooks");
    }
}