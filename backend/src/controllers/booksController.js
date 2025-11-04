const Book = require("../models/Book");
const Category = require("../models/Category");

/**
 * @desc    Lấy sách giảm giá tốt nhất
 * @route   GET /api/books/best-deals
 * @access  Public
 */
const getBestDeals = async (req, res, next) => {
  try {
    const limit = parseInt(req.query.limit) || 8;
    console.log(`Controller: Lấy ${limit} sách giảm giá tốt nhất`);

    const books = await Book.find({ discount: { $gt: 0 } })
      .sort({ discount: "desc" })
      .limit(limit)
      .populate("categoryId");

    res.json(books);
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Lấy sách đánh giá cao nhất
 * @route   GET /api/books/top-books
 * @access  Public
 */
const getTopBooks = async (req, res, next) => {
  try {
    const limit = parseInt(req.query.limit) || 8;
    console.log(`Controller: Lấy ${limit} sách đánh giá cao nhất`);

    const books = await Book.find()
      .sort({ rating: "desc" })
      .limit(limit)
      .populate("categoryId");

    res.json(books);
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Lấy sách mới nhất
 * @route   GET /api/books/latest-books
 * @access  Public
 */
const getLatestBooks = async (req, res, next) => {
  try {
    const limit = parseInt(req.query.limit) || 8;
    console.log(`Controller: Lấy ${limit} sách mới nhất`);

    const books = await Book.find()
      .sort({ createdAt: "desc" })
      .limit(limit)
      .populate("categoryId");

    res.json(books);
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Lấy sách bán chạy nhất
 * @route   GET /api/books/most-buying
 * @access  Public
 */
const getMostBuyingBooks = async (req, res, next) => {
  try {
    const limit = parseInt(req.query.limit) || 8;
    console.log(`Controller: Lấy ${limit} sách bán chạy nhất`);

    const books = await Book.find()
      .sort({ salesCount: "desc" })
      .limit(limit)
      .populate("categoryId");

    res.json(books);
  } catch (error) {
    next(error);
  }
};

// --- API cho Trang "Tất cả sách" và "Danh mục" ---

/**
 * @desc    Lấy tất cả sách (có phân trang)
 * @route   GET /api/books
 * @access  Public
 */
const getAllBooks = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    // Lấy tất cả tham số query
    const { q, sortBy, order } = req.query;

    console.log(
      `Controller: Lấy tất cả sách - Trang ${page}, Giới hạn ${limit}, Query: ${q}`
    );

    // 1. Xây dựng bộ lọc (filter) - Gộp từ getBooks
    const filter = {};
    if (q) {
      filter.$text = { $search: q };
    }

    // 2. Xây dựng tùy chọn sắp xếp (sort)
    let sortOptions = {};
    if (sortBy && order) {
      sortOptions[sortBy] = order === "desc" ? -1 : 1;
    } else if (q) {
      // Nếu tìm kiếm, ưu tiên sắp xếp theo điểm liên quan
      sortOptions = { score: { $meta: "textScore" } };
    } else {
      // Mặc định sắp xếp theo mới nhất
      sortOptions = { createdAt: "desc" };
    }

    // Thêm projection để lấy textScore nếu tìm kiếm
    const projection = q ? { score: { $meta: "textScore" } } : {};

    // 3. Thực thi query
    const [books, totalItems] = await Promise.all([
      Book.find(filter, projection) // <-- Áp dụng filter và projection
        .sort(sortOptions) // <-- Áp dụng sort
        .skip(skip)
        .limit(limit)
        .populate("categoryId"),
      Book.countDocuments(filter), // <-- Áp dụng filter
    ]);

    const totalPages = Math.ceil(totalItems / limit);

    res.json({
      totalItems,
      totalPages,
      currentPage: page,
      itemsPerPage: limit,
      data: books,
    });
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Lấy sách theo danh mục (có phân trang)
 * @route   GET /api/books/category/:categorySlug
 * @access  Public
 */
const getBooksByCategory = async (req, res, next) => {
  try {
    const { categorySlug } = req.params;
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    console.log(
      `Controller: Lấy sách cho '${categorySlug}' - Trang ${page}, Giới hạn ${limit}`
    );

    const category = await Category.findOne({ slug: categorySlug });
    if (!category) {
      res.status(404);
      throw new Error("Không tìm thấy danh mục");
    }

    const filter = { categoryId: category._id };
    const [books, totalItems] = await Promise.all([
      Book.find(filter)
        .sort({ createdAt: "desc" })
        .skip(skip)
        .limit(limit)
        .populate("categoryId"),
      Book.countDocuments(filter),
    ]);

    const totalPages = Math.ceil(totalItems / limit);

    res.json({
      category,
      totalItems,
      totalPages,
      currentPage: page,
      itemsPerPage: limit,
      data: books,
    });
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Lấy chi tiết 1 cuốn sách
 * @route   GET /api/books/:id
 * @access  Public
 */
const getBookById = async (req, res, next) => {
  try {
    const book = await Book.findById(req.params.id).populate("categoryId");
    if (!book) {
      res.status(404);
      throw new Error("Không tìm thấy sách");
    }
    res.json(book);
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getBestDeals,
  getTopBooks,
  getLatestBooks,
  getMostBuyingBooks,
  getAllBooks,
  getBooksByCategory,
  getBookById,
};
