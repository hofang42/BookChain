const mongoose = require("mongoose");

const connectDB = async () => {
  const uri = process.env.MONGODB_URI || "mongodb://localhost:27017/bookchain";
  const opts = {
    // Recommended mongoose options; adjust as needed
    dbName: undefined,
    autoIndex: true,
    maxPoolSize: 10,
    serverSelectionTimeoutMS: 5000,
  };

  await mongoose.connect(uri, opts);
  console.log("MongoDB connected");
};

module.exports = connectDB;
