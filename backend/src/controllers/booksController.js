const Book = require("../models/Book");
const Category = require("../models/Category");
const Branch = require("../models/Branch");
const Inventory = require("../models/Inventory");
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

    // --- BẮT ĐẦU SỬA ĐỔI ---
    // Thêm categoryId vào query
    const { q, sortBy, order, categoryId } = req.query;

    console.log(
      `Controller: Lấy tất cả sách - Trang ${page}, Giới hạn ${limit}, Query: ${q}, Category: ${categoryId}`
    );

    // 1. Xây dựng bộ lọc (filter)
    const filter = {};

    if (q) {
      const searchQuery = removeAccents(q.toLowerCase());
      const searchRegex = new RegExp(searchQuery, "i");
      filter.$or = [
        { title_unaccented: searchRegex },
        { author_unaccented: searchRegex },
        { description_unaccented: searchRegex },
      ];
    }

    // THÊM: Nếu có categoryId, thêm vào bộ lọc
    if (categoryId) {
      filter.categoryId = categoryId;
    }
    // --- KẾT THÚC SỬA ĐỔI ---

    // 2. Xây dựng tùy chọn sắp xếp (sort)
    let sortOptions = {};
    if (sortBy && order) {
      sortOptions[sortBy] = order === "desc" ? -1 : 1;
    } else {
      // Mặc định sắp xếp theo mới nhất (hoặc theo textScore nếu có tìm kiếm)
      sortOptions = { createdAt: "desc" };
    }

    // 3. Thực thi query
    const [books, totalItems] = await Promise.all([
      Book.find(filter)
        .sort(sortOptions)
        .skip(skip)
        .limit(limit)
        .populate("categoryId"), // Bạn đã populate sẵn, rất tốt
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

    // --- BẮT ĐẦU SỬA ĐỔI ---
    // Thêm sortBy và order
    const { sortBy, order } = req.query;
    console.log(
      `Controller: Lấy sách cho '${categorySlug}' - Trang ${page}, Sort: ${sortBy}:${order}`
    );
    // --- KẾT THÚC SỬA ĐỔI ---

    const category = await Category.findOne({ slug: categorySlug });
    if (!category) {
      res.status(404);
      throw new Error("Không tìm thấy danh mục");
    }

    const filter = { categoryId: category._id };

    // --- BẮT ĐẦU SỬA ĐỔI ---
    // Xây dựng tùy chọn sắp xếp (sort)
    let sortOptions = {};
    if (sortBy && order) {
      sortOptions[sortBy] = order === "desc" ? -1 : 1;
    } else {
      // Mặc định sắp xếp theo mới nhất
      sortOptions = { createdAt: "desc" };
    }
    // --- KẾT THÚC SỬA ĐỔI ---

    const [books, totalItems] = await Promise.all([
      Book.find(filter)
        .sort(sortOptions) // <-- Sử dụng sortOptions
        .skip(skip)
        .limit(limit)
        .populate("categoryId"),
      Book.countDocuments(filter),
    ]);

    const totalPages = Math.ceil(totalItems / limit);

    res.json({
      category, // Giữ lại để FE biết đang lọc category nào
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

/**
 * @desc    Lấy các chi nhánh có sách cụ thể, sắp xếp theo khoảng cách
 * @route   GET /api/books/:id/branches
 * @access  Public
 * @query   lat, lng (latitude và longitude của người dùng)
 */
const getBranchesWithBook = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { lat, lng, limit = 10 } = req.query;

    // 1. Kiểm tra sách có tồn tại không
    const book = await Book.findById(id);
    if (!book) {
      res.status(404);
      throw new Error("Không tìm thấy sách");
    }

    // 2. Tìm các inventory có sách này và stock > 0
    const inventories = await Inventory.find({
      bookId: id,
      stock: { $gt: 0 },
    }).populate("branchId");

    if (!inventories || inventories.length === 0) {
      return res.json({
        book: {
          _id: book._id,
          title: book.title,
          author: book.author,
          coverImage: book.coverImage,
        },
        branches: [],
        message: "Hiện tại không có chi nhánh nào có sách này",
      });
    }

    // 3. Lấy danh sách branches từ inventories
    let branches = inventories
      .map((inv) => ({
        branch: inv.branchId,
        quantity: inv.stock,
      }))
      .filter((item) => item.branch && item.branch.isActive);

    // 4. Nếu có tọa độ người dùng, sắp xếp theo khoảng cách
    if (lat && lng) {
      const userLat = parseFloat(lat);
      const userLng = parseFloat(lng);

      if (!isNaN(userLat) && !isNaN(userLng)) {
        // Tính khoảng cách cho mỗi branch
        branches = branches.map((item) => {
          const branchLng = item.branch.location.coordinates[0];
          const branchLat = item.branch.location.coordinates[1];

          // Công thức Haversine để tính khoảng cách (km)
          const distance = calculateDistance(
            userLat,
            userLng,
            branchLat,
            branchLng
          );

          return {
            ...item.branch.toObject(),
            quantity: item.quantity,
            distance: parseFloat(distance.toFixed(2)), // km, làm tròn 2 chữ số
          };
        });

        // Sắp xếp theo khoảng cách tăng dần
        branches.sort((a, b) => a.distance - b.distance);
      }
    } else {
      // Nếu không có tọa độ, chỉ trả về danh sách branches
      branches = branches.map((item) => ({
        ...item.branch.toObject(),
        quantity: item.quantity,
      }));
    }

    // 5. Giới hạn số lượng kết quả
    const limitNum = parseInt(limit) || 10;
    branches = branches.slice(0, limitNum);

    res.json({
      book: {
        _id: book._id,
        title: book.title,
        author: book.author,
        coverImage: book.coverImage,
        price: book.price,
        discount: book.discount,
      },
      userLocation:
        lat && lng ? { lat: parseFloat(lat), lng: parseFloat(lng) } : null,
      totalBranches: branches.length,
      branches,
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Helper function: Tính khoảng cách giữa 2 điểm theo công thức Haversine
 * @param {number} lat1 - Latitude điểm 1
 * @param {number} lng1 - Longitude điểm 1
 * @param {number} lat2 - Latitude điểm 2
 * @param {number} lng2 - Longitude điểm 2
 * @returns {number} Khoảng cách tính theo km
 */
function calculateDistance(lat1, lng1, lat2, lng2) {
  const R = 6371; // Bán kính trái đất (km)
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) *
      Math.cos(toRad(lat2)) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = R * c;

  return distance;
}

function toRad(degrees) {
  return degrees * (Math.PI / 180);
}

module.exports = {
  getBestDeals,
  getTopBooks,
  getLatestBooks,
  getMostBuyingBooks,
  getAllBooks,
  getBooksByCategory,
  getBookById,
  uploadBookCover,
  getBranchesWithBook,
};
