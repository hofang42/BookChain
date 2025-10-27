const mongoose = require("mongoose");
const { Schema } = mongoose;

const inventorySchema = new Schema(
  {
    branchId: {
      type: Schema.Types.ObjectId,
      ref: "Branch",
      required: true,
      index: true,
    },
    bookId: {
      type: Schema.Types.ObjectId,
      ref: "Book",
      required: true,
      index: true,
    },
    stock: { type: Number, default: 0 },
  },
  { timestamps: true }
);

inventorySchema.index({ branchId: 1, bookId: 1 }, { unique: true });

module.exports = mongoose.model("Inventory", inventorySchema);
