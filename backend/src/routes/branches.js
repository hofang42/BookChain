const express = require("express");
const router = express.Router();
const { getBranches } = require("../controllers/branchesController");

router.get("/", getBranches);

module.exports = router;
