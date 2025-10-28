/**
 * OTP Service - In-memory storage for OTP codes
 * Handles email verification and password reset OTP
 */

class OTPService {
  constructor() {
    // In-memory storage for OTPs
    // Format: { email: { otp: "123456", expires: timestamp, type: "email_verification" | "password_reset" } }
    this.otpStorage = new Map();
    
    // Clean expired OTPs every 5 minutes
    setInterval(() => {
      this.cleanExpiredOTPs();
    }, 5 * 60 * 1000);
  }

  /**
   * Generate 6-digit OTP
   * @returns {string} 6-digit OTP
   */
  generateOTP() {
    return Math.floor(100000 + Math.random() * 900000).toString();
  }

  /**
   * Store OTP for email verification
   * @param {string} email - User email
   * @returns {string} Generated OTP
   */
  generateEmailVerificationOTP(email) {
    const otp = this.generateOTP();
    const expires = Date.now() + 10 * 60 * 1000; // 10 minutes
    
    this.otpStorage.set(email, {
      otp,
      expires,
      type: 'email_verification'
    });
    
    return otp;
  }

  /**
   * Store OTP for password reset
   * @param {string} email - User email
   * @returns {string} Generated OTP
   */
  generatePasswordResetOTP(email) {
    const otp = this.generateOTP();
    const expires = Date.now() + 10 * 60 * 1000; // 10 minutes
    
    this.otpStorage.set(email, {
      otp,
      expires,
      type: 'password_reset'
    });
    
    return otp;
  }

  /**
   * Verify email verification OTP
   * @param {string} email - User email
   * @param {string} otp - OTP to verify
   * @returns {boolean} True if valid
   */
  verifyEmailVerificationOTP(email, otp) {
    const stored = this.otpStorage.get(email);
    
    if (!stored) {
      return false;
    }
    
    if (stored.type !== 'email_verification') {
      return false;
    }
    
    if (Date.now() > stored.expires) {
      this.otpStorage.delete(email);
      return false;
    }
    
    if (stored.otp !== otp) {
      return false;
    }
    
    this.otpStorage.delete(email); // Remove after successful verification
    return true;
  }

  /**
   * Verify password reset OTP
   * @param {string} email - User email
   * @param {string} otp - OTP to verify
   * @returns {boolean} True if valid
   */
  verifyPasswordResetOTP(email, otp) {
    const stored = this.otpStorage.get(email);
    
    if (!stored) {
      return false;
    }
    
    if (stored.type !== 'password_reset') {
      return false;
    }
    
    if (Date.now() > stored.expires) {
      this.otpStorage.delete(email);
      return false;
    }
    
    if (stored.otp !== otp) {
      return false;
    }
    
    // Don't delete OTP here - let resetPassword handle it
    return true;
  }

  /**
   * Clear OTP for specific email
   * @param {string} email - User email
   */
  clearOTP(email) {
    this.otpStorage.delete(email);
  }

  /**
   * Clean expired OTPs from memory
   */
  cleanExpiredOTPs() {
    const now = Date.now();
    let cleanedCount = 0;
    
    for (const [email, data] of this.otpStorage.entries()) {
      if (now > data.expires) {
        this.otpStorage.delete(email);
        cleanedCount++;
      }
    }
    
    // Silently clean expired OTPs
  }

  /**
   * Get OTP info for debugging (without revealing the actual OTP)
   * @param {string} email - User email
   * @returns {object|null} OTP info or null
   */
  getOTPInfo(email) {
    const stored = this.otpStorage.get(email);
    if (!stored) return null;
    
    return {
      type: stored.type,
      expires: new Date(stored.expires),
      isExpired: Date.now() > stored.expires,
      timeLeft: Math.max(0, stored.expires - Date.now())
    };
  }

  /**
   * Get storage stats for monitoring
   * @returns {object} Storage statistics
   */
  getStats() {
    const now = Date.now();
    let emailVerificationCount = 0;
    let passwordResetCount = 0;
    let expiredCount = 0;
    
    for (const [email, data] of this.otpStorage.entries()) {
      if (now > data.expires) {
        expiredCount++;
      } else if (data.type === 'email_verification') {
        emailVerificationCount++;
      } else if (data.type === 'password_reset') {
        passwordResetCount++;
      }
    }
    
    return {
      total: this.otpStorage.size,
      emailVerification: emailVerificationCount,
      passwordReset: passwordResetCount,
      expired: expiredCount,
      active: this.otpStorage.size - expiredCount
    };
  }
}

// Create singleton instance
const otpService = new OTPService();

module.exports = otpService;
