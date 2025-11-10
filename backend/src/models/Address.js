const mongoose = require("mongoose");
const { Schema } = mongoose;

const addressSchema = new Schema(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      required: true,
      index: true,
    },
    recipientName: {
      type: String,
      required: true,
      trim: true,
    },
    phoneNumber: {
      type: String,
      required: true,
      trim: true,
    },
    street: {
      type: String,
      required: true,
      trim: true,
    },
    ward: {
      type: String,
      trim: true,
    },
    district: {
      type: String,
      required: true,
      trim: true,
    },
    city: {
      type: String,
      required: true,
      trim: true,
    },
    postalCode: {
      type: String,
      trim: true,
    },
    isDefault: {
      type: Boolean,
      default: false,
    },
  },
  { timestamps: true }
);

// Index for faster queries
addressSchema.index({ userId: 1, isDefault: -1 });

// Method to get formatted address
addressSchema.methods.getFormattedAddress = function () {
  const parts = [this.street];
  if (this.ward) parts.push(this.ward);
  parts.push(this.district, this.city);
  if (this.postalCode) parts.push(this.postalCode);
  return parts.join(", ");
};

// Static method to ensure only one default address per user
addressSchema.statics.setAsDefault = async function (addressId, userId) {
  // Unset all default addresses for this user
  await this.updateMany({ userId, _id: { $ne: addressId } }, { isDefault: false });

  // Set the specified address as default
  await this.findByIdAndUpdate(addressId, { isDefault: true });
};

module.exports = mongoose.model("Address", addressSchema);
