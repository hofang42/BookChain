package com.hofang.bookchainfe.ui.payment; // Hoặc package bạn muốn

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Order;
import com.hofang.bookchainfe.model.OrderHistoryResponse;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.PaymentApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryFragment extends Fragment {

    private static final String TAG = "OrderHistoryFragment";

    private RecyclerView rvOrders;
    private OrderHistoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private PaymentApiService paymentApiService;
    private List<Order> orderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orderhistory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rv_orders);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);

        paymentApiService = ApiConfig.getPaymentApiService();

        setupRecyclerView();
        setupToolbar(view);

        loadOrders();
    }

    private void setupToolbar(View view) {
        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(orderList);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        showLoading(true);
        paymentApiService.getUserOrders().enqueue(new Callback<OrderHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderHistoryResponse> call, @NonNull Response<OrderHistoryResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body().getOrders();
                    if (orders != null && !orders.isEmpty()) {
                        orderList.clear();
                        orderList.addAll(orders);
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.GONE);
                        rvOrders.setVisibility(View.VISIBLE);
                    } else {
                        showEmpty(true);
                    }
                } else {
                    Log.e(TAG, "Failed to load orders: " + response.code());
                    Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
                    showEmpty(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderHistoryResponse> call, @NonNull Throwable t) {
                showLoading(false);
                showEmpty(true);
                Log.e(TAG, "Error loading orders", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            rvOrders.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmpty(boolean isEmpty) {
        rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}