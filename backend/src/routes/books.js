const express = require("express");
const router = express.Router();
const {
  getBestDeals,
  getTopBooks,
  getLatestBooks,
  getMostBuyingBooks,
  getAllBooks,
  getBooksByCategory,
  getBookById,
} = require("../controllers/booksController");

router.get("/best-deals", getBestDeals);
router.get("/top-books", getTopBooks);
router.get("/latest-books", getLatestBooks);
router.get("/most-buying", getMostBuyingBooks);

router.get("/", getAllBooks);

router.get("/category/:categorySlug", getBooksByCategory);

router.get("/:id", getBookById);

module.exports = router;
