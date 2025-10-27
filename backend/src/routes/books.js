const express = require("express");
const router = express.Router();
const { getBooks } = require("../controllers/booksController");

router.get("/", getBooks);

module.exports = router;
