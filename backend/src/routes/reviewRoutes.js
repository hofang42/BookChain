const express = require("express");
const router = express.Router();
const reviewController = require("../controllers/reviewController");
const { authenticate } = require("../middleware/auth");

// Get all reviews for a book (public)
router.get("/books/:bookId/reviews", reviewController.getBookReviews);

// Get all reviews by a user (public)
router.get("/users/:userId/reviews", reviewController.getUserReviews);

// Check if user has completed order (requires auth)
router.get(
  "/orders/check/:userId/:bookId",
  authenticate,
  reviewController.checkCompletedOrder
);

// Create review (requires auth)
router.post("/reviews", authenticate, reviewController.createReview);

// Update review (requires auth)
router.put("/reviews/:id", authenticate, reviewController.updateReview);

// Delete review (requires auth)
router.delete("/reviews/:id", authenticate, reviewController.deleteReview);

module.exports = router;
