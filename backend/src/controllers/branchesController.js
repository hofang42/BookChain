const Branch = require("../models/Branch");

const getBranches = async (req, res, next) => {
  try {
    const branches = await Branch.find().lean();
    res.json(branches);
  } catch (err) {
    next(err);
  }
};

module.exports = { getBranches };
