const Book = require("../models/Book");

const getBooks = async (req, res, next) => {
  try {
    // basic list endpoint, supports optional ?q=search
    const q = req.query.q;
    const filter = q ? { $text: { $search: q } } : {};
    const books = await Book.find(filter).limit(200).lean();
    res.json(books);
  } catch (err) {
    next(err);
  }
};

module.exports = { getBooks };
