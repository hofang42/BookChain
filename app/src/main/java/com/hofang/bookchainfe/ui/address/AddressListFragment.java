package com.hofang.bookchainfe.ui.address;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Address;
import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.network.AddressApiService;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressListFragment extends Fragment implements AddressAdapter.OnAddressActionListener {

    private static final String TAG = "AddressListFragment";

    private RecyclerView recyclerAddresses;
    private AddressAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvError;
    private View layoutEmptyState;

    private AddressApiService addressApiService;
    private TokenManager tokenManager;

    public AddressListFragment() {
        // Required empty public constructor
    }

    public static AddressListFragment newInstance() {
        return new AddressListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerAddresses = view.findViewById(R.id.recycler_addresses);
        progressBar = view.findViewById(R.id.progress_bar);
        tvError = view.findViewById(R.id.tv_error);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        MaterialButton btnAddNewAddress = view.findViewById(R.id.btn_add_new_address);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);

        // Set up toolbar back button
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        // Initialize API service and token manager
        addressApiService = ApiConfig.getRetrofit().create(AddressApiService.class);
        tokenManager = new TokenManager(requireContext());

        // Set up RecyclerView
        adapter = new AddressAdapter(this);
        recyclerAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAddresses.setAdapter(adapter);

        // Set up Add New Address button
        if (btnAddNewAddress != null) {
            btnAddNewAddress.setOnClickListener(v -> {
                // Show bottom sheet to add new address
                if (getActivity() != null) {
                    AddAddressBottomSheet bottomSheet = AddAddressBottomSheet.newInstance(null);
                    bottomSheet.setOnAddressSavedListener(() -> loadAddresses());
                    bottomSheet.show(getParentFragmentManager(), "add_address_sheet");
                }
            });
        }

        // Load addresses
        loadAddresses();
    }

    private void loadAddresses() {
        showLoading(true);
        hideError();
        hideEmptyState();

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            showError("Not authenticated");
            showLoading(false);
            return;
        }

        String authHeader = "Bearer " + token;
        addressApiService.getAllAddresses(authHeader).enqueue(new Callback<ApiResponse<AddressApiService.AddressListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AddressApiService.AddressListResponse>> call,
                                   Response<ApiResponse<AddressApiService.AddressListResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AddressApiService.AddressListResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Address> addresses = apiResponse.getData().getAddresses();
                        if (addresses != null && !addresses.isEmpty()) {
                            adapter.setAddresses(addresses);
                            recyclerAddresses.setVisibility(View.VISIBLE);
                        } else {
                            showEmptyState();
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Failed to load addresses");
                    Log.e(TAG, "Response unsuccessful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AddressApiService.AddressListResponse>> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Failed to load addresses", t);
            }
        });
    }

    @Override
    public void onEditAddress(Address address) {
        // Show bottom sheet with pre-filled data
        AddAddressBottomSheet bottomSheet = AddAddressBottomSheet.newInstance(address);
        bottomSheet.setOnAddressSavedListener(() -> loadAddresses());
        bottomSheet.show(getParentFragmentManager(), "edit_address_sheet");
    }

    @Override
    public void onDeleteAddress(Address address) {
        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    performDeleteAddress(address);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDeleteAddress(Address address) {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;
        addressApiService.deleteAddress(authHeader, address.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(requireContext(), "Address deleted successfully", Toast.LENGTH_SHORT).show();
                        loadAddresses();
                    } else {
                        Toast.makeText(requireContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to delete address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to delete address", t);
            }
        });
    }

    @Override
    public void onSetDefaultAddress(Address address) {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate address ID
        if (address == null || address.getId() == null || address.getId().isEmpty()) {
            Toast.makeText(requireContext(), "Invalid address ID", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Address or address ID is null");
            return;
        }

        String authHeader = "Bearer " + token;
        addressApiService.setDefaultAddress(authHeader, address.getId()).enqueue(new Callback<ApiResponse<AddressApiService.AddressResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AddressApiService.AddressResponse>> call,
                                   Response<ApiResponse<AddressApiService.AddressResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AddressApiService.AddressResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(requireContext(), "Default address set successfully", Toast.LENGTH_SHORT).show();
                        loadAddresses();
                    } else {
                        Toast.makeText(requireContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to set default address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AddressApiService.AddressResponse>> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to set default address", t);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        recyclerAddresses.setVisibility(View.GONE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        layoutEmptyState.setVisibility(View.VISIBLE);
        recyclerAddresses.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        layoutEmptyState.setVisibility(View.GONE);
    }
}
