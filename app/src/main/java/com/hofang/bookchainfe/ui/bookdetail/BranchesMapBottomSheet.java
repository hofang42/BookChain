package com.hofang.bookchainfe.ui.bookdetail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Branch;
import com.hofang.bookchainfe.model.BranchesResponse;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.BookApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class BranchesMapBottomSheet extends BottomSheetDialogFragment 
        implements BranchAdapter.OnBranchClickListener, OnMapReadyCallback {

    private static final String TAG = "BranchesMapBottomSheet";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String ARG_BOOK_ID = "book_id";
    private static final String ARG_BOOK_TITLE = "book_title";
    private static final String ARG_BOOK_AUTHOR = "book_author";
    private static final String ARG_BOOK_COVER = "book_cover";

    private String bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCover;

    private ImageView ivBookCover;
    private TextView tvBookTitle;
    private TextView tvBookAuthor;
    private TextView tvBranchesCount;
    private RecyclerView rvBranches;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;
    private ImageButton btnClose;
    private Button btnViewList;
    private Button btnViewMap;
    private FrameLayout mapContainer;
    private MapView mapView;
    private GoogleMap googleMap;

    private BranchAdapter adapter;
    private BookApiService bookApiService;
    private FusedLocationProviderClient fusedLocationClient;

    private Double userLatitude;
    private Double userLongitude;
    private List<Branch> currentBranches = new ArrayList<>();
    private boolean isMapView = false;

    public static BranchesMapBottomSheet newInstance(String bookId, String bookTitle, 
                                                      String bookAuthor, String bookCover) {
        BranchesMapBottomSheet fragment = new BranchesMapBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_BOOK_ID, bookId);
        args.putString(ARG_BOOK_TITLE, bookTitle);
        args.putString(ARG_BOOK_AUTHOR, bookAuthor);
        args.putString(ARG_BOOK_COVER, bookCover);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getString(ARG_BOOK_ID);
            bookTitle = getArguments().getString(ARG_BOOK_TITLE);
            bookAuthor = getArguments().getString(ARG_BOOK_AUTHOR);
            bookCover = getArguments().getString(ARG_BOOK_COVER);
        }

        bookApiService = ApiConfig.getBookApiService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_branches_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupMapView(savedInstanceState);
        setupViewToggle();
        displayBookInfo();
        getUserLocationAndFetchBranches();

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void initViews(View view) {
        ivBookCover = view.findViewById(R.id.iv_book_cover);
        tvBookTitle = view.findViewById(R.id.tv_book_title);
        tvBookAuthor = view.findViewById(R.id.tv_book_author);
        tvBranchesCount = view.findViewById(R.id.tv_branches_count);
        rvBranches = view.findViewById(R.id.rv_branches);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);
        btnClose = view.findViewById(R.id.btn_close);
        btnViewList = view.findViewById(R.id.btn_view_list);
        btnViewMap = view.findViewById(R.id.btn_view_map);
        mapContainer = view.findViewById(R.id.map_container);
        mapView = view.findViewById(R.id.map_view);
    }

    private void setupRecyclerView() {
        adapter = new BranchAdapter(requireContext(), this);
        rvBranches.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBranches.setAdapter(adapter);
    }
    
    private void setupMapView(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }
    
    private void setupViewToggle() {
        // Default to list view
        showListView();
        
        btnViewList.setOnClickListener(v -> showListView());
        btnViewMap.setOnClickListener(v -> showMapView());
    }
    
    private void showListView() {
        isMapView = false;
        rvBranches.setVisibility(View.VISIBLE);
        mapContainer.setVisibility(View.GONE);
        
        // Update button styles
        btnViewList.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            getResources().getColor(android.R.color.white, null)));
        btnViewList.setTextColor(getResources().getColor(android.R.color.black, null));
        
        btnViewMap.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            getResources().getColor(android.R.color.darker_gray, null)));
        btnViewMap.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
    }
    
    private void showMapView() {
        isMapView = true;
        rvBranches.setVisibility(View.GONE);
        mapContainer.setVisibility(View.VISIBLE);
        
        // Update button styles
        btnViewMap.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            getResources().getColor(android.R.color.white, null)));
        btnViewMap.setTextColor(getResources().getColor(android.R.color.black, null));
        
        btnViewList.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            getResources().getColor(android.R.color.darker_gray, null)));
        btnViewList.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        
        // Display branches on map
        displayBranchesOnMap();
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        
        // Enable zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        
        // If we have branches, display them
        if (!currentBranches.isEmpty()) {
            displayBranchesOnMap();
        }
    }
    
    private void displayBranchesOnMap() {
        if (googleMap == null || currentBranches.isEmpty()) {
            return;
        }
        
        // Clear existing markers
        googleMap.clear();
        
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasBounds = false;
        
        // Add user location if available
        if (userLatitude != null && userLongitude != null) {
            LatLng userLocation = new LatLng(userLatitude, userLongitude);
            googleMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            boundsBuilder.include(userLocation);
            hasBounds = true;
        }
        
        // Add branch markers
        for (Branch branch : currentBranches) {
            if (branch.getLocation() != null && branch.getLocation().getCoordinates() != null) {
                double lat = branch.getLocation().getLatitude();
                double lng = branch.getLocation().getLongitude();
                LatLng position = new LatLng(lat, lng);
                
                String title = branch.getName();
                String snippet = branch.getQuantity() != null ? 
                    branch.getQuantity() + " books available" : "Available";
                if (branch.getDistance() != null) {
                    snippet += " • " + String.format("%.2f km away", branch.getDistance());
                }
                
                googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                
                boundsBuilder.include(position);
                hasBounds = true;
            }
        }
        
        // Adjust camera to show all markers
        if (hasBounds) {
            try {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 100; // padding in pixels
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Log.e(TAG, "Error adjusting camera: " + e.getMessage());
                // Fallback to first branch or user location
                if (!currentBranches.isEmpty() && currentBranches.get(0).getLocation() != null) {
                    LatLng firstBranch = new LatLng(
                        currentBranches.get(0).getLocation().getLatitude(),
                        currentBranches.get(0).getLocation().getLongitude()
                    );
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstBranch, 12f));
                } else if (userLatitude != null && userLongitude != null) {
                    LatLng userLocation = new LatLng(userLatitude, userLongitude);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f));
                }
            }
        }
    }

    private void displayBookInfo() {
        tvBookTitle.setText(bookTitle);
        tvBookAuthor.setText(bookAuthor);

        if (bookCover != null && !bookCover.isEmpty()) {
            Glide.with(this)
                    .load(bookCover)
                    .placeholder(R.drawable.classic_book)
                    .error(R.drawable.classic_book)
                    .into(ivBookCover);
        } else {
            ivBookCover.setImageResource(R.drawable.classic_book);
        }
    }

    private void getUserLocationAndFetchBranches() {
        // Check if we have location permission
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            // Request permission
            Log.d(TAG, "Requesting location permission...");
            requestPermissions(
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        // Permission granted, get location
        getLastKnownLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d(TAG, "Location permission granted");
                getLastKnownLocation();
            } else {
                // Permission denied
                Log.d(TAG, "Location permission denied, fetching branches without location");
                Toast.makeText(requireContext(), 
                    "Location permission denied. Showing all branches without distance sorting.", 
                    Toast.LENGTH_LONG).show();
                fetchBranches(null, null);
            }
        }
    }

    private void getLastKnownLocation() {
        // Double check permission before accessing location
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission check failed, fetching without location");
            fetchBranches(null, null);
            return;
        }

        // Get last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        Log.d(TAG, "User location: " + userLatitude + ", " + userLongitude);
                        fetchBranches(userLatitude, userLongitude);
                    } else {
                        Log.d(TAG, "Location is null, fetching branches without location");
                        Toast.makeText(requireContext(), 
                            "Unable to get current location. Showing all branches.", 
                            Toast.LENGTH_SHORT).show();
                        fetchBranches(null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location: " + e.getMessage());
                    Toast.makeText(requireContext(), 
                        "Failed to get location. Showing all branches.", 
                        Toast.LENGTH_SHORT).show();
                    fetchBranches(null, null);
                });
    }

    private void fetchBranches(Double lat, Double lng) {
        if (bookId == null || bookId.isEmpty()) {
            Toast.makeText(requireContext(), "Book ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        Call<BranchesResponse> call = bookApiService.getBranchesWithBook(bookId, lat, lng, 20);
        call.enqueue(new Callback<BranchesResponse>() {
            @Override
            public void onResponse(@NonNull Call<BranchesResponse> call, 
                                 @NonNull Response<BranchesResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BranchesResponse branchesResponse = response.body();
                    
                    if (branchesResponse.getBranches() != null && 
                        !branchesResponse.getBranches().isEmpty()) {
                        // Store branches
                        currentBranches = branchesResponse.getBranches();
                        
                        // Show branches in list
                        adapter.setBranches(currentBranches);
                        
                        // Update count message based on location availability
                        String countMessage;
                        if (lat != null && lng != null) {
                            countMessage = "Found " + branchesResponse.getTotalBranches() + 
                                         " branch(es) nearby (sorted by distance)";
                        } else {
                            countMessage = "Found " + branchesResponse.getTotalBranches() + 
                                         " branch(es) with this book";
                        }
                        tvBranchesCount.setText(countMessage);
                        showEmptyState(false);
                        
                        // If map is ready and we're in map view, display branches
                        if (isMapView && googleMap != null) {
                            displayBranchesOnMap();
                        }
                    } else {
                        // No branches found
                        currentBranches.clear();
                        showEmptyState(true);
                        tvBranchesCount.setText("No branches found with this book");
                    }
                } else {
                    Log.e(TAG, "Failed to fetch branches: " + response.code());
                    showEmptyState(true);
                    Toast.makeText(requireContext(), 
                        "Failed to load branches", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BranchesResponse> call, @NonNull Throwable t) {
                showLoading(false);
                showEmptyState(true);
                Log.e(TAG, "Error fetching branches: " + t.getMessage());
                Toast.makeText(requireContext(), 
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvBranches.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        layoutEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        rvBranches.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onViewOnMap(Branch branch) {
        if (branch.getLocation() != null && branch.getLocation().getCoordinates() != null) {
            double lat = branch.getLocation().getLatitude();
            double lng = branch.getLocation().getLongitude();
            
            // Open Google Maps with marker
            String uri = String.format("geo:%f,%f?q=%f,%f(%s)", 
                lat, lng, lat, lng, Uri.encode(branch.getName()));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Fallback to browser
                String browserUri = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f",
                    lat, lng);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
                startActivity(browserIntent);
            }
        } else {
            Toast.makeText(requireContext(), 
                "Location information not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCallBranch(Branch branch) {
        if (branch.getPhone() != null && !branch.getPhone().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + branch.getPhone()));
            startActivity(intent);
        }
    }
    
    // MapView lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}
