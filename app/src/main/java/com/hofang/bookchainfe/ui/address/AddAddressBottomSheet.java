package com.hofang.bookchainfe.ui.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.hofang.bookchainfe.R;

public class AddAddressBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this bottom sheet
        View view = inflater.inflate(R.layout.fragment_add_address_bottom_sheet, container, false);

        TextInputEditText street = view.findViewById(R.id.street_input);
        TextInputEditText city = view.findViewById(R.id.city_input);
        TextInputEditText postal = view.findViewById(R.id.postal_input);
        CheckBox saveCheck = view.findViewById(R.id.check_save_address);

        Button add = view.findViewById(R.id.btn_add);
        Button cancel = view.findViewById(R.id.btn_cancel);
        final View progress = view.findViewById(R.id.progress_indicator);

        add.setOnClickListener(v -> {
            // Show local spinner next to buttons so the loading indicator appears in the expected place
            if (progress != null) progress.setVisibility(View.VISIBLE);

            // In a real app, you would save the address to database/API here
            // For now, just simulate saving and dismiss
            String streetText = street != null && street.getText() != null ? street.getText().toString() : "";
            String cityText = city != null && city.getText() != null ? city.getText().toString() : "";
            String postalText = postal != null && postal.getText() != null ? postal.getText().toString() : "";
            boolean shouldSave = saveCheck != null && saveCheck.isChecked();

            // TODO: Save address to database/API
            // addressRepository.saveAddress(new Address(streetText, cityText, postalText, shouldSave));

            // Hide spinner and dismiss
            if (progress != null) progress.setVisibility(View.GONE);
            dismiss();
        });

        cancel.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make dialog window background transparent so our drawable with rounded top is visible
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            // keep the window background transparent so the activity behind remains visible;
            // the sheet's own root view has @drawable/bg_bottom_sheet as its background
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        // Set peek height to about half the screen so underlying content remains visible (collapsed state)
        try {
            android.app.Dialog d = getDialog();
            if (d != null) {
                View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    com.google.android.material.bottomsheet.BottomSheetBehavior behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                    // calculate half of window height
                    int height = getResources().getDisplayMetrics().heightPixels;
                    behavior.setPeekHeight(height / 2);
                    behavior.setSkipCollapsed(false);
                    behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        } catch (Exception ignored) {
        }
    }

}
