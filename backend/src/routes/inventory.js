const express = require("express");
const router = express.Router();
const {
  getInventory,
  getInventoryById,
  upsertInventory,
  updateInventoryStock,
  deleteInventory,
  getLowStockItems,
  getOutOfStockItems,
  bulkUpdateInventory,
  transferStock,
} = require("../controllers/inventoryController");

// Get all inventory records (with filters)
router.get("/", getInventory);

// Get low stock and out of stock items
router.get("/low-stock", getLowStockItems);
router.get("/out-of-stock", getOutOfStockItems);

// Bulk operations
router.post("/bulk-update", bulkUpdateInventory);
router.post("/transfer", transferStock);

// Create or update inventory
router.post("/", upsertInventory);

// Get, update, delete specific inventory record
router.get("/:id", getInventoryById);
router.patch("/:id", updateInventoryStock);
router.delete("/:id", deleteInventory);

module.exports = router;
