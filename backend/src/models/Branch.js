const mongoose = require("mongoose");
const { Schema } = mongoose;

const branchSchema = new Schema(
  {
    name: { type: String, required: true },
    address: { type: String },
    city: { type: String },
    phone: { type: String },
    managerId: { type: Schema.Types.ObjectId, ref: "User" },
  },
  { timestamps: true }
);

module.exports = mongoose.model("Branch", branchSchema);
