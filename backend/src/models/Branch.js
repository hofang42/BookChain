const mongoose = require("mongoose");
const { Schema } = mongoose;

const branchSchema = new Schema(
  {
    name: { type: String, required: true },
    address: { type: String },
    city: { type: String },
    phone: { type: String },
    managerId: { type: Schema.Types.ObjectId, ref: "User" },
    // Map coordinates
    location: {
      type: {
        type: String,
        enum: ["Point"],
        default: "Point",
      },
      coordinates: {
        type: [Number], // [longitude, latitude]
        default: [0, 0],
      },
    },
    // Additional info
    openingHours: { type: String }, // e.g., "8:00 AM - 10:00 PM"
    isActive: { type: Boolean, default: true },
  },
  { timestamps: true }
);

// Create geospatial index for location-based queries
branchSchema.index({ location: "2dsphere" });

module.exports = mongoose.model("Branch", branchSchema);
