package com.hofang.bookchainfe.ui.address;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Address;
import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.network.AddressApiService;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAddressBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "AddAddressBottomSheet";
    private static final String ARG_ADDRESS = "address";

    private Address existingAddress;
    private OnAddressSavedListener listener;

    private TextInputEditText etRecipientName;
    private TextInputEditText etPhone;
    private TextInputEditText etStreet;
    private TextInputEditText etWard;
    private TextInputEditText etDistrict;
    private TextInputEditText etCity;
    private TextInputEditText etPostalCode;
    private CheckBox checkDefault;
    private View progress;

    private AddressApiService addressApiService;
    private TokenManager tokenManager;

    public interface OnAddressSavedListener {
        void onAddressSaved();
    }

    public static AddAddressBottomSheet newInstance(Address address) {
        AddAddressBottomSheet fragment = new AddAddressBottomSheet();
        Bundle args = new Bundle();
        if (address != null) {
            args.putSerializable(ARG_ADDRESS, address);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnAddressSavedListener(OnAddressSavedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingAddress = (Address) getArguments().getSerializable(ARG_ADDRESS);
        }
        addressApiService = ApiConfig.getRetrofit().create(AddressApiService.class);
        tokenManager = new TokenManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_address_bottom_sheet, container, false);

        // Initialize views
        TextView tvTitle = view.findViewById(R.id.tv_title);
        etRecipientName = view.findViewById(R.id.recipient_name_input);
        etPhone = view.findViewById(R.id.phone_input);
        etStreet = view.findViewById(R.id.street_input);
        etWard = view.findViewById(R.id.ward_input);
        etDistrict = view.findViewById(R.id.district_input);
        etCity = view.findViewById(R.id.city_input);
        etPostalCode = view.findViewById(R.id.postal_input);
        checkDefault = view.findViewById(R.id.check_default_address);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        progress = view.findViewById(R.id.progress_indicator);

        // Set title based on mode
        if (existingAddress != null) {
            tvTitle.setText("Edit Address");
            btnSave.setText("UPDATE");
            populateFields();
        } else {
            tvTitle.setText("Add Address");
            btnSave.setText("SAVE");
        }

        btnSave.setOnClickListener(v -> saveAddress());
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private void populateFields() {
        if (existingAddress == null) return;

        if (etRecipientName != null && existingAddress.getRecipientName() != null) {
            etRecipientName.setText(existingAddress.getRecipientName());
        }
        if (etPhone != null && existingAddress.getPhoneNumber() != null) {
            etPhone.setText(existingAddress.getPhoneNumber());
        }
        if (etStreet != null && existingAddress.getStreet() != null) {
            etStreet.setText(existingAddress.getStreet());
        }
        if (etWard != null && existingAddress.getWard() != null) {
            etWard.setText(existingAddress.getWard());
        }
        if (etDistrict != null && existingAddress.getDistrict() != null) {
            etDistrict.setText(existingAddress.getDistrict());
        }
        if (etCity != null && existingAddress.getCity() != null) {
            etCity.setText(existingAddress.getCity());
        }
        if (etPostalCode != null && existingAddress.getPostalCode() != null) {
            etPostalCode.setText(existingAddress.getPostalCode());
        }
        if (checkDefault != null) {
            checkDefault.setChecked(existingAddress.isDefault());
        }
    }

    private void saveAddress() {
        // Validate inputs
        String recipientName = etRecipientName.getText() != null ? etRecipientName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String street = etStreet.getText() != null ? etStreet.getText().toString().trim() : "";
        String ward = etWard.getText() != null ? etWard.getText().toString().trim() : "";
        String district = etDistrict.getText() != null ? etDistrict.getText().toString().trim() : "";
        String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";
        String postalCode = etPostalCode.getText() != null ? etPostalCode.getText().toString().trim() : "";
        boolean isDefault = checkDefault.isChecked();

        if (recipientName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter recipient name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (street.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter street address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (district.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter district", Toast.LENGTH_SHORT).show();
            return;
        }
        if (city.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter city", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create address object
        Address address = new Address();
        if (existingAddress != null) {
            address.setId(existingAddress.getId());
            address.setUserId(existingAddress.getUserId());
        }
        address.setRecipientName(recipientName);
        address.setPhoneNumber(phone);
        address.setStreet(street);
        address.setWard(ward.isEmpty() ? null : ward);
        address.setDistrict(district);
        address.setCity(city);
        address.setPostalCode(postalCode.isEmpty() ? null : postalCode);
        address.setDefault(isDefault);

        // Show progress
        if (progress != null) progress.setVisibility(View.VISIBLE);

        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show();
            if (progress != null) progress.setVisibility(View.GONE);
            return;
        }

        String authHeader = "Bearer " + token;

        if (existingAddress != null) {
            // Update existing address
            addressApiService.updateAddress(authHeader, existingAddress.getId(), address)
                    .enqueue(new AddressCallback("Address updated successfully"));
        } else {
            // Create new address
            addressApiService.createAddress(authHeader, address)
                    .enqueue(new AddressCallback("Address created successfully"));
        }
    }

    private class AddressCallback implements Callback<ApiResponse<AddressApiService.AddressResponse>> {
        private final String successMessage;

        AddressCallback(String successMessage) {
            this.successMessage = successMessage;
        }

        @Override
        public void onResponse(Call<ApiResponse<AddressApiService.AddressResponse>> call,
                               Response<ApiResponse<AddressApiService.AddressResponse>> response) {
            if (progress != null) progress.setVisibility(View.GONE);

            if (response.isSuccessful() && response.body() != null) {
                ApiResponse<AddressApiService.AddressResponse> apiResponse = response.body();
                if (apiResponse.isSuccess()) {
                    Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onAddressSaved();
                    }
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Failed to save address", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Response unsuccessful: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<AddressApiService.AddressResponse>> call, Throwable t) {
            if (progress != null) progress.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to save address", t);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make dialog window background transparent so our drawable with rounded top is visible
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        // Set peek height to about half the screen so underlying content remains visible (collapsed state)
        try {
            android.app.Dialog d = getDialog();
            if (d != null) {
                View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    com.google.android.material.bottomsheet.BottomSheetBehavior behavior =
                            com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                    int height = getResources().getDisplayMetrics().heightPixels;
                    behavior.setPeekHeight(height * 3 / 4); // Increased to 75% for more fields
                    behavior.setSkipCollapsed(false);
                    behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
