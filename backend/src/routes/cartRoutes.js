// routes/cartRoutes.js
const express = require("express");
const router = express.Router();
const {
  getCart,
  addItemToCart,
  updateItemQuantity,
  removeItemFromCart,
} = require("../controllers/cartController");

// Lấy middleware từ file của bạn
const { authenticate } = require("../middleware/auth"); // (Giả sử file là 'middleware/authenticate.js')

// Áp dụng 'authenticate' cho tất cả các route
router.use(authenticate);

// Route chính: Lấy giỏ hàng VÀ Thêm vào giỏ hàng
router.route("/").get(getCart).post(addItemToCart);

// Route cho từng item: Cập nhật số lượng VÀ Xóa
router.route("/:bookId").put(updateItemQuantity).delete(removeItemFromCart);

module.exports = router;
