const mongoose = require("mongoose");
const { Schema } = mongoose;
const slugify = require("slugify");

const categorySchema = new Schema(
  {
    name: { type: String, required: true, index: true },
    slug: { type: String, unique: true, index: true, sparse: true },
    description: { type: String },
  },
  { timestamps: true }
);

categorySchema.pre("save", function (next) {
  if (this.isModified("name")) {
    this.slug = slugify(this.name, { lower: true, strict: true });
  }
  next();
});

module.exports = mongoose.model("Category", categorySchema);
