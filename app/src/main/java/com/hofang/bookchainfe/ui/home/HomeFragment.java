package com.hofang.bookchainfe.ui.home;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputLayout;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.ui.message.MessageActivity;
// Import các lớp Network
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.BookApiService;
import com.hofang.bookchainfe.model.BookListResponse;
import com.hofang.bookchainfe.ui.book.AllBooksFragment;

import java.util.ArrayList;
import java.util.List;

// Import Retrofit
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment"; // Tag để Log

    // Khởi tạo các list rỗng, dữ liệu sẽ được nạp từ API
    private ArrayList<BookItem> bestDealsList = new ArrayList<>();
    private ArrayList<BookItem> topBooksList = new ArrayList<>();
    private ArrayList<BookItem> upcomingBooksList = new ArrayList<>(); // Giữ lại hard-code
    private ArrayList<BookItem> latestBooksList = new ArrayList<>();

    private BookAdapter bestDealsAdapter;
    private BookAdapter topBooksAdapter;
    private BookAdapter upcomingBooksAdapter;
    private BookAdapter latestBooksAdapter;

    private ViewPager2 viewPagerBestDeals;
    private TabLayout tabIndicator;
    private RecyclerView rvTopBooks, rvUpcomingBooks, rvLatestBooks;
    private View headerTopBooks, headerUpcomingBooks, headerLatestBooks;
    private ChipGroup chipGroupTopBooks;
    private View btnHomeMessage;

    // Service để gọi API
    private BookApiService apiService;

    // --- Biến cho chức năng Search ---
    private TextView tvHeaderTitle;
    private ImageButton btnHomeSearch;
    private TextInputLayout tilSearchBar;
    private EditText etSearchBar;
    private RecyclerView rvSearchSuggestions;
    private SearchSuggestionAdapter searchAdapter;

    // Biến cho Debounce (tránh gọi API liên tục)
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        // Khởi tạo service API từ ApiConfig
        apiService = ApiConfig.getBookApiService();

        // 1. Setup Adapters (với list rỗng)
        setupAdapters();

        // 2. Setup Search
        setupSearch(); // Khởi tạo adapter gợi ý
        setupBackPressHandler(); // Xử lý khi bấm nút back

        // 3. Gọi API để lấy dữ liệu và nạp vào adapter
        fetchApiData();

        // 4. Setup các thành phần khác
        setupHeaders();
        setupListeners();
    }

    private void initViews(View view) {
        viewPagerBestDeals = view.findViewById(R.id.viewpager_best_deals);
        tabIndicator = view.findViewById(R.id.tab_indicator);
        rvTopBooks = view.findViewById(R.id.rv_top_books);
        rvUpcomingBooks = view.findViewById(R.id.rv_upcoming_books);
        rvLatestBooks = view.findViewById(R.id.rv_latest_books);
        headerTopBooks = view.findViewById(R.id.header_top_books);
        headerUpcomingBooks = view.findViewById(R.id.header_upcoming_books);
        headerLatestBooks = view.findViewById(R.id.header_latest_books);
        chipGroupTopBooks = view.findViewById(R.id.chip_group_top_books);
        btnHomeMessage = view.findViewById(R.id.btn_home_message);

        // --- Thêm các view cho search ---
        tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        btnHomeSearch = view.findViewById(R.id.btn_home_search);
        tilSearchBar = view.findViewById(R.id.til_search_bar);
        etSearchBar = view.findViewById(R.id.et_search_bar);
        rvSearchSuggestions = view.findViewById(R.id.rv_search_suggestions);
    }

    private void setupAdapters() {
        // Best Deals Adapter
        bestDealsAdapter = new BookAdapter(getContext(), bestDealsList, BookAdapter.VIEW_TYPE_DEAL);
        viewPagerBestDeals.setAdapter(bestDealsAdapter);
        new TabLayoutMediator(tabIndicator, viewPagerBestDeals, (tab, position) -> {}).attach();

        // Top Books Adapter
        topBooksAdapter = new BookAdapter(getContext(), topBooksList, BookAdapter.VIEW_TYPE_CARD);
        rvTopBooks.setAdapter(topBooksAdapter);

        // Upcoming Books Adapter
        upcomingBooksAdapter = new BookAdapter(getContext(), upcomingBooksList, BookAdapter.VIEW_TYPE_CARD);
        rvUpcomingBooks.setAdapter(upcomingBooksAdapter);

        // Latest Books Adapter
        latestBooksAdapter = new BookAdapter(getContext(), latestBooksList, BookAdapter.VIEW_TYPE_CARD);
        rvLatestBooks.setAdapter(latestBooksAdapter);
    }

    /**
     * Hàm tổng để gọi tất cả API cần thiết cho HomeFragment
     */
    private void fetchApiData() {
        fetchBestDeals();
        fetchTopBooks();
        fetchLatestBooks();

        // API cho "Upcoming Books" chưa có, ta giữ lại data hard-code
        loadUpcomingBooksHardcode();
    }

    // --- CÁC HÀM GỌI API MỚI SỬ DỤNG RETROFIT ---

    private void fetchBestDeals() {
        apiService.getBestDeals().enqueue(new Callback<List<BookItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookItem>> call, @NonNull Response<List<BookItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bestDealsAdapter.updateData(response.body());
                } else {
                    Log.e(TAG, "fetchBestDeals error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookItem>> call, @NonNull Throwable t) {
                Log.e(TAG, "fetchBestDeals failure: " + t.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load best deals", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchTopBooks() {
        apiService.getTopBooks().enqueue(new Callback<List<BookItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookItem>> call, @NonNull Response<List<BookItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    topBooksAdapter.updateData(response.body());
                } else {
                    Log.e(TAG, "fetchTopBooks error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookItem>> call, @NonNull Throwable t) {
                Log.e(TAG, "fetchTopBooks failure: " + t.getMessage());
            }
        });
    }

    private void fetchLatestBooks() {
        apiService.getLatestBooks().enqueue(new Callback<List<BookItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookItem>> call, @NonNull Response<List<BookItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    latestBooksAdapter.updateData(response.body());
                } else {
                    Log.e(TAG, "fetchLatestBooks error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookItem>> call, @NonNull Throwable t) {
                Log.e(TAG, "fetchLatestBooks failure: " + t.getMessage());
            }
        });
    }

    /**
     * Giữ lại data hard-code cho Upcoming vì API chưa có
     */
    private void loadUpcomingBooksHardcode() {
        upcomingBooksList.add(new BookItem(R.drawable.queen_of_myth_and_monsters, "Fantasy", "Queen of Myth and Monsters", "Scarlett St. Clair", 250000));
        upcomingBooksList.add(new BookItem(R.drawable.zodiac_academy, "Fantasy", "Zodiac Academy", "Susanne Valenti", 300000));
        upcomingBooksList.add(new BookItem(R.drawable.queen_of_myth_and_monsters, "Fantasy", "Queen of Myth and Monsters", "Scarlett St. Clair", 250000));
        upcomingBooksList.add(new BookItem(R.drawable.zodiac_academy, "Fantasy", "Zodiac Academy", "Susanne Valenti", 300000));
        upcomingBooksAdapter.notifyDataSetChanged(); // Cập nhật adapter
    }

    // --- HẾT CÁC HÀM GỌI API ---

    private void setupHeaders() {
        ((TextView) headerTopBooks.findViewById(R.id.tv_section_title)).setText("Top Books");
        ((TextView) headerUpcomingBooks.findViewById(R.id.tv_section_title)).setText("Upcoming Books");
        ((TextView) headerLatestBooks.findViewById(R.id.tv_section_title)).setText("Latest Books");

        View.OnClickListener seeMoreListener = v -> {
            String title = ((TextView) ((View) v.getParent()).findViewById(R.id.tv_section_title)).getText().toString();
            Toast.makeText(getContext(), "See more for " + title, Toast.LENGTH_SHORT).show();
        };

        headerTopBooks.findViewById(R.id.tv_section_see_more).setOnClickListener(seeMoreListener);
        headerUpcomingBooks.findViewById(R.id.tv_section_see_more).setOnClickListener(seeMoreListener);
        headerLatestBooks.findViewById(R.id.tv_section_see_more).setOnClickListener(seeMoreListener);
    }

    // --- CÁC HÀM MỚI CHO CHỨC NĂNG SEARCH ---

    /**
     * Khởi tạo RecyclerView và Adapter cho gợi ý tìm kiếm
     */
    private void setupSearch() {
        searchAdapter = new SearchSuggestionAdapter(new SearchSuggestionAdapter.OnSuggestionClickListener() {
            @Override
            public void onBookClick(BookItem book) {
                // TODO: Chuyển sang màn hình Book Detail
                // Ví dụ: Intent intent = new Intent(getContext(), BookDetailActivity.class);
                // intent.putExtra("BOOK_ID", book.getId()); // Giả sử book có getId()
                // startActivity(intent);
                Toast.makeText(getContext(), "Bấm vào: " + book.getTitle(), Toast.LENGTH_SHORT).show();

                // Ẩn search sau khi chọn
                toggleSearch(false);
            }

            @Override
            public void onSeeAllClick(String query) {
                String currentQuery = etSearchBar.getText().toString().trim();

                Bundle args = new Bundle();
                args.putString("searchQuery", currentQuery);

                // Dùng NavController để điều hướng
                NavController navController = NavHostFragment.findNavController(HomeFragment.this);

                // DÙNG ID MỚI VÀ CHÍNH XÁC TỪ nav_graph.xml
                navController.navigate(R.id.action_nav_home_to_allBooksFragment, args);

                toggleSearch(false);
            }
        });
        rvSearchSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchSuggestions.setAdapter(searchAdapter);
    }

    /**
     * Gọi API tìm kiếm
     * @param query Từ khóa tìm kiếm
     */
    private void fetchSearchSuggestions(String query) {
        // SỬA LẠI KIỂU TRONG CALLBACK
        apiService.searchBooks(query).enqueue(new Callback<BookListResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookListResponse> call, @NonNull Response<BookListResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<BookItem> books = response.body().getData();
                    if (books.isEmpty()) {
                        rvSearchSuggestions.setVisibility(View.GONE);
                    } else {
                        searchAdapter.updateData(books);
                        rvSearchSuggestions.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "fetchSearchSuggestions error: " + response.code() + " " + response.message());
                    rvSearchSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookListResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "fetchSearchSuggestions failure: " + t.getMessage());
                rvSearchSuggestions.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Ẩn/Hiện thanh tìm kiếm và bàn phím
     * @param show true để hiện, false để ẩn
     */
    private void toggleSearch(boolean show) {
        if (show) {
            tvHeaderTitle.setVisibility(View.GONE);
            tilSearchBar.setVisibility(View.VISIBLE);
            etSearchBar.requestFocus();
            showKeyboard(etSearchBar);
        } else {
            tvHeaderTitle.setVisibility(View.VISIBLE);
            tilSearchBar.setVisibility(View.GONE);
            etSearchBar.setText(""); // Xóa text
            rvSearchSuggestions.setVisibility(View.GONE); // Ẩn gợi ý
            if(searchAdapter != null) { // Tránh NullPointerException nếu view bị destroy
                searchAdapter.clearData();
            }
            hideKeyboard(etSearchBar);
        }
    }

    /**
     * Xử lý khi người dùng bấm nút back của hệ thống
     */
    private void setupBackPressHandler() {
        if (getActivity() == null) return;
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Nếu thanh tìm kiếm đang hiện, bấm back sẽ tắt nó đi
                if (tilSearchBar.getVisibility() == View.VISIBLE) {
                    toggleSearch(false);
                } else {
                    // Nếu không thì thoát fragment/activity như bình thường
                    if (isEnabled()) {
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                }
            }
        });
    }

    // --- CÁC HÀM HỖ TRỢ BÀN PHÍM ---
    private void showKeyboard(View view) {
        if (view.requestFocus() && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void hideKeyboard(View view) {
        if (getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    // --- HÀM NÀY ĐƯỢC CẬP NHẬT ---
    private void setupListeners() {
        // (Giữ nguyên code cũ của chipGroup)
        chipGroupTopBooks.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
            }
            int checkedId = checkedIds.get(0);

            String filter = "This Week";
            if (checkedId == R.id.chip_this_month) {
                filter = "This Month";
            } else if (checkedId == R.id.chip_this_year) {
                filter = "This Year";
            }
            Toast.makeText(getContext(), "Filter Top Books by: " + filter, Toast.LENGTH_SHORT).show();
            // TODO: (Tương lai) Gọi lại fetchTopBooks với filter
        });

        // --- THÊM LISTENER CHO SEARCH ---
        btnHomeSearch.setOnClickListener(v -> toggleSearch(true));

        etSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hủy bỏ runnable cũ (nếu có)
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                // Tạo runnable mới
                searchRunnable = () -> {
                    if (query.length() > 1) { // Chỉ tìm kiếm nếu query đủ dài
                        fetchSearchSuggestions(query);
                    } else {
                        rvSearchSuggestions.setVisibility(View.GONE);
                        if(searchAdapter != null) {
                            searchAdapter.clearData();
                        }
                    }
                };

                // Chạy runnable sau 300ms (debounce)
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
        etSearchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // 1. Lấy text
                String query = etSearchBar.getText().toString().trim();

                // 2. Hủy các tìm kiếm tự động đang chờ
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // 3. Thực thi tìm kiếm ngay lập tức
                if (query.length() > 1) {
                    fetchSearchSuggestions(query);
                } else {
                    rvSearchSuggestions.setVisibility(View.GONE);
                    if(searchAdapter != null) {
                        searchAdapter.clearData();
                    }
                }

                // 4. Ẩn bàn phím
                hideKeyboard(etSearchBar);
                return true; // Báo rằng ta đã xử lý sự kiện
            }
            return false;
        });

        btnHomeMessage.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MessageActivity.class);
            startActivity(intent);
        });
    }


}