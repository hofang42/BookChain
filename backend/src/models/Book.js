const mongoose = require("mongoose");
const { Schema } = mongoose;

const bookSchema = new Schema(
  {
    title: { type: String, required: true, index: true },
    author: { type: String },
    publisher: { type: String },
    publishedYear: { type: Number },
    categoryId: { type: Schema.Types.ObjectId, ref: "Category" },
    isbn: { type: String, index: true },
    bookLanguage: { type: String },
    price: { type: Number, required: true },
    discount: { type: Number, default: 0 },
    rating: { type: Number, default: 0 },
    salesCount: { type: Number, default: 0 },
    description: { type: String },
    coverImage: { type: String },
  },
  { timestamps: true }
);

bookSchema.index({ title: "text", description: "text", author: "text" });

module.exports = mongoose.model("Book", bookSchema);
