const express = require("express");
const router = express.Router();
const {
  getBranches,
  getBranchById,
  createBranch,
  updateBranch,
  deleteBranch,
  getBranchInventory,
  getNearestBranches,
  checkBookAvailability,
} = require("../controllers/branchesController");

// Get all branches or nearest branches
router.get("/", getBranches);
router.get("/nearest", getNearestBranches);

// Check book availability across branches
router.get("/book/:bookId/availability", checkBookAvailability);

// Get, update, delete specific branch
router.get("/:id", getBranchById);
router.put("/:id", updateBranch);
router.delete("/:id", deleteBranch);

// Create new branch
router.post("/", createBranch);

// Get inventory for a specific branch
router.get("/:id/inventory", getBranchInventory);

module.exports = router;
