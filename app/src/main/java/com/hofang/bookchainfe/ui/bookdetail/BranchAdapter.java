package com.hofang.bookchainfe.ui.bookdetail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Branch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {

    private Context context;
    private List<Branch> branches;
    private OnBranchClickListener listener;

    public interface OnBranchClickListener {
        void onViewOnMap(Branch branch);
        void onCallBranch(Branch branch);
    }

    public BranchAdapter(Context context, OnBranchClickListener listener) {
        this.context = context;
        this.branches = new ArrayList<>();
        this.listener = listener;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches != null ? branches : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_branch, parent, false);
        return new BranchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
        Branch branch = branches.get(position);
        holder.bind(branch);
    }

    @Override
    public int getItemCount() {
        return branches.size();
    }

    class BranchViewHolder extends RecyclerView.ViewHolder {
        TextView tvBranchName;
        TextView tvAddress;
        TextView tvDistance;
        TextView tvStock;
        TextView tvPhone;
        TextView tvOpeningHours;
        LinearLayout layoutPhone;
        MaterialButton btnViewOnMap;

        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBranchName = itemView.findViewById(R.id.tv_branch_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvOpeningHours = itemView.findViewById(R.id.tv_opening_hours);
            layoutPhone = itemView.findViewById(R.id.layout_phone);
            btnViewOnMap = itemView.findViewById(R.id.btn_view_on_map);
        }

        public void bind(Branch branch) {
            // Branch name
            tvBranchName.setText(branch.getName());

            // Address
            tvAddress.setText(branch.getFullAddress());

            // Distance
            if (branch.getDistance() != null && branch.getDistance() > 0) {
                tvDistance.setVisibility(View.VISIBLE);
                tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", branch.getDistance()));
            } else {
                tvDistance.setVisibility(View.GONE);
            }

            // Stock
            if (branch.getQuantity() != null && branch.getQuantity() > 0) {
                tvStock.setText(String.format(Locale.getDefault(), "%d in stock", branch.getQuantity()));
                tvStock.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStock.setText("Out of stock");
                tvStock.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            }

            // Phone
            if (branch.getPhone() != null && !branch.getPhone().isEmpty()) {
                layoutPhone.setVisibility(View.VISIBLE);
                tvPhone.setText("Call");
                layoutPhone.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCallBranch(branch);
                    }
                });
            } else {
                layoutPhone.setVisibility(View.GONE);
            }

            // Opening hours
            if (branch.getOpeningHours() != null && !branch.getOpeningHours().isEmpty()) {
                tvOpeningHours.setVisibility(View.VISIBLE);
                tvOpeningHours.setText("Open: " + branch.getOpeningHours());
            } else {
                tvOpeningHours.setVisibility(View.GONE);
            }

            // View on Map button
            btnViewOnMap.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOnMap(branch);
                }
            });
        }
    }
}
