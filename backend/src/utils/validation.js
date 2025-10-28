const { body, validationResult } = require("express-validator");

/**
 * Validation middleware to handle validation errors
 */
const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  
  if (!errors.isEmpty()) {
    const errorMessages = errors.array().map(error => ({
      field: error.path,
      message: error.msg,
      value: error.value
    }));
    
    // Create a more user-friendly main error message
    const mainErrorMessage = errorMessages.length === 1 
      ? errorMessages[0].message
      : `Có ${errorMessages.length} lỗi trong thông tin bạn nhập`;
    
    return res.status(400).json({
      success: false,
      error: mainErrorMessage,
      details: errorMessages
    });
  }
  
  next();
};

/**
 * Validation rules for user registration
 */
const validateRegister = [
  body("username")
    .trim()
    .isLength({ min: 3, max: 30 })
    .withMessage("Tên đăng nhập phải có từ 3-30 ký tự")
    .matches(/^[a-zA-Z0-9_]+$/)
    .withMessage("Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới (_)"),
    
  body("email")
    .trim()
    .isEmail()
    .withMessage("Vui lòng nhập địa chỉ email hợp lệ (ví dụ: user@example.com)")
    .normalizeEmail(),
    
  body("password")
    .isLength({ min: 8 })
    .withMessage("Mật khẩu phải có ít nhất 8 ký tự")
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
    .withMessage("Mật khẩu phải chứa ít nhất: 1 chữ thường (a-z), 1 chữ hoa (A-Z), và 1 số (0-9)")
    .custom((value) => {
      // Additional check for common weak passwords
      const weakPasswords = ['12345678', 'password', 'Password1', 'abcd1234', 'Abcd1234'];
      if (weakPasswords.includes(value)) {
        throw new Error('Mật khẩu này quá đơn giản, vui lòng chọn mật khẩu khác');
      }
      return true;
    }),
    
  body("fullName")
    .trim()
    .isLength({ min: 2, max: 100 })
    .withMessage("Họ và tên phải có từ 2-100 ký tự")
    .matches(/^[a-zA-ZÀ-ỹ\s]+$/)
    .withMessage("Họ và tên chỉ được chứa chữ cái và khoảng trắng"),
    
  body("phone")
    .optional()
    .trim()
    .matches(/^[0-9+\-\s()]+$/)
    .withMessage("Số điện thoại không hợp lệ (chỉ chứa số, +, -, khoảng trắng, dấu ngoặc)")
    .isLength({ min: 10, max: 15 })
    .withMessage("Số điện thoại phải có từ 10-15 ký tự"),
    
  handleValidationErrors
];

/**
 * Validation rules for user login
 */
const validateLogin = [
  body("login")
    .trim()
    .notEmpty()
    .withMessage("Vui lòng nhập tên đăng nhập hoặc email"),
    
  body("password")
    .notEmpty()
    .withMessage("Vui lòng nhập mật khẩu"),
    
  handleValidationErrors
];

/**
 * Validation rules for forgot password
 */
const validateForgotPassword = [
  body("email")
    .trim()
    .isEmail()
    .withMessage("Vui lòng nhập địa chỉ email hợp lệ")
    .normalizeEmail(),
    
  handleValidationErrors
];

/**
 * Validation rules for reset password
 */
const validateResetPassword = [
  body("token")
    .notEmpty()
    .withMessage("Mã xác thực không được để trống"),
    
  body("password")
    .isLength({ min: 8 })
    .withMessage("Mật khẩu mới phải có ít nhất 8 ký tự")
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
    .withMessage("Mật khẩu mới phải chứa ít nhất: 1 chữ thường (a-z), 1 chữ hoa (A-Z), và 1 số (0-9)")
    .custom((value) => {
      const weakPasswords = ['12345678', 'password', 'Password1', 'abcd1234', 'Abcd1234'];
      if (weakPasswords.includes(value)) {
        throw new Error('Mật khẩu này quá đơn giản, vui lòng chọn mật khẩu khác');
      }
      return true;
    }),
    
  handleValidationErrors
];

/**
 * Validation rules for change password
 */
const validateChangePassword = [
  body("currentPassword")
    .notEmpty()
    .withMessage("Vui lòng nhập mật khẩu hiện tại"),
    
  body("newPassword")
    .isLength({ min: 8 })
    .withMessage("Mật khẩu mới phải có ít nhất 8 ký tự")
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
    .withMessage("Mật khẩu mới phải chứa ít nhất: 1 chữ thường (a-z), 1 chữ hoa (A-Z), và 1 số (0-9)")
    .custom((value) => {
      const weakPasswords = ['12345678', 'password', 'Password1', 'abcd1234', 'Abcd1234'];
      if (weakPasswords.includes(value)) {
        throw new Error('Mật khẩu này quá đơn giản, vui lòng chọn mật khẩu khác');
      }
      return true;
    }),
    
  body("confirmPassword")
    .custom((value, { req }) => {
      if (value !== req.body.newPassword) {
        throw new Error("Xác nhận mật khẩu không khớp với mật khẩu mới");
      }
      return true;
    }),
    
  handleValidationErrors
];

/**
 * Validation rules for updating user profile
 */
const validateUpdateProfile = [
  body("fullName")
    .optional()
    .trim()
    .isLength({ min: 2, max: 100 })
    .withMessage("Họ và tên phải có từ 2-100 ký tự")
    .matches(/^[a-zA-ZÀ-ỹ\s]+$/)
    .withMessage("Họ và tên chỉ được chứa chữ cái và khoảng trắng"),
    
  body("phone")
    .optional()
    .trim()
    .matches(/^[0-9+\-\s()]+$/)
    .withMessage("Số điện thoại không hợp lệ (chỉ chứa số, +, -, khoảng trắng, dấu ngoặc)")
    .isLength({ min: 10, max: 15 })
    .withMessage("Số điện thoại phải có từ 10-15 ký tự"),
    
  handleValidationErrors
];

module.exports = {
  validateRegister,
  validateLogin,
  validateForgotPassword,
  validateResetPassword,
  validateChangePassword,
  validateUpdateProfile,
  handleValidationErrors
};