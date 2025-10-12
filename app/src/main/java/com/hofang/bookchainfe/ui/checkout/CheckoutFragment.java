package com.hofang.bookchainfe.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.ui.address.AddAddressBottomSheet;

public class CheckoutFragment extends Fragment {

    public CheckoutFragment() {
        // Required empty public constructor
    }

    public static CheckoutFragment newInstance() {
        return new CheckoutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup back button
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Setup Change address button
        MaterialButton btnChangeAddress = view.findViewById(R.id.btn_change_address);
        if (btnChangeAddress != null) {
            btnChangeAddress.setOnClickListener(v -> {
                // Show bottom sheet to change address
                AddAddressBottomSheet bottomSheet = new AddAddressBottomSheet();
                bottomSheet.show(getParentFragmentManager(), "add_address_sheet");
            });
        }

        // Setup Add New Delivery Address button
        MaterialButton btnAddAddress = view.findViewById(R.id.btn_add_new_delivery_address);
        if (btnAddAddress != null) {
            btnAddAddress.setOnClickListener(v -> {
                // Show bottom sheet to add new address
                AddAddressBottomSheet bottomSheet = new AddAddressBottomSheet();
                bottomSheet.show(getParentFragmentManager(), "add_address_sheet");
            });
        }

        // Setup Pay button
        MaterialButton btnPay = view.findViewById(R.id.btn_pay);
        if (btnPay != null) {
            btnPay.setOnClickListener(v -> {
                // Navigate to Payment Success
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_checkout_to_payment_success);
            });
        }
    }
}
