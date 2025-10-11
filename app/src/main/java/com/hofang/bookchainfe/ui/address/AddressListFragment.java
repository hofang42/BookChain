package com.hofang.bookchainfe.ui.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.hofang.bookchainfe.R;

public class AddressListFragment extends Fragment {

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

        // Handle window insets for bottom navigation
        View bottomNav = view.findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Apply bottom padding to avoid system navigation bar overlap
                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    systemBars.bottom
                );

                return insets;
            });
        }

        // Set up Add New Address button
        MaterialButton btnAddNewAddress = view.findViewById(R.id.btn_add_new_address);
        if (btnAddNewAddress != null) {
            btnAddNewAddress.setOnClickListener(v -> {
                // Show bottom sheet to add new address
                if (getActivity() != null) {
                    AddAddressBottomSheet bottomSheet = new AddAddressBottomSheet();
                    bottomSheet.show(getParentFragmentManager(), "add_address_sheet");
                }
            });
        }

        // Set up edit and delete buttons for addresses
        // In a real app, you'd have a RecyclerView with multiple addresses
        MaterialButton btnEdit = view.findViewById(R.id.btn_edit_address);
        MaterialButton btnDelete = view.findViewById(R.id.btn_delete_address);

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                // Handle edit address
                // Show bottom sheet with pre-filled data
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                // Handle delete address
                // Show confirmation dialog
            });
        }
    }
}
