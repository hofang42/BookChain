const payOs = require("../config/payos"); // Giả định bạn đã config file này
const Order = require("../models/Order");
const User = require("../models/User");
const Cart = require("../models/Cart");
const Book = require("../models/Book"); // <-- 1. IMPORT BOOK MODEL
const mongoose = require("mongoose");
const { APIError } = require("@payos/node");

// ⚠️ QUAN TRỌNG: Thay đổi scheme này cho phù hợp với app Android của bạn
// Đây là scheme để PayOS gọi lại app của bạn sau khi thanh toán
// Bạn cần cấu hình "Deep Linking" trong app Android với scheme này
const APP_SCHEME = "mybookapp"; // Ví dụ: mybookapp://

/**
 * @desc    Tạo link thanh toán PayOS cho đơn hàng sách
 * @route   POST /api/payments/create-link
 * @access  Private
 */
const createPaymentLink = async (req, res) => {
  // Nhận thông tin giỏ hàng từ app Android
  const { items, totalPrice, deliveryAddress } = req.body;
  const userId = req.user.id; // Lấy từ middleware "protect"

  if (!items || items.length === 0 || !totalPrice || !deliveryAddress) {
    return res
      .status(400)
      .json({ message: "Vui lòng cung cấp đủ thông tin đơn hàng." });
  }

  try {
    // --- TẠO MỘT ĐƠN HÀNG MỚI VỚI TRẠNG THÁI "PENDING" ---
    const newOrderCode = Date.now(); // Dùng timestamp làm mã đơn hàng
    const orderDescription = `DH ${newOrderCode}`;

    const newOrder = new Order({
      userId,
      items,
      totalPrice,
      deliveryAddress,
      paymentMethod: "payOS", // Ghi nhận phương thức thanh toán
      status: "pending", // Trạng thái chờ thanh toán
      orderCode: newOrderCode, // Lưu mã đơn hàng
    });

    await newOrder.save();
    console.log(`Tạo đơn hàng PENDING mới: ${newOrder.orderCode}`);
    // ------------------------------------

    const REDIRECT_BASE_URL = "https://bookchainpayment.vercel.app/";

    const payosOrder = {
      amount: newOrder.totalPrice,
      description: orderDescription,
      orderCode: newOrder.orderCode,
      returnUrl: `${REDIRECT_BASE_URL}?status=success&orderCode=${newOrderCode}`,
      cancelUrl: `${REDIRECT_BASE_URL}?status=cancelled&orderCode=${newOrderCode}`,
      buyerName: req.user.fullName, // Lấy từ req.user
      buyerEmail: req.user.email, // Lấy từ req.user
    };

    const paymentLinkResponse = await payOs.paymentRequests.create(payosOrder);

    res.status(200).json({
      message: "Tạo link thanh toán thành công",
      checkoutUrl: paymentLinkResponse.checkoutUrl,
      orderCode: newOrder.orderCode, // Trả về orderCode để app lưu lại
    });
  } catch (error) {
    console.error("Lỗi khi tạo link thanh toán:", error);
    if (error instanceof APIError) {
      console.error("Lỗi từ PayOS:", error.error);
    }
    res.status(500).json({ message: "Không thể tạo link thanh toán." });
  }
};

/**
 * @desc    Nhận và xử lý webhook từ PayOS
 * @route   POST /api/payments/webhook
 * @access  Public
 */
const handlePayOsWebhook = async (req, res) => {
  const webhookData = JSON.parse(req.body);

  // 3. BẮT ĐẦU MỘT SESSION
  const session = await mongoose.startSession();

  try {
    console.log("--- Bắt đầu xử lý Webhook ---");
    const verifiedData = await payOs.webhooks.verify(webhookData);

    if (
      verifiedData.code === "00" &&
      verifiedData.desc.toLowerCase() === "success"
    ) {
      const orderCode = verifiedData.orderCode;
      console.log(`Webhook xác thực thành công cho đơn hàng: ${orderCode}`);

      // 4. BẮT ĐẦU TRANSACTION
      session.startTransaction();

      // 5. TÌM ORDER (VỚI SESSION)
      const order = await Order.findOne({
        orderCode: Number(orderCode),
      }).session(session);

      if (!order) {
        console.error(
          `LỖI WEBHOOK: Không tìm thấy đơn hàng ${orderCode} trong DB.`
        );
        // Không cần abort vì chưa làm gì, chỉ cần kết thúc session
        await session.endSession();
        return res
          .status(200)
          .json({ message: "Webhook received, but order not found." });
      }

      console.log(
        `Tìm thấy đơn hàng: ${order._id}, Trạng thái: ${order.status}`
      );

      if (order.status === "pending") {
        // --- 6. LOGIC MỚI: TRỪ TỒN KHO SÁCH (TRONG TRANSACTION) ---
        // Dùng Promise.all để chạy song song các lệnh cập nhật
        const stockUpdates = order.items.map(async (item) => {
          // 'item' trong Order Schema của bạn
          const book = await Book.findById(item.bookId).session(session);

          if (!book) {
            throw new Error(`Sách với ID ${item.bookId} không tìm thấy.`);
          }
          if (book.quantity < item.quantity) {
            throw new Error(
              `Sách "${book.title}" không đủ tồn kho (cần ${item.quantity}, còn ${book.quantity}).`
            );
          }

          // Trừ số lượng
          book.quantity -= item.quantity;
          // (Tùy chọn) Cập nhật số lượng đã bán
          book.salesCount += item.quantity;

          return book.save({ session });
        });

        // Chờ tất cả các sách được cập nhật
        await Promise.all(stockUpdates);
        console.log(
          `Đã cập nhật tồn kho cho các sách trong đơn hàng ${orderCode}.`
        );
        // --- KẾT THÚC LOGIC TRỪ TỒN KHO ---

        // 7. CẬP NHẬT TRẠNG THÁI ORDER (VỚI SESSION)
        order.status = "confirmed";
        await order.save({ session });
        console.log(
          `ĐÃ CẬP NHẬT đơn hàng ${orderCode} sang trạng thái "confirmed".`
        );

        // 8. LOGIC XÓA GIỎ HÀNG (VỚI SESSION)
        const userId = order.userId;
        console.log(`Bắt đầu xóa giỏ hàng cho User ID: ${userId}`);
        const cartUpdateResult = await Cart.updateOne(
          { userId: userId },
          { $set: { items: [] } }
        ).session(session);

        if (cartUpdateResult.matchedCount > 0) {
          console.log(`Đã xóa sạch giỏ hàng cho User ID: ${userId}.`);
        } else {
          console.log(`Không tìm thấy giỏ hàng của User ID: ${userId} để xóa.`);
        }

        // 9. COMMIT TRANSACTION (NẾU TẤT CẢ THÀNH CÔNG)
        await session.commitTransaction();
        console.log(`--- Xử lý Webhook hoàn tất cho ${orderCode} ---`);
      } else {
        console.log(
          `Bỏ qua cập nhật cho đơn ${orderCode} vì trạng thái là '${order.status}'.`
        );
      }
    } else {
      console.log(
        `Webhook bị bỏ qua vì giao dịch không thành công. Code: ${verifiedData.code}, Desc: ${verifiedData.desc}`
      );
    }

    // 10. KẾT THÚC SESSION
    await session.endSession();
    return res.status(200).json({ message: "Webhook received" });
  } catch (error) {
    // 11. ABORT TRANSACTION (NẾU CÓ LỖI BẤT KỲ)
    await session.abortTransaction();
    await session.endSession();

    console.error("Lỗi xử lý webhook:", error.message);

    // Kiểm tra xem lỗi có phải từ PayOS (vd: sai signature)
    if (error.name === "SignatureVerificationError") {
      // Giả sử tên lỗi là vậy
      console.error("Lỗi xác thực webhook (sai signature?):", error.message);
      return res.status(400).json({ message: "Webhook verification failed" });
    }

    // Lỗi nghiệp vụ (hết hàng, v.v.)
    console.error(`LỖI NGHIỆP VỤ WEBHOOK: ${error.message}`);
    // Vẫn trả 200 OK để PayOS không retry, nhưng backend đã ghi nhận lỗi
    return res
      .status(200)
      .json({ message: "Webhook received but business logic failed." });
  }
};

/**
 * @desc    Hủy đơn hàng (khi người dùng chủ động hủy)
 * @route   POST /api/payments/cancel-order
 * @access  Private
 */
const handleCancelOrder = async (req, res) => {
  const { orderCode } = req.body;
  const userId = req.user.id;

  if (!orderCode) {
    return res.status(400).json({ message: "Thiếu mã đơn hàng." });
  }

  try {
    const order = await Order.findOne({
      orderCode: Number(orderCode),
      userId,
    });

    if (!order) {
      return res.status(404).json({ message: "Không tìm thấy đơn hàng." });
    }

    if (order.status === "pending") {
      order.status = "cancelled";
      await order.save();

      console.log(`Đã hủy đơn hàng: ${orderCode}`);
      return res.status(200).json({ message: "Đã hủy đơn hàng thành công." });
    } else {
      return res
        .status(200)
        .json({ message: `Đơn hàng đã ở trạng thái ${order.status}.` });
    }
  } catch (error) {
    console.error("Lỗi khi hủy đơn hàng:", error);
    res.status(500).json({ message: "Lỗi máy chủ khi hủy đơn hàng." });
  }
};

module.exports = {
  createPaymentLink,
  handlePayOsWebhook,
  handleCancelOrder,
};
