const User = require("../models/User");

const getUsers = async (req, res, next) => {
  try {
    const users = await User.find().limit(100).lean();
    res.json(users);
  } catch (err) {
    next(err);
  }
};

module.exports = { getUsers };
