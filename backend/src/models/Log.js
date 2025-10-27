const mongoose = require("mongoose");
const { Schema } = mongoose;

const logSchema = new Schema(
  {
    userId: { type: Schema.Types.ObjectId, ref: "User" },
    action: { type: String, required: true },
    details: { type: String },
  },
  { timestamps: true }
);

module.exports = mongoose.model("Log", logSchema);
