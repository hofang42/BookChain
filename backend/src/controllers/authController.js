const jwt = require("jsonwebtoken");
const crypto = require("crypto");
const User = require("../models/User");
const { sendEmailVerificationOTP, sendWelcomeEmail, /* sendPasswordResetEmail, */ sendPasswordResetOTP } = require("../services/emailService");
const otpService = require("../services/otpService");

/**
 * Generate JWT token
 * @param {string} userId - User ID
 * @returns {string} JWT token
 */
const generateToken = (userId) => {
  return jwt.sign(
    { userId },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRES_IN || "7d" }
  );
};

/**
 * Register a new user
 * POST /api/auth/register
 */
const register = async (req, res, next) => {
  try {
    const { username, email, password, fullName, phone } = req.body;

    // Check if user already exists
    const existingUser = await User.findOne({
      $or: [{ email }, { username }]
    });

    if (existingUser) {
      // Handle email conflict (both pending and active)
      if (existingUser.email === email) {
        return res.status(400).json({
          success: false,
          error: "Email này đã được đăng ký. Vui lòng sử dụng email khác hoặc đăng nhập.",
          details: [{
            field: "email",
            message: "Email này đã được đăng ký",
            value: email
          }]
        });
      }
      
      // Handle username conflict
      if (existingUser.username === username) {
        return res.status(400).json({
          success: false,
          error: "Tên đăng nhập này đã được sử dụng. Vui lòng chọn tên khác.",
          details: [{
            field: "username", 
            message: "Tên đăng nhập này đã được sử dụng",
            value: username
          }]
        });
      }
    }

    // Create new user
    const user = new User({
      username,
      email,
      passwordHash: password, // Will be hashed by pre-save middleware
      fullName,
      phone
    });

    await user.save();

    // Generate and send OTP for new user with rate limiting
    const otpResult = otpService.generateEmailVerificationOTP(email);
    
    if (!otpResult.success) {
      return res.status(429).json({
        success: false,
        error: otpResult.error,
        remainingTime: otpResult.remainingTime
      });
    }
    
    try {
      // Send verification email with timeout
      const emailPromise = sendEmailVerificationOTP(user.email, otpResult.otp, user.fullName);
      const timeoutPromise = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Email sending timeout')), 30000)
      );
      
      const emailResult = await Promise.race([emailPromise, timeoutPromise]);
      
      // Check if email sending failed
      if (emailResult && !emailResult.success) {
        throw new Error(emailResult.message || 'Failed to send email');
      }
      
      res.status(201).json({
        success: true,
        requiresVerification: true,
        message: "User registered successfully. Verification code sent to your email.",
        data: {
          email: user.email,
          source: "register"
        }
      });
    } catch (emailError) {
      console.error(`Failed to send email to ${user.email}:`, emailError.message);
      
      // Clear OTP if email fails
      otpService.clearOTP(email);

      return res.status(500).json({
        success: false,
        error: "User registered but failed to send verification email. Please try again."
      });
    }
  } catch (error) {
    next(error);
  }
};

/**
 * Login user
 * POST /api/auth/login
 */
const login = async (req, res, next) => {
  try {
    const { login, password } = req.body;

    // Find user by username or email (including pending status)
    const user = await User.findOne({
      $or: [
        { email: login },
        { username: login }
      ]
    });

    if (!user) {
      return res.status(401).json({
        success: false,
        error: "Tên đăng nhập/email hoặc mật khẩu không đúng",
        details: [{
          field: "login",
          message: "Không tìm thấy tài khoản với thông tin này",
          value: login
        }]
      });
    }

    // Handle pending account - send OTP and redirect to verification
    if (user.status === "pending") {
      // Verify password first for security
      const isPasswordValid = await user.comparePassword(password);
      
      if (!isPasswordValid) {
        return res.status(401).json({
          success: false,
          error: "Tên đăng nhập/email hoặc mật khẩu không đúng",
          details: [{
            field: "password",
            message: "Mật khẩu không chính xác",
            value: "***"
          }]
        });
      }

      // Generate and send OTP for pending account with rate limiting
      const otpResult = otpService.generateEmailVerificationOTP(user.email);
      
      if (!otpResult.success) {
        return res.status(429).json({
          success: false,
          error: otpResult.error,
          remainingTime: otpResult.remainingTime
        });
      }
      
      try {
        // Send verification email with timeout
        const emailPromise = sendEmailVerificationOTP(user.email, otpResult.otp, user.fullName);
        const timeoutPromise = new Promise((_, reject) => 
          setTimeout(() => reject(new Error('Email sending timeout')), 30000)
        );
        
        const emailResult = await Promise.race([emailPromise, timeoutPromise]);
        
        // Check if email sending failed
        if (emailResult && !emailResult.success) {
          throw new Error(emailResult.message || 'Failed to send email');
        }
        
        return res.json({
          success: true,
          message: "Tài khoản chưa được xác thực. Mã OTP đã được gửi đến email của bạn.",
          data: {
            email: user.email,
            source: "login",
            requiresVerification: true
          }
        });
      } catch (emailError) {
        console.error(`Failed to send email to ${user.email}:`, emailError.message);
        
        // Clear OTP if email fails
        otpService.clearOTP(user.email);

        return res.status(500).json({
          success: false,
          error: "Failed to send verification email. Please try again."
        });
      }
    }

    // Handle banned account
    if (user.status === "banned") {
      return res.status(401).json({
        success: false,
        error: "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ hỗ trợ.",
        details: [{
          field: "login",
          message: "Tài khoản bị khóa",
          value: login
        }]
      });
    }

    // Check password
    const isPasswordValid = await user.comparePassword(password);
    
    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        error: "Tên đăng nhập/email hoặc mật khẩu không đúng",
        details: [{
          field: "password",
          message: "Mật khẩu không chính xác",
          value: "***"
        }]
      });
    }

    // Update last login
    user.lastLogin = new Date();
    await user.save();

    // Generate token
    const token = generateToken(user._id);

    // Return user data (excluding sensitive fields)
    const userData = {
      id: user._id,
      username: user.username,
      email: user.email,
      fullName: user.fullName,
      phone: user.phone,
      role: user.role,
      status: user.status,
      lastLogin: user.lastLogin
    };

    res.json({
      success: true,
      message: "Login successful",
      data: {
        user: userData,
        token
      }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get current user profile
 * GET /api/auth/me
 */
const getMe = async (req, res, next) => {
  try {
    const userData = {
      id: req.user._id,
      username: req.user.username,
      email: req.user.email,
      fullName: req.user.fullName,
      phone: req.user.phone,
      role: req.user.role,
      isEmailVerified: req.user.isEmailVerified,
      lastLogin: req.user.lastLogin,
      createdAt: req.user.createdAt,
      updatedAt: req.user.updatedAt
    };

    res.json({
      success: true,
      data: { user: userData }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Send email verification OTP
 * POST /api/auth/send-verification-otp
 */
const sendVerificationOTP = async (req, res, next) => {
  try {
    const { email } = req.body;

    if (!email) {
      return res.status(400).json({
        success: false,
        error: "Email is required"
      });
    }

    // Find user (allow both pending and active status for resend)
    const user = await User.findOne({ email });

    if (!user) {
      return res.status(404).json({
        success: false,
        error: "User not found. Please register first."
      });
    }

    // If user is already active, don't send OTP
    if (user.status === "active") {
      return res.status(400).json({
        success: false,
        error: "Email already verified. You can login now."
      });
    }

    // Generate OTP with rate limiting
    const otpResult = otpService.generateEmailVerificationOTP(email);

    if (!otpResult.success) {
      return res.status(429).json({
        success: false,
        error: otpResult.error,
        remainingTime: otpResult.remainingTime
      });
    }

    try {
      // Send verification email with timeout
      const emailPromise = sendEmailVerificationOTP(user.email, otpResult.otp, user.fullName);
      const timeoutPromise = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Email sending timeout')), 30000)
      );
      
      const emailResult = await Promise.race([emailPromise, timeoutPromise]);
      
      // Check if email sending failed
      if (emailResult && !emailResult.success) {
        throw new Error(emailResult.message || 'Failed to send email');
      }
      
      res.json({
        success: true,
        message: "Verification code sent to your email"
      });
    } catch (emailError) {
      console.error(`Failed to send email to ${user.email}:`, emailError.message);
      
      // Clear OTP if email fails
      otpService.clearOTP(email);

      return res.status(500).json({
        success: false,
        error: "Failed to send verification email. Please try again."
      });
    }
  } catch (error) {
    next(error);
  }
};

/**
 * Verify email with OTP
 * POST /api/auth/verify-email
 */
const verifyEmail = async (req, res, next) => {
  try {
    const { email, otp } = req.body;

    if (!email || !otp) {
      return res.status(400).json({
        success: false,
        error: "Email and OTP are required"
      });
    }

    // Find user with pending status
    const user = await User.findOne({ email, status: "pending" });

    if (!user) {
      return res.status(404).json({
        success: false,
        error: "User not found or already verified"
      });
    }

    // Verify OTP
    const isValidOTP = otpService.verifyEmailVerificationOTP(email, otp);

    if (!isValidOTP) {
      return res.status(400).json({
        success: false,
        error: "Invalid or expired verification code"
      });
    }

    // Update user status to active
    user.status = "active";
    await user.save();

    // Send welcome email (non-blocking)
    sendWelcomeEmail(user.email, user.fullName)
      .catch(error => console.error("Failed to send welcome email:", error));

    // Generate token
    const token = generateToken(user._id);

    // Return user data
    const userData = {
      id: user._id,
      username: user.username,
      email: user.email,
      fullName: user.fullName,
      phone: user.phone,
      role: user.role,
      status: user.status,
      createdAt: user.createdAt
    };

    res.json({
      success: true,
      message: "Email verified successfully",
      data: {
        user: userData,
        token
      }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * DEPRECATED: Forgot password - send reset email (token-based)
 * POST /api/auth/forgot-password
 * 
 * Hàm này không còn được sử dụng. Hệ thống hiện tại sử dụng OTP-based reset thay vì token-based.
 * Giữ lại để tham khảo trong tương lai nếu cần.
 */
/*
const forgotPassword = async (req, res, next) => {
  try {
    const { email } = req.body;

    const user = await User.findOne({ email, isActive: true });

    if (!user) {
      // Don't reveal if email exists or not for security
      return res.json({
        success: true,
        message: "If the email exists, a password reset link has been sent"
      });
    }

    // Generate reset token
    const resetToken = user.createPasswordResetToken();
    await user.save({ validateBeforeSave: false });

    try {
      // Send reset email
      const emailResult = await sendPasswordResetEmail(user.email, resetToken, user.fullName);
      
      // Check if email sending failed
      if (emailResult && !emailResult.success) {
        throw new Error(emailResult.message || 'Failed to send password reset email');
      }

      res.json({
        success: true,
        message: "Password reset email sent successfully"
      });
    } catch (emailError) {
      // Reset the token fields if email fails
      user.passwordResetToken = undefined;
      user.passwordResetExpires = undefined;
      await user.save({ validateBeforeSave: false });

      return res.status(500).json({
        success: false,
        error: "Failed to send password reset email. Please try again."
      });
    }
  } catch (error) {
    next(error);
  }
};
*/

/**
 * Send password reset OTP
 * POST /api/auth/send-password-reset-otp
 */
const sendPasswordResetOTPController = async (req, res, next) => {
  try {
    const { email } = req.body;

    if (!email) {
      return res.status(400).json({
        success: false,
        error: "Email is required"
      });
    }

    // Find user (không cần check status active vì user có thể chưa verify email nhưng vẫn cần reset password)
    const user = await User.findOne({ 
      email
    });

    if (!user) {
      return res.status(404).json({
        success: false,
        error: "Email not found in our system"
      });
    }

    // Generate OTP
    const otp = otpService.generatePasswordResetOTP(email);

    try {
      // Send OTP email with timeout
      const emailPromise = sendPasswordResetOTP(user.email, otp, user.fullName);
      const timeoutPromise = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Email sending timeout')), 30000)
      );
      
      const emailResult = await Promise.race([emailPromise, timeoutPromise]);
      
      // Check if email sending failed
      if (emailResult && !emailResult.success) {
        throw new Error(emailResult.message || 'Failed to send password reset email');
      }

      res.json({
        success: true,
        message: "Password reset code sent to your email"
      });
    } catch (emailError) {
      console.error(`Failed to send password reset OTP to ${user.email}:`, emailError.message);
      
      // Clear OTP if email fails
      otpService.clearOTP(email);

      return res.status(500).json({
        success: false,
        error: "Failed to send password reset email. Please try again."
      });
    }
  } catch (error) {
    next(error);
  }
};

/**
 * Verify password reset OTP
 * POST /api/auth/verify-password-reset-otp
 */
const verifyPasswordResetOTP = async (req, res, next) => {
  try {
    const { email, otp } = req.body;

    if (!email || !otp) {
      return res.status(400).json({
        success: false,
        error: "Email and OTP are required"
      });
    }

    // Find user (không cần check status active vì user có thể chưa verify email nhưng vẫn cần reset password)
    const user = await User.findOne({ 
      email
    });

    if (!user) {
      return res.status(404).json({
        success: false,
        error: "User not found"
      });
    }

    // Verify OTP
    const isValidOTP = otpService.verifyPasswordResetOTP(email, otp);

    if (!isValidOTP) {
      return res.status(400).json({
        success: false,
        error: "Invalid or expired OTP"
      });
    }

    // OTP verified successfully - user can now reset password
    res.json({
      success: true,
      message: "OTP verified successfully. You can now reset your password.",
      data: {
        email: email,
        verified: true
      }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Reset password with token
 * POST /api/auth/reset-password
 */
const resetPassword = async (req, res, next) => {
  try {
    const { email, otp, password } = req.body;

    if (!email || !otp || !password) {
      return res.status(400).json({
        success: false,
        error: "Email, OTP and password are required"
      });
    }

    // Verify OTP one more time for security
    const isValidOTP = otpService.verifyPasswordResetOTP(email, otp);
    if (!isValidOTP) {
      return res.status(400).json({
        success: false,
        error: "Invalid or expired OTP"
      });
    }

    // Find user
    const user = await User.findOne({ email });

    if (!user) {
      return res.status(404).json({
        success: false,
        error: "User not found"
      });
    }

    // Update password
    user.passwordHash = password; // Will be hashed by pre-save middleware
    await user.save();

    // Clear the OTP after successful password reset
    otpService.clearOTP(email);

    // Generate new token
    const authToken = generateToken(user._id);

    // Return user data
    const userData = {
      id: user._id,
      username: user.username,
      email: user.email,
      fullName: user.fullName,
      phone: user.phone,
      role: user.role,
      isEmailVerified: user.isEmailVerified
    };

    res.json({
      success: true,
      message: "Password reset successful",
      data: {
        user: userData,
        token: authToken
      }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Change password (for authenticated users)
 * POST /api/auth/change-password
 */
const changePassword = async (req, res, next) => {
  try {
    const { currentPassword, newPassword } = req.body;
    const user = await User.findById(req.user._id);

    // Verify current password
    const isCurrentPasswordValid = await user.comparePassword(currentPassword);
    
    if (!isCurrentPasswordValid) {
      return res.status(400).json({
        success: false,
        error: "Current password is incorrect"
      });
    }

    // Update password
    user.passwordHash = newPassword; // Will be hashed by pre-save middleware
    await user.save();

    res.json({
      success: true,
      message: "Password changed successfully"
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update user profile
 * PUT /api/auth/profile
 */
const updateProfile = async (req, res, next) => {
  try {
    const { fullName, phone } = req.body;
    const user = await User.findById(req.user._id);

    // Update fields
    if (fullName !== undefined) user.fullName = fullName;
    if (phone !== undefined) user.phone = phone;

    await user.save();

    // Return updated user data
    const userData = {
      id: user._id,
      username: user.username,
      email: user.email,
      fullName: user.fullName,
      phone: user.phone,
      avatar: user.avatar,
      role: user.role,
      status: user.status,
      lastLogin: user.lastLogin,
      updatedAt: user.updatedAt
    };

    res.json({
      success: true,
      message: "Profile updated successfully",
      data: { user: userData }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Upload user avatar
 * POST /api/auth/avatar
 */
const uploadAvatar = async (req, res, next) => {
  try {
    if (!req.file) {
      return res.status(400).json({
        success: false,
        error: "No image file provided"
      });
    }

    const user = await User.findById(req.user._id);

    // Update avatar URL
    user.avatar = req.file.path; // Cloudinary URL
    await user.save();

    res.json({
      success: true,
      message: "Avatar uploaded successfully",
      data: {
        avatar: user.avatar
      }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Logout user (client-side token removal)
 * POST /api/auth/logout
 */
const logout = async (req, res, next) => {
  try {
    // In a stateless JWT system, logout is handled client-side
    // But we can still track it server-side if needed
    
    res.json({
      success: true,
      message: "Logged out successfully"
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Google Sign-In with Firebase ID token
 * POST /api/auth/google
 */
const googleSignIn = async (req, res, next) => {
  try {
    const { idToken, email, name, uid } = req.body;

    if (!idToken || !email || !uid) {
      return res.status(400).json({
        success: false,
        error: "Firebase ID token, email, and UID are required"
      });
    }

    // TODO: Verify Firebase ID token with Firebase Admin SDK
    // For now, we'll trust the client-side verification
    
    // Check if user exists
    let user = await User.findOne({ email });

    if (!user) {
      // Create new user from Google account
      user = new User({
        username: email.split('@')[0] + '_google', // Generate username from email
        email: email,
        fullName: name || 'Google User',
        passwordHash: 'google_oauth', // Placeholder password for OAuth users
        role: 'customer',
        status: 'active', // Google users are pre-verified
        lastLogin: new Date()
      });

      await user.save();
    } else {
      // Update last login for existing user
      user.lastLogin = new Date();
      
      // If user was pending, activate account since Google has verified the email
      if (user.status === 'pending') {
        user.status = 'active';
        // Optionally update fullName if provided and different
        if (name && name !== 'Google User' && name !== user.fullName) {
          user.fullName = name;
        }
      }
      
      await user.save();
    }

    // Generate JWT token
    const token = generateToken(user._id);

    // Return user data
    const userData = {
      id: user._id,
      username: user.username,
      email: user.email,
      fullName: user.fullName,
      phone: user.phone,
      role: user.role,
      status: user.status,
      lastLogin: user.lastLogin
    };

    res.json({
      success: true,
      message: "Google sign-in successful",
      data: {
        user: userData,
        token
      }
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  register,
  login,
  getMe,
  sendVerificationOTP,
  verifyEmail,
  // forgotPassword, // DEPRECATED: Không còn sử dụng, đã chuyển sang OTP-based reset
  sendPasswordResetOTPController,
  verifyPasswordResetOTP,
  resetPassword,
  changePassword,
  updateProfile,
  uploadAvatar,
  logout,
  googleSignIn
};
