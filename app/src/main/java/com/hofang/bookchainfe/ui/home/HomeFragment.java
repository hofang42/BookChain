package com.hofang.bookchainfe.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hofang.bookchainfe.R;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private ArrayList<BookItem> bestDealsList;
    private ArrayList<BookItem> topBooksList;
    private ArrayList<BookItem> upcomingBooksList;
    private ArrayList<BookItem> latestBooksList;

    private BookAdapter bestDealsAdapter;
    private BookAdapter topBooksAdapter;
    private BookAdapter upcomingBooksAdapter;
    private BookAdapter latestBooksAdapter;

    private ViewPager2 viewPagerBestDeals;
    private TabLayout tabIndicator;
    private RecyclerView rvTopBooks, rvUpcomingBooks, rvLatestBooks;
    private View headerTopBooks, headerUpcomingBooks, headerLatestBooks;
    private ChipGroup chipGroupTopBooks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadData();
        setupAdapters();
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
    }

    private void loadData() {
        // (Không thay đổi - vẫn dùng BookItem)

        // Best Deals
        bestDealsList = new ArrayList<>();
        bestDealsList.add(new BookItem(R.drawable.tuesday_mooney_talks_to_ghosts, "Novel", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "$33.00", "12% off"));
        bestDealsList.add(new BookItem(R.drawable.when_the_moon_hatched, "Fantasy", "When the Moon Hatched", "Sarah A. Parker", "$29.99", "10% off"));
        bestDealsList.add(new BookItem(R.drawable.tuesday_mooney_talks_to_ghosts, "Novel", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "$33.00", "12% off"));
        bestDealsList.add(new BookItem(R.drawable.when_the_moon_hatched, "Fantasy", "When the Moon Hatched", "Sarah A. Parker", "$29.99", "10% off"));
        bestDealsList.add(new BookItem(R.drawable.tuesday_mooney_talks_to_ghosts, "Novel", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "$33.00", "12% off"));
        bestDealsList.add(new BookItem(R.drawable.when_the_moon_hatched, "Fantasy", "When the Moon Hatched", "Sarah A. Parker", "$29.99", "10% off"));

        // Top Books
        topBooksList = new ArrayList<>();
        topBooksList.add(new BookItem(R.drawable.the_picture_of_dorian_gray, "Classics", "The Picture of Dorian Gray", "Oscar Wilde", "$25.00"));
        topBooksList.add(new BookItem(R.drawable.the_catcher_in_the_rye, "Classics", "The Catcher in the Rye", "J.D. Salinger", "$30.00"));
        topBooksList.add(new BookItem(R.drawable.the_picture_of_dorian_gray, "Classics", "The Picture of Dorian Gray", "Oscar Wilde", "$25.00"));
        topBooksList.add(new BookItem(R.drawable.the_catcher_in_the_rye, "Classics", "The Catcher in the Rye", "J.D. Salinger", "$30.00"));
        topBooksList.add(new BookItem(R.drawable.the_picture_of_dorian_gray, "Classics", "The Picture of Dorian Gray", "Oscar Wilde", "$25.00"));
        topBooksList.add(new BookItem(R.drawable.the_catcher_in_the_rye, "Classics", "The Catcher in the Rye", "J.D. Salinger", "$30.00"));

        // Upcoming Books
        upcomingBooksList = new ArrayList<>();
        upcomingBooksList.add(new BookItem(R.drawable.queen_of_myth_and_monsters, "Fantasy", "Queen of Myth and Monsters", "Scarlett St. Clair", "$25.00"));
        upcomingBooksList.add(new BookItem(R.drawable.zodiac_academy, "Fantasy", "Zodiac Academy", "Susanne Valenti", "$30.00"));
        upcomingBooksList.add(new BookItem(R.drawable.queen_of_myth_and_monsters, "Fantasy", "Queen of Myth and Monsters", "Scarlett St. Clair", "$25.00"));
        upcomingBooksList.add(new BookItem(R.drawable.zodiac_academy, "Fantasy", "Zodiac Academy", "Susanne Valenti", "$30.00"));
        upcomingBooksList.add(new BookItem(R.drawable.queen_of_myth_and_monsters, "Fantasy", "Queen of Myth and Monsters", "Scarlett St. Clair", "$25.00"));
        upcomingBooksList.add(new BookItem(R.drawable.zodiac_academy, "Fantasy", "Zodiac Academy", "Susanne Valenti", "$30.00"));

        // Latest Books
        latestBooksList = new ArrayList<>();
        latestBooksList.add(new BookItem(R.drawable.nine_liars, "Young adult", "Nine Liars", "Maureen Johnson", "$16.00"));
        latestBooksList.add(new BookItem(R.drawable.sorrow_and_starlight, "Fantasy", "Sorrow and Starlight", "Caroline Peckham", "$30.00"));
        latestBooksList.add(new BookItem(R.drawable.nine_liars, "Young adult", "Nine Liars", "Maureen Johnson", "$16.00"));
        latestBooksList.add(new BookItem(R.drawable.sorrow_and_starlight, "Fantasy", "Sorrow and Starlight", "Caroline Peckham", "$30.00"));
        latestBooksList.add(new BookItem(R.drawable.nine_liars, "Young adult", "Nine Liars", "Maureen Johnson", "$16.00"));
        latestBooksList.add(new BookItem(R.drawable.sorrow_and_starlight, "Fantasy", "Sorrow and Starlight", "Caroline Peckham", "$30.00"));
    }

    private void setupAdapters() {
        // Khởi tạo BookAdapter với đúng VIEW_TYPE

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

    private void setupHeaders() {
        // Gán tiêu đề và xử lý click "see more"
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

    private void setupListeners() {
        // Xử lý click cho ChipGroup
        chipGroupTopBooks.setOnCheckedChangeListener((group, checkedId) -> {
            String filter = "This Week";
            if (checkedId == R.id.chip_this_month) {
                filter = "This Month";
            } else if (checkedId == R.id.chip_this_year) {
                filter = "This Year";
            }

            // TODO: Gọi API để fetch/filter lại 'topBooksList'
            Toast.makeText(getContext(), "Filter Top Books by: " + filter, Toast.LENGTH_SHORT).show();
            // Ví dụ: viewModel.fetchTopBooks(filter);
        });
    }
}