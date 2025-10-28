/**
 * Email Health Check Middleware
 * Ensures email service is properly configured and working
 */

const { isInitialized } = require('../services/emailService');

/**
 * Middleware to check email service health before processing email-related requests
 */
const emailHealthCheck = async (req, res, next) => {
  try {
    // Check if email service is initialized
    if (!isInitialized()) {
      return res.status(503).json({
        success: false,
        message: 'Email service not initialized',
        error: 'SMTP_SERVICE_NOT_READY'
      });
    }

    next();
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Email service health check failed',
      error: 'HEALTH_CHECK_ERROR'
    });
  }
};

/**
 * Express route to check email service status
 */
const emailHealthRoute = async (req, res) => {
  try {
    if (!isInitialized()) {
      return res.status(503).json({
        success: false,
        status: 'unhealthy',
        message: 'Email service not initialized',
        timestamp: new Date().toISOString()
      });
    }
    
    res.json({
      success: true,
      status: 'healthy',
      message: 'Email service is operational',
      smtp: {
        host: process.env.EMAIL_HOST,
        port: process.env.EMAIL_PORT,
        user: process.env.EMAIL_USER,
        secure: process.env.EMAIL_PORT === '465'
      },
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    res.status(503).json({
      success: false,
      status: 'unhealthy',
      message: 'Email service connection failed',
      error: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

module.exports = {
  emailHealthCheck,
  emailHealthRoute
};
