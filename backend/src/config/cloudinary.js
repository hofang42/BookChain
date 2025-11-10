// Trong file: src/config/cloudinary.js

const cloudinary = require("cloudinary").v2;
const { CloudinaryStorage } = require("multer-storage-cloudinary");
const multer = require("multer"); // <--- THÊM DÒNG NÀY

// Cấu hình Cloudinary
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_NAME,
  api_key: process.env.CLOUDINARY_KEY,
  api_secret: process.env.CLOUDINARY_SECRET,
});

// Cấu hình Multer storage
const storage = new CloudinaryStorage({
  cloudinary: cloudinary,
  params: {
    folder: "book-covers",
    allowed_formats: ["jpg", "png", "jpeg"],
  },
});

// Chỗ này bị lỗi vì 'multer' chưa được định nghĩa
const uploadCloud = multer({ storage: storage });

module.exports = uploadCloud;
