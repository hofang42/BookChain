const express = require("express");
const router = express.Router();
const uploadCloud = require("../config/cloudinary");
const {
  getBestDeals,
  getTopBooks,
  getLatestBooks,
  getMostBuyingBooks,
  getAllBooks,
  getBooksByCategory,
  getBookById,
  uploadBookCover,
} = require("../controllers/booksController");

router.get("/best-deals", getBestDeals);
router.get("/top-books", getTopBooks);
router.get("/latest-books", getLatestBooks);
router.get("/most-buying", getMostBuyingBooks);

router.get("/", getAllBooks);
router.get("/search", getAllBooks);

router.get("/category/:categorySlug", getBooksByCategory);

router.get("/:id", getBookById);

router.post("/upload", uploadCloud.single("coverImage"), uploadBookCover);

module.exports = router;
