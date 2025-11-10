const Category = require("../models/Category");

/**
 * @desc    Lấy tất cả danh mục
 * @route   GET /api/categories
 * @access  Public
 */
const getAllCategories = async (req, res, next) => {
  try {
    console.log("Controller: Lấy tất cả danh mục");
    const categories = await Category.find().sort({ name: "asc" });
    res.json(categories);
  } catch (error) {
    next(error); // Chuyển lỗi cho errorHandler
  }
};

module.exports = {
  getAllCategories,
};
