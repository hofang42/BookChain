const mongoose = require("mongoose");
const { Schema } = mongoose;
const { removeAccents } = require("../utils/helpers");

const bookSchema = new Schema(
  {
    title: { type: String, required: true, index: true },
    quantity: { type: Number, required: true, min: 0 },
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
    title_unaccented: { type: String, index: true },
    author_unaccented: { type: String, index: true },
    description_unaccented: { type: String },
  },
  { timestamps: true }
);

bookSchema.pre("save", function (next) {
  // 'this' trỏ đến cuốn sách đang được lưu
  if (this.isModified("title")) {
    this.title_unaccented = removeAccents(this.title.toLowerCase());
  }
  if (this.isModified("author")) {
    this.author_unaccented = removeAccents(this.author.toLowerCase());
  }
  if (this.isModified("description")) {
    this.description_unaccented = removeAccents(this.description.toLowerCase());
  }
  next(); // Tiếp tục quá trình save
});

module.exports = mongoose.model("Book", bookSchema);
