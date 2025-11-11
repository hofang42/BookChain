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
import androidx.appcompat.app.ActionBar; // <-- THÊM IMPORT NÀY
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
import com.hofang.bookchainfe.model.UploadResponse;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.BookApiService;
import com.hofang.bookchainfe.network.CartApiService;
import com.hofang.bookchainfe.ui.bookdetail.BranchesMapBottomSheet;
import com.hofang.bookchainfe.ui.home.BookItem;

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

    // (Các biến logic khác giữ nguyên)
    private boolean isLoading = false;
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private String currentQuery = "";
    private String sortBy = null;
    private String sortOrder = null;
    private int selectedSortIndex = 0;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

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

        setupToolbarAndMenu(); // <-- Sửa đổi trong hàm này

        if (getArguments() != null) {
            currentQuery = getArguments().getString("searchQuery", "");
        }

        setupRecyclerView();
        fetchBooks(currentQuery, true);
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        rvAllBooks = view.findViewById(R.id.rv_all_books);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
    }

    private void setupToolbarAndMenu() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // --- THÊM MỚI (PHẦN 1): Kích hoạt nút Back (Up) trên Toolbar ---
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            // (Bạn có thể thêm icon tùy chỉnh ở đây nếu muốn, nếu không nó sẽ dùng icon mặc định)
            // actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        // --- KẾT THÚC THÊM MỚI (PHẦN 1) ---

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
                // --- THÊM MỚI (PHẦN 2): Xử lý sự kiện bấm nút Back ---
                // android.R.id.home là ID mặc định của nút Back trên Toolbar
                if (menuItem.getItemId() == android.R.id.home) {
                    // Dùng NavController để quay lại màn hình trước đó
                    Navigation.findNavController(requireView()).popBackStack();
                    return true;
                }
                // --- KẾT THÚC THÊM MỚI (PHẦN 2) ---

                if (menuItem.getItemId() == R.id.action_filter) {
                    showFilterDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void setupSearchViewListeners() {
        // (Hàm này giữ nguyên)
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

    private void showFilterDialog() {
        // (Hàm này giữ nguyên)
        if (getContext() == null) return;
        String[] options = {"Mặc định", "Giá: Thấp đến Cao", "Giá: Cao đến Thấp"};
        new AlertDialog.Builder(getContext())
                .setTitle("Sắp xếp theo")
                .setSingleChoiceItems(options, selectedSortIndex, (dialog, which) -> {
                    selectedSortIndex = which;
                    switch (which) {
                        case 1: sortBy = "price"; sortOrder = "asc"; break;
                        case 2: sortBy = "price"; sortOrder = "desc"; break;
                        default: sortBy = null; sortOrder = null; break;
                    }
                    fetchBooks(currentQuery, true);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performSearch(String query) {
        // (Hàm này giữ nguyên)
        currentQuery = query.trim();
        fetchBooks(currentQuery, true);
    }

    private void setupRecyclerView() {
        // (Hàm này giữ nguyên)
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
        // (Hàm này giữ nguyên)
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

        apiService.getAllBooks(currentPage, PAGE_SIZE, query, sortBy, sortOrder)
                .enqueue(new Callback<BookListResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BookListResponse> call, @NonNull Response<BookListResponse> response) {
                        if (getContext() == null) return;
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<BookItem> books = response.body().getData();
                            if (books.size() < PAGE_SIZE) {
                                totalPages = currentPage;
                            } else {
                                totalPages = currentPage + 1;
                            }

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

    // --- Implementation of AllBooksAdapter.OnBookClickListener ---

    @Override
    public void onBookClick(BookItem book) {
        // (Hàm này giữ nguyên - đã sửa ở lần trước)
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
        // (Hàm này giữ nguyên)
        if (getContext() == null) return;
        showBranchesMap(book);
    }

    private void showBranchesMap(BookItem book) {
        // (Hàm này giữ nguyên)
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