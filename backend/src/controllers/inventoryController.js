const Inventory = require("../models/Inventory");
const Branch = require("../models/Branch");
const Book = require("../models/Book");

// Get all inventory records (with pagination and filters)
const getInventory = async (req, res, next) => {
  try {
    const {
      page = 1,
      limit = 20,
      branchId,
      bookId,
      minStock,
      maxStock,
    } = req.query;

    // Build filter
    const filter = {};
    if (branchId) filter.branchId = branchId;
    if (bookId) filter.bookId = bookId;
    if (minStock !== undefined || maxStock !== undefined) {
      filter.stock = {};
      if (minStock !== undefined) filter.stock.$gte = parseInt(minStock);
      if (maxStock !== undefined) filter.stock.$lte = parseInt(maxStock);
    }

    const skip = (page - 1) * limit;
    const inventory = await Inventory.find(filter)
      .populate("branchId", "name address city")
      .populate("bookId", "title author isbn price")
      .skip(skip)
      .limit(parseInt(limit))
      .lean();

    const totalItems = await Inventory.countDocuments(filter);

    res.json({
      inventory,
      totalItems,
      currentPage: parseInt(page),
      totalPages: Math.ceil(totalItems / limit),
    });
  } catch (err) {
    next(err);
  }
};

// Get inventory by ID
const getInventoryById = async (req, res, next) => {
  try {
    const { id } = req.params;
    const inventory = await Inventory.findById(id)
      .populate("branchId")
      .populate("bookId")
      .lean();

    if (!inventory) {
      return res.status(404).json({ message: "Inventory record not found" });
    }

    res.json(inventory);
  } catch (err) {
    next(err);
  }
};

// Create or update inventory
const upsertInventory = async (req, res, next) => {
  try {
    const { branchId, bookId, stock } = req.body;

    // Validate branch and book exist
    const branch = await Branch.findById(branchId);
    if (!branch) {
      return res.status(404).json({ message: "Branch not found" });
    }

    const book = await Book.findById(bookId);
    if (!book) {
      return res.status(404).json({ message: "Book not found" });
    }

    // Check if inventory record already exists
    const existingInventory = await Inventory.findOne({ branchId, bookId });

    if (existingInventory) {
      // Update existing record
      existingInventory.stock = stock;
      await existingInventory.save();

      const updated = await Inventory.findById(existingInventory._id)
        .populate("branchId", "name address city")
        .populate("bookId", "title author isbn price");

      return res.json({
        message: "Inventory updated successfully",
        inventory: updated,
      });
    }

    // Create new record
    const inventory = await Inventory.create({ branchId, bookId, stock });
    const populated = await Inventory.findById(inventory._id)
      .populate("branchId", "name address city")
      .populate("bookId", "title author isbn price");

    res.status(201).json({
      message: "Inventory created successfully",
      inventory: populated,
    });
  } catch (err) {
    next(err);
  }
};

// Update inventory stock
const updateInventoryStock = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { stock, adjustment } = req.body;

    const inventory = await Inventory.findById(id);
    if (!inventory) {
      return res.status(404).json({ message: "Inventory record not found" });
    }

    // Either set absolute stock value or adjust by amount
    if (stock !== undefined) {
      inventory.stock = stock;
    } else if (adjustment !== undefined) {
      inventory.stock += adjustment; // Can be positive or negative
      if (inventory.stock < 0) inventory.stock = 0; // Prevent negative stock
    }

    await inventory.save();

    const updated = await Inventory.findById(id)
      .populate("branchId", "name address city")
      .populate("bookId", "title author isbn price");

    res.json({
      message: "Inventory stock updated successfully",
      inventory: updated,
    });
  } catch (err) {
    next(err);
  }
};

// Delete inventory record
const deleteInventory = async (req, res, next) => {
  try {
    const { id } = req.params;
    const inventory = await Inventory.findByIdAndDelete(id);

    if (!inventory) {
      return res.status(404).json({ message: "Inventory record not found" });
    }

    res.json({ message: "Inventory record deleted successfully" });
  } catch (err) {
    next(err);
  }
};

// Get low stock items across all branches
const getLowStockItems = async (req, res, next) => {
  try {
    const { threshold = 10, branchId } = req.query;

    const filter = { stock: { $lte: parseInt(threshold), $gt: 0 } };
    if (branchId) filter.branchId = branchId;

    const lowStockItems = await Inventory.find(filter)
      .populate("branchId", "name address city")
      .populate("bookId", "title author isbn price")
      .sort({ stock: 1 })
      .lean();

    res.json({
      threshold: parseInt(threshold),
      totalLowStockItems: lowStockItems.length,
      items: lowStockItems,
    });
  } catch (err) {
    next(err);
  }
};

// Get out of stock items
const getOutOfStockItems = async (req, res, next) => {
  try {
    const { branchId } = req.query;

    const filter = { stock: 0 };
    if (branchId) filter.branchId = branchId;

    const outOfStockItems = await Inventory.find(filter)
      .populate("branchId", "name address city")
      .populate("bookId", "title author isbn price")
      .lean();

    res.json({
      totalOutOfStockItems: outOfStockItems.length,
      items: outOfStockItems,
    });
  } catch (err) {
    next(err);
  }
};

// Bulk update inventory (for multiple books/branches)
const bulkUpdateInventory = async (req, res, next) => {
  try {
    const { updates } = req.body; // Array of {branchId, bookId, stock}

    if (!Array.isArray(updates) || updates.length === 0) {
      return res.status(400).json({ message: "Updates array is required" });
    }

    const results = {
      created: [],
      updated: [],
      errors: [],
    };

    for (const update of updates) {
      try {
        const { branchId, bookId, stock } = update;

        // Validate branch and book
        const branch = await Branch.findById(branchId);
        const book = await Book.findById(bookId);

        if (!branch || !book) {
          results.errors.push({
            branchId,
            bookId,
            error: !branch ? "Branch not found" : "Book not found",
          });
          continue;
        }

        // Upsert inventory
        const inventory = await Inventory.findOneAndUpdate(
          { branchId, bookId },
          { stock },
          { new: true, upsert: true, runValidators: true }
        );

        if (inventory.isNew) {
          results.created.push(inventory);
        } else {
          results.updated.push(inventory);
        }
      } catch (err) {
        results.errors.push({
          branchId: update.branchId,
          bookId: update.bookId,
          error: err.message,
        });
      }
    }

    res.json({
      message: "Bulk update completed",
      summary: {
        totalProcessed: updates.length,
        created: results.created.length,
        updated: results.updated.length,
        errors: results.errors.length,
      },
      results,
    });
  } catch (err) {
    next(err);
  }
};

// Transfer stock between branches
const transferStock = async (req, res, next) => {
  try {
    const { bookId, fromBranchId, toBranchId, quantity } = req.body;

    if (!bookId || !fromBranchId || !toBranchId || !quantity) {
      return res.status(400).json({ message: "Missing required fields" });
    }

    if (quantity <= 0) {
      return res
        .status(400)
        .json({ message: "Quantity must be greater than 0" });
    }

    if (fromBranchId === toBranchId) {
      return res
        .status(400)
        .json({ message: "Cannot transfer to the same branch" });
    }

    // Get source inventory
    const sourceInventory = await Inventory.findOne({
      branchId: fromBranchId,
      bookId,
    });

    if (!sourceInventory) {
      return res.status(404).json({ message: "Source inventory not found" });
    }

    if (sourceInventory.stock < quantity) {
      return res.status(400).json({
        message: "Insufficient stock in source branch",
        available: sourceInventory.stock,
        requested: quantity,
      });
    }

    // Deduct from source
    sourceInventory.stock -= quantity;
    await sourceInventory.save();

    // Add to destination
    let destInventory = await Inventory.findOne({
      branchId: toBranchId,
      bookId,
    });

    if (destInventory) {
      destInventory.stock += quantity;
      await destInventory.save();
    } else {
      destInventory = await Inventory.create({
        branchId: toBranchId,
        bookId,
        stock: quantity,
      });
    }

    const sourcePopulated = await Inventory.findById(sourceInventory._id)
      .populate("branchId", "name")
      .populate("bookId", "title");

    const destPopulated = await Inventory.findById(destInventory._id)
      .populate("branchId", "name")
      .populate("bookId", "title");

    res.json({
      message: "Stock transferred successfully",
      transfer: {
        book: sourcePopulated.bookId.title,
        quantity,
        from: {
          branch: sourcePopulated.branchId.name,
          remainingStock: sourcePopulated.stock,
        },
        to: {
          branch: destPopulated.branchId.name,
          newStock: destPopulated.stock,
        },
      },
    });
  } catch (err) {
    next(err);
  }
};

module.exports = {
  getInventory,
  getInventoryById,
  upsertInventory,
  updateInventoryStock,
  deleteInventory,
  getLowStockItems,
  getOutOfStockItems,
  bulkUpdateInventory,
  transferStock,
};
