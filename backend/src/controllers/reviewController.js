const Review = require("../models/Review");
const Order = require("../models/Order");
const Book = require("../models/Book");

// GET /api/books/:bookId/reviews - Get all reviews for a book
exports.getBookReviews = async (req, res) => {
  try {
    const { bookId } = req.params;

    const reviews = await Review.find({ bookId })
      .populate("userId", "fullName username email")
      .sort({ createdAt: -1 });

    res.json({
      success: true,
      data: reviews,
    });
  } catch (error) {
    console.error("Error fetching reviews:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch reviews",
    });
  }
};

// POST /api/reviews - Create a new review
exports.createReview = async (req, res) => {
  try {
    const { bookId, rating, comment } = req.body;
    const userId = req.user._id;

    // Validate input
    if (!bookId || !rating) {
      return res.status(400).json({
        success: false,
        message: "Book ID and rating are required",
      });
    }

    if (rating < 1 || rating > 5) {
      return res.status(400).json({
        success: false,
        message: "Rating must be between 1 and 5",
      });
    }

    // Check if book exists
    const book = await Book.findById(bookId);
    if (!book) {
      return res.status(404).json({
        success: false,
        message: "Book not found",
      });
    }

    // Check if user has completed order with this book
    const hasCompletedOrder = await Order.findOne({
      userId,
      "items.bookId": bookId,
      status: "completed",
    });

    if (!hasCompletedOrder) {
      return res.status(403).json({
        success: false,
        message: "You can only review books from completed orders",
      });
    }

    // Check if user already reviewed this book
    const existingReview = await Review.findOne({ userId, bookId });
    if (existingReview) {
      return res.status(400).json({
        success: false,
        message: "You have already reviewed this book",
      });
    }

    // Create review
    const review = await Review.create({
      userId,
      bookId,
      rating,
      comment,
    });

    // Populate user info
    await review.populate("userId", "fullName username email");

    // Update book's average rating
    await updateBookRating(bookId);

    res.status(201).json({
      success: true,
      data: review,
    });
  } catch (error) {
    console.error("Error creating review:", error);
    res.status(500).json({
      success: false,
      message: "Failed to create review",
    });
  }
};

// PUT /api/reviews/:id - Update a review
exports.updateReview = async (req, res) => {
  try {
    const { id } = req.params;
    const { rating, comment } = req.body;
    const userId = req.user._id;

    // Validate input
    if (rating && (rating < 1 || rating > 5)) {
      return res.status(400).json({
        success: false,
        message: "Rating must be between 1 and 5",
      });
    }

    // Find review
    const review = await Review.findById(id);
    if (!review) {
      return res.status(404).json({
        success: false,
        message: "Review not found",
      });
    }

    // Check ownership
    if (review.userId.toString() !== userId.toString()) {
      return res.status(403).json({
        success: false,
        message: "You can only edit your own reviews",
      });
    }

    // Update review
    if (rating) review.rating = rating;
    if (comment !== undefined) review.comment = comment;
    await review.save();

    // Populate user info
    await review.populate("userId", "fullName username email");

    // Update book's average rating
    await updateBookRating(review.bookId);

    res.json({
      success: true,
      data: review,
    });
  } catch (error) {
    console.error("Error updating review:", error);
    res.status(500).json({
      success: false,
      message: "Failed to update review",
    });
  }
};

// DELETE /api/reviews/:id - Delete a review
exports.deleteReview = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user._id;

    // Find review
    const review = await Review.findById(id);
    if (!review) {
      return res.status(404).json({
        success: false,
        message: "Review not found",
      });
    }

    // Check ownership
    if (review.userId.toString() !== userId.toString()) {
      return res.status(403).json({
        success: false,
        message: "You can only delete your own reviews",
      });
    }

    const bookId = review.bookId;

    // Delete review
    await Review.findByIdAndDelete(id);

    // Update book's average rating
    await updateBookRating(bookId);

    res.json({
      success: true,
      message: "Review deleted successfully",
    });
  } catch (error) {
    console.error("Error deleting review:", error);
    res.status(500).json({
      success: false,
      message: "Failed to delete review",
    });
  }
};

// GET /api/orders/check/:userId/:bookId - Check if user has completed order
exports.checkCompletedOrder = async (req, res) => {
  try {
    const { userId, bookId } = req.params;

    const hasCompletedOrder = await Order.findOne({
      userId,
      "items.bookId": bookId,
      status: "completed",
    });

    res.json({
      success: true,
      hasCompletedOrder: !!hasCompletedOrder,
    });
  } catch (error) {
    console.error("Error checking order:", error);
    res.status(500).json({
      success: false,
      message: "Failed to check order status",
    });
  }
};

// GET /api/users/:userId/reviews - Get all reviews by a user
exports.getUserReviews = async (req, res) => {
  try {
    const { userId } = req.params;

    const reviews = await Review.find({ userId })
      .populate("bookId", "title author coverImage price")
      .sort({ createdAt: -1 });

    res.json({
      success: true,
      data: reviews,
    });
  } catch (error) {
    console.error("Error fetching user reviews:", error);
    res.status(500).json({
      success: false,
      message: "Failed to fetch user reviews",
    });
  }
};

// Helper function to update book's average rating
async function updateBookRating(bookId) {
  try {
    const reviews = await Review.find({ bookId });

    if (reviews.length === 0) {
      await Book.findByIdAndUpdate(bookId, {
        rating: 0,
        reviewCount: 0,
      });
      return;
    }

    const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0);
    const averageRating = totalRating / reviews.length;

    await Book.findByIdAndUpdate(bookId, {
      rating: Math.round(averageRating * 10) / 10, // Round to 1 decimal
      reviewCount: reviews.length,
    });
  } catch (error) {
    console.error("Error updating book rating:", error);
  }
}
