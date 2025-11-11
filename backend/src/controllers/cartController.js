const Cart = require("../models/Cart");
const Book = require("../models/Book");
const Inventory = require("../models/Inventory");

/**
 * @desc    Lấy giỏ hàng của người dùng (FIX CUỐI CÙNG)
 * @route   GET /api/cart
 * @access  Private
 */
exports.getCart = async (req, res, next) => {
  try {
    const userId = req.user.id;

    // 1. Populate 'items.bookId'
    const cart = await Cart.findOne({ userId })
      .populate({
        path: "items.bookId",
        // 2. Select tất cả các trường cần thiết (bao gồm 'categoryId')
        select:
          "title price coverImage author discount description rating categoryId",
        // 3. Populate lồng 'categoryId' để lấy tên
        populate: {
          path: "categoryId",
          select: "name",
        },
      })
      .populate("branchId", "name address");

    if (!cart || !cart.branchId) {
      return res.json({ items: [], totalPrice: 0, branch: null });
    }

    let totalPrice = 0;
    const validItems = [];

    // 4. Lọc các item hợp lệ (sách có thể đã bị xóa)
    for (const item of cart.items) {
      if (item.bookId) {
        const finalPrice = item.bookId.price * (1 - item.bookId.discount / 100);
        totalPrice += finalPrice * item.quantity;
        validItems.push(item);
      }
    }

    // 5. XÓA BỎ HOÀN TOÀN .map()
    // Trả về 'validItems' nguyên bản.
    // Android (BookItem.java) sẽ tự xử lý 'categoryId'.
    res.status(200).json({
      items: validItems, // Trả về dữ liệu gốc, không biến đổi
      totalPrice: totalPrice,
      branch: cart.branchId,
    });
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Thêm sản phẩm vào giỏ hàng
 * @route   POST /api/cart
 * @access  Private
 */
exports.addItemToCart = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { bookId, quantity = 1, branchId } = req.body;

    if (!branchId) {
      res.status(400);
      throw new Error("Vui lòng chọn một chi nhánh để mua hàng.");
    }

    const inventoryItem = await Inventory.findOne({ bookId, branchId });
    if (!inventoryItem || inventoryItem.stock < quantity) {
      res.status(404);
      throw new Error("Sản phẩm đã hết hàng tại chi nhánh này.");
    }

    let cart = await Cart.findOne({ userId });

    if (cart) {
      if (cart.branchId && cart.branchId.toString() !== branchId) {
        cart.items = [];
        cart.branchId = branchId;
      } else if (!cart.branchId) {
        cart.branchId = branchId;
      }

      const itemIndex = cart.items.findIndex(
        (item) => item.bookId.toString() === bookId
      );

      if (itemIndex > -1) {
        const newQuantity = cart.items[itemIndex].quantity + quantity;

        if (inventoryItem.stock < newQuantity) {
          res.status(400);
          throw new Error("Không đủ số lượng tồn kho.");
        }
        cart.items[itemIndex].quantity = newQuantity;
      } else {
        cart.items.push({ bookId, quantity });
      }
      await cart.save();
    } else {
      cart = await Cart.create({
        userId,
        branchId,
        items: [{ bookId, quantity }],
      });
    }

    res.status(200).json({ message: "Đã thêm vào giỏ hàng" });
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Cập nhật số lượng item
 * @route   PUT /api/cart/:bookId
 * @access  Private
 */
exports.updateItemQuantity = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { bookId } = req.params;
    const { quantity } = req.body;

    if (quantity <= 0) {
      return exports.removeItemFromCart(req, res, next);
    }

    const cart = await Cart.findOne({ userId });
    if (!cart || !cart.branchId) {
      res.status(404);
      throw new Error("Không tìm thấy giỏ hàng.");
    }

    const inventoryItem = await Inventory.findOne({
      bookId,
      branchId: cart.branchId,
    });
    if (!inventoryItem || inventoryItem.stock < quantity) {
      res.status(400);
      throw new Error("Không đủ số lượng tồn kho tại chi nhánh này.");
    }

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

    const cart = await Cart.findOne({ userId });
    if (!cart) {
      return res.status(200).json({ message: "Giỏ hàng đã rỗng." });
    }

    cart.items.pull({ bookId: bookId });

    if (cart.items.length === 0) {
      cart.branchId = undefined;
    }

    await cart.save();

    res.status(200).json({ message: "Đã xóa sản phẩm khỏi giỏ hàng" });
  } catch (error) {
    next(error);
  }
};
