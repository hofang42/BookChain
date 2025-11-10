const Cart = require("../models/Cart");
const Book = require("../models/Book"); // Cần để kiểm tra sách

/**
 * @desc    Lấy giỏ hàng của người dùng
 * @route   GET /api/cart
 * @access  Private (Cần đăng nhập)
 */
exports.getCart = async (req, res, next) => {
  try {
    const userId = req.user.id; // Lấy từ middleware 'protect'

    // Tìm giỏ hàng và populate thông tin sách
    // Chúng ta chỉ lấy các trường cần thiết của sách
    const cart = await Cart.findOne({ userId }).populate(
      "items.bookId",
      "title price coverImage"
    );

    if (!cart) {
      // Nếu không có giỏ hàng, trả về một giỏ hàng rỗng
      return res.json({ items: [], totalPrice: 0 });
    }

    // Tính tổng tiền (totalPrice)
    // Trường 'totalPrice' không lưu trong DB để tránh sai lệch
    let totalPrice = 0;
    cart.items.forEach((item) => {
      if (item.bookId) {
        totalPrice += item.bookId.price * item.quantity;
      }
    });

    res.status(200).json({
      items: cart.items,
      totalPrice: totalPrice,
    });
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Thêm một sản phẩm vào giỏ hàng (hoặc cập nhật số lượng)
 * @route   POST /api/cart
 * @access  Private
 */
exports.addItemToCart = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { bookId, quantity = 1 } = req.body;

    // 1. Kiểm tra xem sách có tồn tại không
    const book = await Book.findById(bookId);
    if (!book) {
      res.status(404);
      throw new Error("Không tìm thấy sách");
    }

    // 2. Tìm giỏ hàng của người dùng
    let cart = await Cart.findOne({ userId });

    if (cart) {
      // 3. Nếu giỏ hàng tồn tại, kiểm tra xem sách đã có trong giỏ chưa
      const itemIndex = cart.items.findIndex(
        (item) => item.bookId.toString() === bookId
      );

      if (itemIndex > -1) {
        // Sách đã có -> Cập nhật số lượng
        cart.items[itemIndex].quantity += quantity;
      } else {
        // Sách chưa có -> Thêm mới vào mảng
        cart.items.push({ bookId, quantity });
      }
      await cart.save();
    } else {
      // 4. Nếu chưa có giỏ hàng, tạo giỏ hàng mới
      cart = await Cart.create({
        userId,
        items: [{ bookId, quantity }],
      });
    }

    res.status(200).json({ message: "Đã thêm vào giỏ hàng" });
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Cập nhật số lượng item (ví dụ: từ CartFragment)
 * @route   PUT /api/cart/:bookId
 * @access  Private
 */
exports.updateItemQuantity = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { bookId } = req.params;
    const { quantity } = req.body;

    if (quantity <= 0) {
      // Nếu số lượng <= 0, coi như là xóa
      return exports.removeItemFromCart(req, res, next);
    }

    // $set dùng để set giá trị mới cho item trong mảng
    await Cart.updateOne(
      { userId, "items.bookId": bookId },
      { $set: { "items.$.quantity": quantity } }
    );

    res.status(200).json({ message: "Cập nhật số lượng thành công" });
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Xóa một item khỏi giỏ hàng
 * @route   DELETE /api/cart/:bookId
 * @access  Private
 */
exports.removeItemFromCart = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { bookId } = req.params;

    // $pull dùng để xóa một element khỏi mảng
    await Cart.updateOne({ userId }, { $pull: { items: { bookId: bookId } } });

    res.status(200).json({ message: "Đã xóa sản phẩm khỏi giỏ hàng" });
  } catch (error) {
    next(error);
  }
};
