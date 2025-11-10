const express = require("express");
const dotenv = require("dotenv");
dotenv.config({ path: "./.env" });
const usersRoutes = require("./routes/users");
const booksRoutes = require("./routes/books");
const categoryRoutes = require("./routes/categories");
const branchesRoutes = require("./routes/branches");
const authRoutes = require("./routes/auth");
const addressesRoutes = require("./routes/addresses");
const cartRoutes = require("./routes/cartRoutes");
const cors = require("cors");
const { errorHandler } = require("./middleware/errorHandler");
const connectDB = require("./services/db");
const paymentRoutes = require("./routes/paymentRoutes");
const { handlePayOsWebhook } = require("./controllers/paymentController");

// --- BƯỚC 1: Khởi tạo 'app' ĐẦU TIÊN ---
const app = express();

// --- BƯỚC 2: Định nghĩa Webhook (cần raw body) ---
// Phải nằm TRƯỚC app.use(express.json())
app.post(
  "/api/payments/webhook",
  express.raw({ type: "application/json" }),
  handlePayOsWebhook
);

// --- BƯỚC 3: Mới đến các middleware chung ---
app.use(express.json());
app.use(cors());

// --- BƯỚC 4: Các routes còn lại ---
app.get("/health", (req, res) => res.json({ status: "ok" }));

app.use("/api/auth", authRoutes);
app.use("/api/users", usersRoutes);
app.use("/api/books", booksRoutes);
app.use("/api/branches", branchesRoutes);
app.use("/api/addresses", addressesRoutes);
app.use("/api/categories", categoryRoutes);
app.use("/api/cart", cartRoutes);
app.use("/api/payments", paymentRoutes); // Route này giờ chỉ còn create-link và cancel-order

// --- BƯỚC 5: Error handler (thường nằm cuối) ---
app.use(errorHandler);

const PORT = process.env.PORT || 3000;

(async () => {
  try {
    await connectDB();
    app.listen(PORT, () =>
      console.log(`Server listening on http://localhost:${PORT}`)
    );
  } catch (err) {
    console.error("Failed to start server:", err);
    process.exit(1);
  }
})();
