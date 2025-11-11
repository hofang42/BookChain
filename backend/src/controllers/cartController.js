const Cart = require("../models/Cart");
const Book = require("../models/Book");
const Inventory = require("../models/Inventory"); // <-- 1. IMPORT INVENTORY

/**
 * @desc    Lấy giỏ hàng của người dùng (ĐÃ CẬP NHẬT LOGIC)
 * @route   GET /api/cart
 * @access  Private
 */
exports.getCart = async (req, res, next) => {
  try {
    const userId = req.user.id;

    // THAY ĐỔI: Populate thêm thông tin sách (thêm author, discount)
    // và populate thông tin chi nhánh
    const cart = await Cart.findOne({ userId })
      .populate({
        path: "items.bookId",
        select: "title price coverImage author discount",
      })
      .populate("branchId", "name address"); // Lấy tên và địa chỉ chi nhánh

    if (!cart || !cart.branchId) {
      // Nếu không có giỏ hàng, hoặc không có chi nhánh (giỏ hàng rỗng)
      return res.json({ items: [], totalPrice: 0, branch: null });
    }

    // THAY ĐỔI: Tính tổng tiền (totalPrice) CÓ BAO GỒM DISCOUNT
    let totalPrice = 0;
    const validItems = []; // Lọc ra các sách có thể đã bị xóa

    for (const item of cart.items) {
      if (item.bookId) {
        // Tính giá cuối cùng của 1 sản phẩm
        const finalPrice = item.bookId.price * (1 - item.bookId.discount / 100);
        totalPrice += finalPrice * item.quantity;
        validItems.push(item);
      }
      // Nếu item.bookId là null (sách đã bị xóa khỏi DB), nó sẽ tự động bị bỏ qua
    }

    res.status(200).json({
      items: validItems,
      totalPrice: totalPrice,
      branch: cart.branchId, // Trả về chi nhánh hiện tại của giỏ hàng
    });
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Thêm sản phẩm vào giỏ hàng (ĐÃ VIẾT LẠI HOÀN TOÀN)
 * @route   POST /api/cart
 * @access  Private
 */
exports.addItemToCart = async (req, res, next) => {
  try {
    const userId = req.user.id;
    // THAY ĐỔI: Bắt buộc phải có 'branchId' khi thêm hàng
    const { bookId, quantity = 1, branchId } = req.body;

    if (!branchId) {
      res.status(400);
      throw new Error("Vui lòng chọn một chi nhánh để mua hàng.");
    }

    // 1. Kiểm tra xem sách có tồn tại và CÓ HÀNG tại chi nhánh không
    const inventoryItem = await Inventory.findOne({ bookId, branchId });
    if (!inventoryItem || inventoryItem.stock < quantity) {
      res.status(404);
      throw new Error("Sản phẩm đã hết hàng tại chi nhánh này.");
    }

    // 2. Tìm giỏ hàng của người dùng
    let cart = await Cart.findOne({ userId });

    if (cart) {
      // 3. Nếu giỏ hàng tồn tại:

      // 3a. KIỂM TRA ĐỔI CHI NHÁNH
      // Nếu người dùng thêm sách từ chi nhánh MỚI, xóa giỏ hàng cũ.
      if (cart.branchId && cart.branchId.toString() !== branchId) {
        cart.items = []; // Xóa các item của chi nhánh cũ
        cart.branchId = branchId; // Đổi sang chi nhánh mới
      } else if (!cart.branchId) {
        // Nếu giỏ hàng rỗng, gán chi nhánh mới
        cart.branchId = branchId;
      }

      // 3b. Xử lý item
      const itemIndex = cart.items.findIndex(
        (item) => item.bookId.toString() === bookId
      );

      if (itemIndex > -1) {
        // Sách đã có -> Cập nhật số lượng
        const newQuantity = cart.items[itemIndex].quantity + quantity;

        // Kiểm tra lại tồn kho cho số lượng mới
        if (inventoryItem.stock < newQuantity) {
          res.status(400);
          throw new Error("Không đủ số lượng tồn kho.");
        }
        cart.items[itemIndex].quantity = newQuantity;
      } else {
        // Sách chưa có -> Thêm mới vào mảng
        cart.items.push({ bookId, quantity });
      }
      await cart.save();
    } else {
      // 4. Nếu chưa có giỏ hàng, tạo giỏ hàng mới
      cart = await Cart.create({
        userId,
        branchId, // Gán chi nhánh cho giỏ hàng mới
        items: [{ bookId, quantity }],
      });
    }

    res.status(200).json({ message: "Đã thêm vào giỏ hàng" });
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Cập nhật số lượng item (ĐÃ CẬP NHẬT LOGIC KHO)
 * @route   PUT /api/cart/:bookId
 * @access  Private
 */
exports.updateItemQuantity = async (req, res, next) => {
  try {
    const userId = req.user.id;
    const { bookId } = req.params;
    const { quantity } = req.body; // Số lượng MỚI (ví dụ: 5)

    if (quantity <= 0) {
      // Nếu số lượng <= 0, coi như là xóa
      return exports.removeItemFromCart(req, res, next);
    }

    const cart = await Cart.findOne({ userId });
    if (!cart || !cart.branchId) {
      res.status(404);
      throw new Error("Không tìm thấy giỏ hàng.");
    }

    // THAY ĐỔI: Kiểm tra tồn kho trước khi cập nhật
    const inventoryItem = await Inventory.findOne({
      bookId,
      branchId: cart.branchId,
    });
    if (!inventoryItem || inventoryItem.stock < quantity) {
      res.status(400);
      throw new Error("Không đủ số lượng tồn kho tại chi nhánh này.");
    }

    // Cập nhật số lượng
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

    // THAY ĐỔI: Logic này vẫn đúng, nhưng chúng ta cần kiểm tra xem giỏ hàng
    // có còn item nào không. Nếu không, nên xóa luôn branchId.
    const cart = await Cart.findOne({ userId });
    if (!cart) {
      return res.status(200).json({ message: "Giỏ hàng đã rỗng." });
    }

    // $pull dùng để xóa một element khỏi mảng
    cart.items.pull({ bookId: bookId });

    // Nếu giỏ hàng rỗng, xóa luôn chi nhánh
    if (cart.items.length === 0) {
      cart.branchId = undefined; // hoặc null
    }

    await cart.save();

    res.status(200).json({ message: "Đã xóa sản phẩm khỏi giỏ hàng" });
  } catch (error) {
    next(error);
  }
};
