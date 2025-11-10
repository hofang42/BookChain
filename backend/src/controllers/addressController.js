const Address = require("../models/Address");

/**
 * Get all addresses for the authenticated user
 * GET /api/addresses
 */
const getUserAddresses = async (req, res, next) => {
  try {
    const userId = req.user._id;

    const addresses = await Address.find({ userId })
      .sort({ isDefault: -1, createdAt: -1 })
      .lean();

    res.json({
      success: true,
      data: { addresses },
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get a single address by ID
 * GET /api/addresses/:id
 */
const getAddressById = async (req, res, next) => {
  try {
    const { id } = req.params;
    const userId = req.user._id;

    const address = await Address.findOne({ _id: id, userId }).lean();

    if (!address) {
      return res.status(404).json({
        success: false,
        error: "Address not found",
      });
    }

    res.json({
      success: true,
      data: { address },
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Create a new address
 * POST /api/addresses
 */
const createAddress = async (req, res, next) => {
  try {
    const userId = req.user._id;
    const {
      recipientName,
      phoneNumber,
      street,
      ward,
      district,
      city,
      postalCode,
      isDefault,
    } = req.body;

    // Validate required fields
    if (!recipientName || !phoneNumber || !street || !district || !city) {
      return res.status(400).json({
        success: false,
        error: "Missing required fields",
        details: [
          {
            field: "address",
            message: "Recipient name, phone, street, district, and city are required",
          },
        ],
      });
    }

    // If this is the first address or marked as default, ensure no other default exists
    const existingAddressCount = await Address.countDocuments({ userId });
    const shouldBeDefault = isDefault || existingAddressCount === 0;

    if (shouldBeDefault) {
      // Unset all other default addresses
      await Address.updateMany({ userId }, { isDefault: false });
    }

    // Create new address
    const address = new Address({
      userId,
      recipientName,
      phoneNumber,
      street,
      ward,
      district,
      city,
      postalCode,
      isDefault: shouldBeDefault,
    });

    await address.save();

    res.status(201).json({
      success: true,
      message: "Address created successfully",
      data: { address },
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update an existing address
 * PUT /api/addresses/:id
 */
const updateAddress = async (req, res, next) => {
  try {
    const { id } = req.params;
    const userId = req.user._id;
    const {
      recipientName,
      phoneNumber,
      street,
      ward,
      district,
      city,
      postalCode,
      isDefault,
    } = req.body;

    // Find address and verify ownership
    const address = await Address.findOne({ _id: id, userId });

    if (!address) {
      return res.status(404).json({
        success: false,
        error: "Address not found",
      });
    }

    // Update fields
    if (recipientName !== undefined) address.recipientName = recipientName;
    if (phoneNumber !== undefined) address.phoneNumber = phoneNumber;
    if (street !== undefined) address.street = street;
    if (ward !== undefined) address.ward = ward;
    if (district !== undefined) address.district = district;
    if (city !== undefined) address.city = city;
    if (postalCode !== undefined) address.postalCode = postalCode;

    // Handle isDefault
    if (isDefault !== undefined) {
      if (isDefault) {
        // If setting as default, unset all other defaults
        await Address.updateMany(
          { userId, _id: { $ne: id } },
          { isDefault: false }
        );
      }
      address.isDefault = isDefault;
    }

    await address.save();

    res.json({
      success: true,
      message: "Address updated successfully",
      data: { address },
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Delete an address
 * DELETE /api/addresses/:id
 */
const deleteAddress = async (req, res, next) => {
  try {
    const { id } = req.params;
    const userId = req.user._id;

    // Find and delete address
    const address = await Address.findOneAndDelete({ _id: id, userId });

    if (!address) {
      return res.status(404).json({
        success: false,
        error: "Address not found",
      });
    }

    // If deleted address was default, set another address as default
    if (address.isDefault) {
      const firstAddress = await Address.findOne({ userId }).sort({
        createdAt: -1,
      });
      if (firstAddress) {
        firstAddress.isDefault = true;
        await firstAddress.save();
      }
    }

    res.json({
      success: true,
      message: "Address deleted successfully",
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Set an address as default
 * PUT /api/addresses/:id/default
 */
const setDefaultAddress = async (req, res, next) => {
  try {
    const { id } = req.params;
    const userId = req.user._id;

    // Find address and verify ownership
    const address = await Address.findOne({ _id: id, userId });

    if (!address) {
      return res.status(404).json({
        success: false,
        error: "Address not found",
      });
    }

    // Use static method to set as default
    await Address.setAsDefault(id, userId);

    // Fetch updated address
    const updatedAddress = await Address.findById(id);

    res.json({
      success: true,
      message: "Default address updated successfully",
      data: { address: updatedAddress },
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get default address for the authenticated user
 * GET /api/addresses/default
 */
const getDefaultAddress = async (req, res, next) => {
  try {
    const userId = req.user._id;

    const address = await Address.findOne({ userId, isDefault: true }).lean();

    if (!address) {
      return res.status(404).json({
        success: false,
        error: "No default address found",
      });
    }

    res.json({
      success: true,
      data: { address },
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getUserAddresses,
  getAddressById,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  getDefaultAddress,
};
