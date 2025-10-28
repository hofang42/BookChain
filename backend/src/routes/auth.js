const express = require("express");
const router = express.Router();

// Import controllers
const {
  register,
  login,
  getMe,
  sendVerificationOTP,
  verifyEmail,
  forgotPassword,
  sendPasswordResetOTPController,
  verifyPasswordResetOTP,
  resetPassword,
  changePassword,
  updateProfile,
  logout
} = require("../controllers/authController");

// Import middleware
const { authenticate } = require("../middleware/auth");
const { emailHealthCheck, emailHealthRoute } = require("../middleware/emailHealthCheck");

// Import validation
const {
  validateRegister,
  validateLogin,
  validateForgotPassword,
  validateResetPassword,
  validateChangePassword,
  validateUpdateProfile
} = require("../utils/validation");

/**
 * @route   GET /api/auth/health/email
 * @desc    Check email service health
 * @access  Public
 */
router.get("/health/email", emailHealthRoute);

/**
 * @route   POST /api/auth/register
 * @desc    Register a new user
 * @access  Public
 */
router.post("/register", emailHealthCheck, validateRegister, register);

/**
 * @route   POST /api/auth/login
 * @desc    Login user
 * @access  Public
 */
router.post("/login", validateLogin, login);

/**
 * @route   GET /api/auth/me
 * @desc    Get current user profile
 * @access  Private
 */
router.get("/me", authenticate, getMe);

/**
 * @route   GET /api/auth/test
 * @desc    Test endpoint
 * @access  Public
 */
router.get("/test", (req, res) => {
  res.json({
    success: true,
    message: "Auth API is working!",
    timestamp: new Date().toISOString()
  });
});

/**
 * @route   POST /api/auth/test-otp
 * @desc    Test OTP generation without email
 * @access  Public
 */
router.post("/test-otp", (req, res) => {
  const { email } = req.body;
  
  if (!email) {
    return res.status(400).json({
      success: false,
      error: "Email is required"
    });
  }

  // Import OTP service
  const otpService = require("../services/otpService");
  
  // Generate OTP
  const otp = otpService.generateEmailVerificationOTP(email);
  
  res.json({
    success: true,
    message: "OTP generated successfully (development mode)",
    data: {
      email: email,
      otp: otp,
      note: "In production, OTP would be sent via email"
    }
  });
});

/**
 * @route   POST /api/auth/send-verification-otp
 * @desc    Send email verification OTP
 * @access  Public
 */
router.post("/send-verification-otp", emailHealthCheck, sendVerificationOTP);

/**
 * @route   POST /api/auth/verify-email
 * @desc    Verify email with OTP
 * @access  Public
 */
router.post("/verify-email", verifyEmail);

/**
 * @route   POST /api/auth/forgot-password
 * @desc    Send password reset email
 * @access  Public
 */
router.post("/forgot-password", emailHealthCheck, validateForgotPassword, forgotPassword);

/**
 * @route   POST /api/auth/send-password-reset-otp
 * @desc    Send password reset OTP
 * @access  Public
 */
router.post("/send-password-reset-otp", emailHealthCheck, validateForgotPassword, sendPasswordResetOTPController);

/**
 * @route   POST /api/auth/verify-password-reset-otp
 * @desc    Verify password reset OTP
 * @access  Public
 */
router.post("/verify-password-reset-otp", verifyPasswordResetOTP);

/**
 * @route   POST /api/auth/reset-password
 * @desc    Reset password with token
 * @access  Public
 */
router.post("/reset-password", validateResetPassword, resetPassword);

/**
 * @route   POST /api/auth/change-password
 * @desc    Change password (for authenticated users)
 * @access  Private
 */
router.post("/change-password", authenticate, validateChangePassword, changePassword);

/**
 * @route   PUT /api/auth/profile
 * @desc    Update user profile
 * @access  Private
 */
router.put("/profile", authenticate, validateUpdateProfile, updateProfile);

/**
 * @route   POST /api/auth/logout
 * @desc    Logout user
 * @access  Private
 */
router.post("/logout", authenticate, logout);

/**
 * @route   POST /api/auth/google
 * @desc    Google Sign-In with Firebase ID token
 * @access  Public
 */
router.post("/google", require("../controllers/authController").googleSignIn);

module.exports = router;
