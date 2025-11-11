package com.hofang.bookchainfe.ui.payment; // Hoặc package bạn muốn

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.Order;
import com.hofang.bookchainfe.model.OrderItem;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat inputFormat;
    private SimpleDateFormat outputFormat;

    public OrderHistoryAdapter(List<Order> orderList) {
        this.orderList = orderList;
        // Khởi tạo định dạng tiền VN
        Locale localeVN = new Locale("vi", "VN");
        this.currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

        // Khởi tạo định dạng ngày
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        this.outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        this.inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo tên layout của bạn là chính xác
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orhis_item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvOrderStatus, tvOrderDate, tvOrderItemsSummary, tvOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tv_order_code);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderItemsSummary = itemView.findViewById(R.id.tv_order_items_summary);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
        }

        void bind(Order order) {
            tvOrderCode.setText(String.valueOf(order.getOrderCode()));

            // Set Status
            tvOrderStatus.setText(capitalize(order.getStatus()));
            // (Tùy chọn: Set màu dựa trên status)
            // ví dụ: if (order.getStatus().equals("cancelled")) { ... }

            // Format Date
            try {
                tvOrderDate.setText(outputFormat.format(inputFormat.parse(order.getCreatedAt())));
            } catch (Exception e) {
                tvOrderDate.setText(order.getCreatedAt());
            }

            // Format Total Price
            String totalText = "Total: " + currencyFormatter.format(order.getTotalPrice());
            tvOrderTotal.setText(totalText);

            // --- BẮT ĐẦU THAY ĐỔI: Hiển thị TẤT CẢ item ---
            StringBuilder itemsSummary = new StringBuilder();
            if (order.getItems() != null && !order.getItems().isEmpty()) {

                // Lặp qua tất cả các item trong đơn hàng
                for (int i = 0; i < order.getItems().size(); i++) {
                    OrderItem item = order.getItems().get(i);

                    if (item.getBookInfo() != null) {
                        itemsSummary.append(item.getBookInfo().getTitle())
                                .append(" (x")
                                .append(item.getQuantity())
                                .append(")");
                    } else {
                        // Trường hợp bookInfo bị null
                        itemsSummary.append("Unknown Item (x")
                                .append(item.getQuantity())
                                .append(")");
                    }

                    // Thêm dấu xuống dòng nếu không phải là item cuối cùng
                    if (i < order.getItems().size() - 1) {
                        itemsSummary.append("\n");
                    }
                }

            } else {
                itemsSummary.append("No items in this order.");
            }
            tvOrderItemsSummary.setText(itemsSummary.toString());
            // --- KẾT THÚC THAY ĐỔI ---
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
    }
}