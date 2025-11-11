package com.hofang.bookchainfe.model;

import com.google.gson.annotations.SerializedName; // <-- THÊM IMPORT NÀY
import com.hofang.bookchainfe.ui.home.BookItem;
import java.util.List;
import java.io.Serializable;

public class CartResponse {
    private List<CartItem> items;
    private double totalPrice;

    // --- THÊM MỚI ---
    // Tên "branch" phải khớp với key JSON mà backend trả về
    @SerializedName("branch")
    private Branch branch;
    // --- KẾT THÚC THÊM MỚI ---

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    // --- THÊM MỚI ---
    public Branch getBranch() {
        return branch;
    }
    // --- KẾT THÚC THÊM MỚI ---

    // (Class CartItem bên trong giữ nguyên)
    public static class CartItem implements Serializable{
        private BookItem bookId;
        private int quantity;
        private String _id;

        public BookItem getBook() {
            return bookId;
        }
        public int getQuantity() {
            return quantity;
        }
        public String getId() {
            return _id;
        }
    }

    // --- THÊM MỚI (hoặc import) ---
    // Thêm một class lồng (nested) tối thiểu để Gson có thể parse object 'branch'
    // Nếu bạn đã có file model.Branch, bạn có thể xóa class này và
    // import file đó: import com.hofang.bookchainfe.model.Branch;
    public static class Branch implements Serializable {
        @SerializedName("_id")
        private String id;
        @SerializedName("name")
        private String name;

        public String getId() { return id; }
        public String getName() { return name; }
    }
}