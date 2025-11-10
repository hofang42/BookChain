const mongoose = require("mongoose");
const { Schema } = mongoose;

const orderItemSchema = new Schema({
  bookId: { type: Schema.Types.ObjectId, ref: "Book", required: true },
  quantity: { type: Number, required: true, min: 1 },
  priceAtPurchase: { type: Number, required: true },
});

const orderSchema = new Schema(
  {
    userId: { type: Schema.Types.ObjectId, ref: "User", required: true },
    branchId: { type: Schema.Types.ObjectId, ref: "Branch" },
    items: [orderItemSchema],
    totalPrice: { type: Number, required: true },
    status: {
      type: String,
      enum: ["pending", "confirmed", "delivering", "completed", "cancelled"],
      default: "pending",
    },
    paymentMethod: {
      type: String,
      // THÊM "payOS" VÀO ĐÂY
      enum: ["cash", "credit_card", "momo", "zalo_pay", "payOS"],
    },
    deliveryAddress: { type: String, required: true }, // Nên yêu cầu địa chỉ khi thanh toán online

    // THÊM TRƯỜNG NÀY
    orderCode: { type: Number, required: true, unique: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model("Order", orderSchema);
