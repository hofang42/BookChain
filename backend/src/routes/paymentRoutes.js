const express = require("express");
const router = express.Router();
const {
  createPaymentLink,
  handleCancelOrder,
  handlePayOsWebhook, // Đảm bảo đã import hàm này
} = require("../controllers/paymentController");
const { authenticate } = require("../middleware/auth");

router.use(authenticate);

router.post("/create-link", createPaymentLink);
router.post("/cancel-order", handleCancelOrder);

// LƯU Ý: Route webhook sẽ được định nghĩa ở file index.js (hoặc app.js)
// router.post("/webhook", handlePayOsWebhook);

module.exports = router;
