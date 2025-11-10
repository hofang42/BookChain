const express = require("express");
const router = express.Router();

// Import controllers
const {
  getUserAddresses,
  getAddressById,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  getDefaultAddress,
} = require("../controllers/addressController");

// Import middleware
const { authenticate } = require("../middleware/auth");

/**
 * All routes require authentication
 */

/**
 * @route   GET /api/addresses
 * @desc    Get all addresses for authenticated user
 * @access  Private
 */
router.get("/", authenticate, getUserAddresses);

/**
 * @route   GET /api/addresses/default
 * @desc    Get default address for authenticated user
 * @access  Private
 */
router.get("/default", authenticate, getDefaultAddress);

/**
 * @route   GET /api/addresses/:id
 * @desc    Get single address by ID
 * @access  Private
 */
router.get("/:id", authenticate, getAddressById);

/**
 * @route   POST /api/addresses
 * @desc    Create a new address
 * @access  Private
 */
router.post("/", authenticate, createAddress);

/**
 * @route   PUT /api/addresses/:id
 * @desc    Update an address
 * @access  Private
 */
router.put("/:id", authenticate, updateAddress);

/**
 * @route   DELETE /api/addresses/:id
 * @desc    Delete an address
 * @access  Private
 */
router.delete("/:id", authenticate, deleteAddress);

/**
 * @route   PUT /api/addresses/:id/default
 * @desc    Set an address as default
 * @access  Private
 */
router.put("/:id/default", authenticate, setDefaultAddress);

module.exports = router;
