package com.hofang.bookchainfe.ui.address;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addresses = new ArrayList<>();
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEditAddress(Address address);
        void onDeleteAddress(Address address);
        void onSetDefaultAddress(Address address);
    }

    public AddressAdapter(OnAddressActionListener listener) {
        this.listener = listener;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses != null ? addresses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRecipientName;
        private TextView tvPhone;
        private TextView tvFullAddress;
        private TextView tvPostalCode;
        private Chip chipDefault;
        private MaterialButton btnSetDefault;
        private MaterialButton btnEdit;
        private MaterialButton btnDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipientName = itemView.findViewById(R.id.tv_recipient_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvFullAddress = itemView.findViewById(R.id.tv_full_address);
            tvPostalCode = itemView.findViewById(R.id.tv_postal_code);
            chipDefault = itemView.findViewById(R.id.chip_default);
            btnSetDefault = itemView.findViewById(R.id.btn_set_default);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Address address) {
            // Set recipient name
            tvRecipientName.setText(address.getRecipientName());

            // Set phone
            tvPhone.setText("Phone: " + address.getPhoneNumber());

            // Set full address
            StringBuilder fullAddress = new StringBuilder();
            if (address.getStreet() != null && !address.getStreet().isEmpty()) {
                fullAddress.append(address.getStreet());
            }
            if (address.getWard() != null && !address.getWard().isEmpty()) {
                if (fullAddress.length() > 0) fullAddress.append(", ");
                fullAddress.append(address.getWard());
            }
            if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
                if (fullAddress.length() > 0) fullAddress.append(", ");
                fullAddress.append(address.getDistrict());
            }
            if (address.getCity() != null && !address.getCity().isEmpty()) {
                if (fullAddress.length() > 0) fullAddress.append(", ");
                fullAddress.append(address.getCity());
            }
            tvFullAddress.setText(fullAddress.toString());

            // Set postal code
            if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) {
                tvPostalCode.setVisibility(View.VISIBLE);
                tvPostalCode.setText("Postal Code: " + address.getPostalCode());
            } else {
                tvPostalCode.setVisibility(View.GONE);
            }

            // Show/hide default badge
            if (address.isDefault()) {
                chipDefault.setVisibility(View.VISIBLE);
                btnSetDefault.setVisibility(View.GONE);
            } else {
                chipDefault.setVisibility(View.GONE);
                btnSetDefault.setVisibility(View.VISIBLE);
            }

            // Set click listeners
            btnSetDefault.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetDefaultAddress(address);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditAddress(address);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteAddress(address);
                }
            });
        }
    }
}
