const Book = require("../models/Book");
const Category = require("../models/Category");
const { removeAccents } = require("../utils/helpers");

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

    const { q, sortBy, order } = req.query;

    console.log(
      `Controller: Lấy tất cả sách - Trang ${page}, Giới hạn ${limit}, Query: ${q}`
    );

    // 1. Xây dựng bộ lọc (filter) - SỬA LẠI LOGIC TÌM KIẾM
    const filter = {};

    if (q) {
      // Chuyển query của người dùng sang không dấu, chữ thường
      const searchQuery = removeAccents(q.toLowerCase());

      // Tạo một biểu thức Regex để tìm kiếm
      // 'i' = không phân biệt hoa thường (mặc dù chúng ta đã dùng toLowerCase)
      const searchRegex = new RegExp(searchQuery, "i");

      // Tìm kiếm trên CẢ 3 trường không dấu
      filter.$or = [
        { title_unaccented: searchRegex },
        { author_unaccented: searchRegex },
        { description_unaccented: searchRegex },
      ];

      // Bỏ logic $text cũ
      // filter.$text = { $search: q };
    }

    // 2. Xây dựng tùy chọn sắp xếp (sort)
    let sortOptions = {};
    if (sortBy && order) {
      sortOptions[sortBy] = order === "desc" ? -1 : 1;
    } else {
      // Mặc định sắp xếp theo mới nhất
      sortOptions = { createdAt: "desc" };
    }
    // Bỏ logic textScore
    // const projection = q ? { score: { $meta: "textScore" } } : {};

    // 3. Thực thi query
    const [books, totalItems] = await Promise.all([
      Book.find(filter) // Bỏ projection
        .sort(sortOptions)
        .skip(skip)
        .limit(limit)
        .populate("categoryId"),
      Book.countDocuments(filter),
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

const uploadBookCover = async (req, res) => {
  try {
    // 1. Kiểm tra xem middleware 'uploadCloud' có upload file thành công không?
    if (!req.file) {
      return res.status(400).json({ msg: "Lỗi: Không có file nào được chọn." });
    }

    // 2. Middleware đã upload file lên Cloudinary
    //    Thông tin file nằm trong req.file
    //    URL của ảnh nằm trong req.file.path
    const imageUrl = req.file.path;
    const publicId = req.file.filename; // Đây là public_id để sau này có thể xóa

    // (Tùy chọn) Lấy thêm thông tin từ body, ví dụ bookId
    // const { bookId } = req.body;
    // (Tùy chọn) Lưu imageUrl vào database
    // await Book.findByIdAndUpdate(bookId, { coverImageUrl: imageUrl });

    // 3. Trả về kết quả
    res.status(200).json({
      message: "Upload ảnh bìa thành công!",
      imageUrl: imageUrl,
      publicId: publicId,
    });
  } catch (error) {
    res.status(500).json({
      msg: "Lỗi server khi upload ảnh",
      error: error.message,
    });
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
  uploadBookCover,
};
