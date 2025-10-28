const nodemailer = require("nodemailer");
require('dotenv').config();

let transporter = null;
let isInitialized = false;

// Initialize email service
const initializeEmailService = () => {
  try {
    if (!process.env.EMAIL_HOST || !process.env.EMAIL_USER || !process.env.EMAIL_PASS) {
      return false;
    }

    transporter = nodemailer.createTransport({
      host: process.env.EMAIL_HOST,
      port: Number(process.env.EMAIL_PORT) || 587,
      secure: false,
      auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS,
      },
    });

    isInitialized = true;
    return true;
  } catch (error) {
    return false;
  }
};

const sendEmail = async (email, subject, textOrHtml) => {
  try {
    // Check if service is initialized
    if (!isInitialized || !transporter) {
      throw new Error("SMTP_SERVICE_NOT_READY");
    }

    await transporter.sendMail({
      from: process.env.EMAIL_USER,
      to: email,
      subject: subject,
      html: textOrHtml,
    });

    return { success: true, message: "Email sent successfully" };
  } catch (error) {
    
    if (error.message === "SMTP_SERVICE_NOT_READY") {
      return { 
        success: false, 
        message: "Email service not initialized", 
        error: "SMTP_SERVICE_NOT_READY" 
      };
    }
    
    return { 
      success: false, 
      message: "Failed to send email", 
      error: error.message 
    };
  }
};

// Helper methods for specific email types
const sendEmailVerificationOTP = async (email, otp, fullName) => {
  const subject = "BookChain - Email Verification";
  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
      <h2 style="color: #333;">Email Verification</h2>
      <p>Hi ${fullName || 'there'},</p>
      <p>Thank you for registering with BookChain. Please use the following verification code to activate your account:</p>
      <div style="background-color: #f4f4f4; padding: 20px; text-align: center; margin: 20px 0;">
        <h1 style="color: #007bff; font-size: 32px; margin: 0;">${otp}</h1>
      </div>
      <p>This code will expire in 10 minutes.</p>
      <p>If you didn't request this verification, please ignore this email.</p>
      <p>Best regards,<br>BookChain Team</p>
    </div>
  `;
  
  return await sendEmail(email, subject, html);
};

const sendWelcomeEmail = async (email, fullName) => {
  const subject = "Welcome to BookChain!";
  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
      <h2 style="color: #333;">Welcome to BookChain!</h2>
      <p>Hi ${fullName || 'there'},</p>
      <p>Welcome to BookChain! Your email has been verified successfully and your account is now active.</p>
      <p>You can now start exploring our platform and discover amazing books.</p>
      <p>Happy reading!</p>
      <p>Best regards,<br>BookChain Team</p>
    </div>
  `;
  
  return await sendEmail(email, subject, html);
};

const sendPasswordResetEmail = async (email, resetToken, fullName) => {
  const resetUrl = `${process.env.FRONTEND_URL || 'http://localhost:3000'}/reset-password?token=${resetToken}`;
  const subject = "BookChain - Password Reset";
  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
      <h2 style="color: #333;">Password Reset Request</h2>
      <p>Hi ${fullName || 'there'},</p>
      <p>You requested a password reset for your BookChain account. Click the button below to reset your password:</p>
      <div style="text-align: center; margin: 30px 0;">
        <a href="${resetUrl}" style="background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; display: inline-block;">Reset Password</a>
      </div>
      <p>Or copy and paste this link in your browser:</p>
      <p style="word-break: break-all; color: #666;">${resetUrl}</p>
      <p>This link will expire in 1 hour.</p>
      <p>If you didn't request this password reset, please ignore this email.</p>
      <p>Best regards,<br>BookChain Team</p>
    </div>
  `;
  
  return await sendEmail(email, subject, html);
};

// Initialize on module load
initializeEmailService();

module.exports = { 
  sendEmail, 
  sendEmailVerificationOTP,
  sendWelcomeEmail,
  sendPasswordResetEmail,
  initializeEmailService, 
  isInitialized: () => isInitialized 
};