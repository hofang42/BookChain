package com.hofang.bookchainfe.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.hofang.bookchainfe.R;

public class CartFragment extends Fragment {

    public CartFragment() {
        // Required empty public constructor
    }

    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup button to navigate to Checkout
        MaterialButton btnGoToCheckout = view.findViewById(R.id.btn_go_to_checkout);
        if (btnGoToCheckout != null) {
            btnGoToCheckout.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_cart_to_checkout);
            });
        }
    }
}
