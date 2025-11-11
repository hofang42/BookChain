const Branch = require("../models/Branch");
const Inventory = require("../models/Inventory");

// Get all branches
const getBranches = async (req, res, next) => {
  try {
    const { includeInactive } = req.query;
    const filter = includeInactive === "true" ? {} : { isActive: true };

    const branches = await Branch.find(filter)
      .populate("managerId", "name email")
      .lean();
    res.json(branches);
  } catch (err) {
    next(err);
  }
};

// Get branch by ID
const getBranchById = async (req, res, next) => {
  try {
    const { id } = req.params;
    const branch = await Branch.findById(id)
      .populate("managerId", "name email")
      .lean();

    if (!branch) {
      return res.status(404).json({ message: "Branch not found" });
    }

    res.json(branch);
  } catch (err) {
    next(err);
  }
};

// Create new branch
const createBranch = async (req, res, next) => {
  try {
    const {
      name,
      address,
      city,
      phone,
      managerId,
      latitude,
      longitude,
      openingHours,
      isActive,
    } = req.body;

    const branchData = {
      name,
      address,
      city,
      phone,
      managerId,
      openingHours,
      isActive,
    };

    // Add location if coordinates are provided
    if (latitude !== undefined && longitude !== undefined) {
      branchData.location = {
        type: "Point",
        coordinates: [longitude, latitude], // GeoJSON format: [lng, lat]
      };
    }

    const branch = await Branch.create(branchData);
    res.status(201).json(branch);
  } catch (err) {
    next(err);
  }
};

// Update branch
const updateBranch = async (req, res, next) => {
  try {
    const { id } = req.params;
    const {
      name,
      address,
      city,
      phone,
      managerId,
      latitude,
      longitude,
      openingHours,
      isActive,
    } = req.body;

    const updateData = {
      name,
      address,
      city,
      phone,
      managerId,
      openingHours,
      isActive,
    };

    // Update location if coordinates are provided
    if (latitude !== undefined && longitude !== undefined) {
      updateData.location = {
        type: "Point",
        coordinates: [longitude, latitude],
      };
    }

    const branch = await Branch.findByIdAndUpdate(id, updateData, {
      new: true,
      runValidators: true,
    }).populate("managerId", "name email");

    if (!branch) {
      return res.status(404).json({ message: "Branch not found" });
    }

    res.json(branch);
  } catch (err) {
    next(err);
  }
};

// Delete branch
const deleteBranch = async (req, res, next) => {
  try {
    const { id } = req.params;
    const branch = await Branch.findByIdAndDelete(id);

    if (!branch) {
      return res.status(404).json({ message: "Branch not found" });
    }

    // Also delete all inventory records for this branch
    await Inventory.deleteMany({ branchId: id });

    res.json({ message: "Branch deleted successfully" });
  } catch (err) {
    next(err);
  }
};

// Get inventory for a specific branch
const getBranchInventory = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { page = 1, limit = 20, search } = req.query;

    const branch = await Branch.findById(id);
    if (!branch) {
      return res.status(404).json({ message: "Branch not found" });
    }

    // Build inventory query
    const inventoryQuery = { branchId: id };

    // Get all inventory items for this branch with book details
    const query = Inventory.find(inventoryQuery).populate("bookId");

    // If search is provided, filter by book title
    if (search) {
      const inventoryItems = await query.lean();
      const filtered = inventoryItems.filter((item) =>
        item.bookId?.title?.toLowerCase().includes(search.toLowerCase())
      );

      const startIndex = (page - 1) * limit;
      const endIndex = startIndex + parseInt(limit);
      const paginatedItems = filtered.slice(startIndex, endIndex);

      return res.json({
        branch,
        inventory: paginatedItems,
        totalItems: filtered.length,
        currentPage: parseInt(page),
        totalPages: Math.ceil(filtered.length / limit),
      });
    }

    // Regular pagination
    const skip = (page - 1) * limit;
    const inventory = await query.skip(skip).limit(parseInt(limit)).lean();
    const totalItems = await Inventory.countDocuments(inventoryQuery);

    res.json({
      branch,
      inventory,
      totalItems,
      currentPage: parseInt(page),
      totalPages: Math.ceil(totalItems / limit),
    });
  } catch (err) {
    next(err);
  }
};

// Find nearest branches by coordinates
const getNearestBranches = async (req, res, next) => {
  try {
    const { latitude, longitude, maxDistance = 10000 } = req.query; // maxDistance in meters, default 10km

    if (!latitude || !longitude) {
      return res
        .status(400)
        .json({ message: "Latitude and longitude are required" });
    }

    const branches = await Branch.find({
      isActive: true,
      location: {
        $near: {
          $geometry: {
            type: "Point",
            coordinates: [parseFloat(longitude), parseFloat(latitude)],
          },
          $maxDistance: parseInt(maxDistance),
        },
      },
    })
      .populate("managerId", "name email")
      .lean();

    res.json(branches);
  } catch (err) {
    next(err);
  }
};

// Check book availability across branches
const checkBookAvailability = async (req, res, next) => {
  try {
    const { bookId } = req.params;
    const { latitude, longitude } = req.query;

    // Get all inventory records for this book
    const inventoryQuery = Inventory.find({ bookId })
      .populate("branchId")
      .lean();

    const inventoryItems = await inventoryQuery;

    // Filter only active branches with stock > 0
    let availability = inventoryItems
      .filter((item) => item.branchId?.isActive && item.stock > 0)
      .map((item) => ({
        branchId: item.branchId._id,
        branchName: item.branchId.name,
        address: item.branchId.address,
        city: item.branchId.city,
        phone: item.branchId.phone,
        stock: item.stock,
        location: item.branchId.location,
        openingHours: item.branchId.openingHours,
      }));

    // If user location is provided, calculate distance and sort by nearest
    if (latitude && longitude) {
      const userLng = parseFloat(longitude);
      const userLat = parseFloat(latitude);

      availability = availability.map((branch) => {
        const branchLng = branch.location.coordinates[0];
        const branchLat = branch.location.coordinates[1];
        const distance = calculateDistance(
          userLat,
          userLng,
          branchLat,
          branchLng
        );
        return { ...branch, distance: distance.toFixed(2) }; // distance in km
      });

      // Sort by distance
      availability.sort(
        (a, b) => parseFloat(a.distance) - parseFloat(b.distance)
      );
    }

    res.json({
      bookId,
      totalBranchesWithStock: availability.length,
      branches: availability,
    });
  } catch (err) {
    next(err);
  }
};

// Helper function to calculate distance between two coordinates (Haversine formula)
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371; // Radius of the Earth in km
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = R * c;
  return distance;
}

module.exports = {
  getBranches,
  getBranchById,
  createBranch,
  updateBranch,
  deleteBranch,
  getBranchInventory,
  getNearestBranches,
  checkBookAvailability,
};
