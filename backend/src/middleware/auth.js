const jwt = require("jsonwebtoken");
const User = require("../models/User");

/**
 * JWT Authentication Middleware
 * Verifies JWT token and attaches user to request object
 */
const authenticate = async (req, res, next) => {
  try {
    const authHeader = req.header("Authorization");
    
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      return res.status(401).json({
        success: false,
        error: "Access denied. No token provided or invalid format."
      });
    }

    const token = authHeader.substring(7); // Remove "Bearer " prefix
    
    if (!token) {
      return res.status(401).json({
        success: false,
        error: "Access denied. No token provided."
      });
    }

    // Verify token
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    
    // Get user from database
    const user = await User.findById(decoded.userId).select("-passwordHash -passwordResetToken");
    
    if (!user) {
      return res.status(401).json({
        success: false,
        error: "Invalid token. User not found."
      });
    }

    if (user.status !== "active") {
      let message = "Account access denied.";
      if (user.status === "pending") {
        message = "Please verify your email first.";
      } else if (user.status === "banned") {
        message = "Account has been banned.";
      }
      
      return res.status(401).json({
        success: false,
        error: message
      });
    }

    // Attach user to request
    req.user = user;
    next();
  } catch (error) {
    if (error.name === "JsonWebTokenError") {
      return res.status(401).json({
        success: false,
        error: "Invalid token."
      });
    }
    
    if (error.name === "TokenExpiredError") {
      return res.status(401).json({
        success: false,
        error: "Token expired."
      });
    }

    res.status(500).json({
      success: false,
      error: "Server error during authentication."
    });
  }
};

/**
 * Role-based authorization middleware
 * @param {Array} roles - Array of allowed roles
 */
const authorize = (...roles) => {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        error: "Access denied. Authentication required."
      });
    }

    if (!roles.includes(req.user.role)) {
      return res.status(403).json({
        success: false,
        error: "Access denied. Insufficient permissions."
      });
    }

    next();
  };
};

/**
 * Optional authentication middleware
 * Attaches user if token is valid, but doesn't require authentication
 */
const optionalAuth = async (req, res, next) => {
  try {
    const authHeader = req.header("Authorization");
    
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      return next();
    }

    const token = authHeader.substring(7);
    
    if (!token) {
      return next();
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    const user = await User.findById(decoded.userId).select("-passwordHash -passwordResetToken");
    
    if (user && user.status === "active") {
      req.user = user;
    }
    
    next();
  } catch (error) {
    // If token is invalid, just continue without user
    next();
  }
};

module.exports = {
  authenticate,
  authorize,
  optionalAuth
};
