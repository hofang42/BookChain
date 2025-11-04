/**
 * OTP Service - In-memory storage for OTP codes
 * Handles email verification and password reset OTP
 */

class OTPService {
  constructor() {
    // In-memory storage for OTPs
    // Format: { email: { otp: "123456", expires: timestamp, type: "email_verification" | "password_reset" } }
    this.otpStorage = new Map();
    
    // Rate limiting storage
    // Format: { email: { attempts: number, lastAttempt: timestamp, cooldownUntil: timestamp } }
    this.rateLimitStorage = new Map();
    
    // Clean expired OTPs every 5 minutes
    setInterval(() => {
      this.cleanExpiredOTPs();
      this.cleanExpiredRateLimits();
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
   * Check rate limiting for OTP generation
   * @param {string} email - User email
   * @returns {object} Rate limit result
   */
  checkRateLimit(email) {
    const now = Date.now();
    const rateLimit = this.rateLimitStorage.get(email);
    
    if (!rateLimit) {
      return { allowed: true };
    }
    
    // Check if still in cooldown
    if (now < rateLimit.cooldownUntil) {
      const remainingTime = Math.ceil((rateLimit.cooldownUntil - now) / 1000);
      return { 
        allowed: false, 
        reason: 'cooldown',
        remainingTime 
      };
    }
    
    // Reset attempts if cooldown period has passed
    if (now >= rateLimit.cooldownUntil) {
      this.rateLimitStorage.delete(email);
      return { allowed: true };
    }
    
    // Check if too many attempts in time window (30 minutes)
    const timeWindow = 30 * 60 * 1000; // 30 minutes
    if (now - rateLimit.lastAttempt > timeWindow) {
      // Reset attempts if outside time window
      this.rateLimitStorage.delete(email);
      return { allowed: true };
    }
    
    if (rateLimit.attempts >= 5) {
      // Set cooldown for 30 minutes
      rateLimit.cooldownUntil = now + timeWindow;
      const remainingTime = Math.ceil(timeWindow / 1000);
      return { 
        allowed: false, 
        reason: 'too_many_attempts',
        remainingTime 
      };
    }
    
    return { allowed: true };
  }

  /**
   * Update rate limiting after OTP generation
   * @param {string} email - User email
   */
  updateRateLimit(email) {
    const now = Date.now();
    const rateLimit = this.rateLimitStorage.get(email);
    
    if (!rateLimit) {
      this.rateLimitStorage.set(email, {
        attempts: 1,
        lastAttempt: now,
        cooldownUntil: now + 60 * 1000 // 60 seconds cooldown between requests
      });
    } else {
      rateLimit.attempts += 1;
      rateLimit.lastAttempt = now;
      rateLimit.cooldownUntil = now + 60 * 1000; // 60 seconds cooldown between requests
    }
  }

  /**
   * Store OTP for email verification
   * @param {string} email - User email
   * @returns {object} Result with OTP or error
   */
  generateEmailVerificationOTP(email) {
    // Check rate limiting
    const rateLimitResult = this.checkRateLimit(email);
    if (!rateLimitResult.allowed) {
      return {
        success: false,
        error: rateLimitResult.reason === 'cooldown' 
          ? `Please wait ${rateLimitResult.remainingTime} seconds before requesting another code`
          : `Too many attempts. Please try again in ${Math.ceil(rateLimitResult.remainingTime / 60)} minutes`,
        remainingTime: rateLimitResult.remainingTime
      };
    }

    const otp = this.generateOTP();
    const expires = Date.now() + 10 * 60 * 1000; // 10 minutes
    
    this.otpStorage.set(email, {
      otp,
      expires,
      type: 'email_verification'
    });
    
    // Update rate limiting
    this.updateRateLimit(email);
    
    return {
      success: true,
      otp
    };
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
   * Clean expired rate limits from memory
   */
  cleanExpiredRateLimits() {
    const now = Date.now();
    const timeWindow = 30 * 60 * 1000; // 30 minutes
    
    for (const [email, data] of this.rateLimitStorage.entries()) {
      // Remove rate limits older than time window and not in cooldown
      if (now - data.lastAttempt > timeWindow && now >= data.cooldownUntil) {
        this.rateLimitStorage.delete(email);
      }
    }
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
