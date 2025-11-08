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
  const subject = "🔐 BookChain - Mã Xác Thực Email";
  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </head>
    <body style="margin: 0; padding: 0; background-color: #f5f7fa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <table role="presentation" style="width: 100%; border-collapse: collapse; background-color: #f5f7fa; padding: 20px;">
        <tr>
          <td align="center">
            <table role="presentation" style="max-width: 600px; width: 100%; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
              <!-- Header with Gradient -->
              <tr>
                <td style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 30px; text-align: center;">
                  <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">
                    📚 BookChain
                  </h1>
                  <p style="margin: 10px 0 0 0; color: rgba(255, 255, 255, 0.9); font-size: 16px;">
                    Xác Thực Email Của Bạn
                  </p>
                </td>
              </tr>
              
              <!-- Content -->
              <tr>
                <td style="padding: 40px 30px;">
                  <h2 style="margin: 0 0 20px 0; color: #1a202c; font-size: 24px; font-weight: 600;">
                    Xin chào ${fullName || 'bạn'}! 👋
                  </h2>
                  <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                    Cảm ơn bạn đã đăng ký tài khoản BookChain! Để hoàn tất quá trình đăng ký, vui lòng sử dụng mã xác thực bên dưới:
                  </p>
                  
                  <!-- OTP Box -->
                  <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);">
                    <p style="margin: 0 0 10px 0; color: rgba(255, 255, 255, 0.9); font-size: 14px; font-weight: 500; text-transform: uppercase; letter-spacing: 1px;">
                      Mã Xác Thực
                    </p>
                    <h1 style="margin: 0; color: #ffffff; font-size: 48px; font-weight: 700; letter-spacing: 8px; font-family: 'Courier New', monospace;">
                      ${otp}
                    </h1>
      </div>
                  
                  <div style="background-color: #fff5e6; border-left: 4px solid #ffa726; padding: 15px; margin: 25px 0; border-radius: 4px;">
                    <p style="margin: 0; color: #e65100; font-size: 14px; line-height: 1.5;">
                      ⏰ <strong>Lưu ý:</strong> Mã xác thực này sẽ hết hạn sau <strong>10 phút</strong>. Vui lòng sử dụng ngay để kích hoạt tài khoản của bạn.
                    </p>
    </div>
                  
                  <p style="margin: 20px 0 0 0; color: #718096; font-size: 14px; line-height: 1.6;">
                    Nếu bạn không yêu cầu mã xác thực này, vui lòng bỏ qua email này. Tài khoản của bạn sẽ không được tạo nếu không xác thực email.
                  </p>
                </td>
              </tr>
              
              <!-- Footer -->
              <tr>
                <td style="background-color: #f7fafc; padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                  <p style="margin: 0 0 10px 0; color: #4a5568; font-size: 14px; font-weight: 600;">
                    BookChain Team
                  </p>
                  <p style="margin: 0; color: #718096; font-size: 12px;">
                    © ${new Date().getFullYear()} BookChain. Tất cả quyền được bảo lưu.
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
  `;
  
  return await sendEmail(email, subject, html);
};

const sendWelcomeEmail = async (email, fullName) => {
  const subject = "🎉 Chào Mừng Đến Với BookChain!";
  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </head>
    <body style="margin: 0; padding: 0; background-color: #f5f7fa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <table role="presentation" style="width: 100%; border-collapse: collapse; background-color: #f5f7fa; padding: 20px;">
        <tr>
          <td align="center">
            <table role="presentation" style="max-width: 600px; width: 100%; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
              <!-- Header with Gradient -->
              <tr>
                <td style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 50px 30px; text-align: center;">
                  <div style="font-size: 64px; margin-bottom: 10px;">🎉</div>
                  <h1 style="margin: 0; color: #ffffff; font-size: 36px; font-weight: 700; letter-spacing: -0.5px;">
                    Chào Mừng Đến BookChain!
                  </h1>
                  <p style="margin: 15px 0 0 0; color: rgba(255, 255, 255, 0.95); font-size: 18px; font-weight: 300;">
                    Email của bạn đã được xác thực thành công
                  </p>
                </td>
              </tr>
              
              <!-- Content -->
              <tr>
                <td style="padding: 50px 30px;">
                  <h2 style="margin: 0 0 25px 0; color: #1a202c; font-size: 28px; font-weight: 600;">
                    Xin chào ${fullName || 'bạn'}! 👋
                  </h2>
                  
                  <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.8;">
                    Chúc mừng! Tài khoản BookChain của bạn đã được kích hoạt thành công. Bây giờ bạn có thể bắt đầu khám phá thế giới sách tuyệt vời của chúng tôi.
                  </p>
                  
                  <!-- Features Box -->
                  <div style="background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%); border-radius: 12px; padding: 30px; margin: 30px 0;">
                    <h3 style="margin: 0 0 20px 0; color: #1a202c; font-size: 20px; font-weight: 600; text-align: center;">
                      ✨ Bạn có thể làm gì với BookChain?
                    </h3>
                    <table role="presentation" style="width: 100%; border-collapse: collapse;">
                      <tr>
                        <td style="padding: 15px 0; border-bottom: 1px solid #e2e8f0;">
                          <div style="font-size: 24px; margin-bottom: 8px;">📚</div>
                          <p style="margin: 0; color: #2d3748; font-size: 15px; font-weight: 500;">
                            Khám phá hàng ngàn cuốn sách
                          </p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 15px 0; border-bottom: 1px solid #e2e8f0;">
                          <div style="font-size: 24px; margin-bottom: 8px;">🔄</div>
                          <p style="margin: 0; color: #2d3748; font-size: 15px; font-weight: 500;">
                            Trao đổi và chia sẻ sách với cộng đồng
                          </p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 15px 0;">
                          <div style="font-size: 24px; margin-bottom: 8px;">⭐</div>
                          <p style="margin: 0; color: #2d3748; font-size: 15px; font-weight: 500;">
                            Đánh giá và review sách yêu thích
                          </p>
                        </td>
                      </tr>
                    </table>
                  </div>
                  
                  <!-- CTA Button -->
                  <div style="text-align: center; margin: 35px 0;">
                    <a href="#" style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; text-decoration: none; padding: 16px 40px; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4); transition: all 0.3s;">
                      Bắt Đầu Khám Phá Ngay 🚀
                    </a>
                  </div>
                  
                  <p style="margin: 30px 0 0 0; color: #4a5568; font-size: 16px; line-height: 1.8; text-align: center; font-style: italic;">
                    Chúc bạn có những trải nghiệm đọc sách tuyệt vời! 📖✨
                  </p>
                </td>
              </tr>
              
              <!-- Footer -->
              <tr>
                <td style="background-color: #f7fafc; padding: 40px 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                  <p style="margin: 0 0 15px 0; color: #2d3748; font-size: 18px; font-weight: 700;">
                    📚 BookChain
                  </p>
                  <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 14px; line-height: 1.6;">
                    Nền tảng trao đổi sách hàng đầu Việt Nam
                  </p>
                  <div style="margin: 25px 0; padding-top: 25px; border-top: 1px solid #e2e8f0;">
                    <p style="margin: 0 0 10px 0; color: #718096; font-size: 12px;">
                      © ${new Date().getFullYear()} BookChain. Tất cả quyền được bảo lưu.
                    </p>
                    <p style="margin: 0; color: #a0aec0; font-size: 11px;">
                      Email này được gửi tự động, vui lòng không trả lời.
                    </p>
    </div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
  `;
  
  return await sendEmail(email, subject, html);
};

// DEPRECATED: Hàm này không còn được sử dụng. Hệ thống hiện tại sử dụng OTP thay vì token-based reset.
// Giữ lại để tham khảo trong tương lai nếu cần.
/*
const sendPasswordResetEmail = async (email, resetToken, fullName) => {
  const resetUrl = `${process.env.FRONTEND_URL || 'http://localhost:3000'}/reset-password?token=${resetToken}`;
  const subject = "🔒 BookChain - Đặt Lại Mật Khẩu";
  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </head>
    <body style="margin: 0; padding: 0; background-color: #f5f7fa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <table role="presentation" style="width: 100%; border-collapse: collapse; background-color: #f5f7fa; padding: 20px;">
        <tr>
          <td align="center">
            <table role="presentation" style="max-width: 600px; width: 100%; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
              <!-- Header with Gradient -->
              <tr>
                <td style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); padding: 40px 30px; text-align: center;">
                  <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">
                    🔒 BookChain
                  </h1>
                  <p style="margin: 10px 0 0 0; color: rgba(255, 255, 255, 0.9); font-size: 16px;">
                    Đặt Lại Mật Khẩu
                  </p>
                </td>
              </tr>
              
              <!-- Content -->
              <tr>
                <td style="padding: 40px 30px;">
                  <h2 style="margin: 0 0 20px 0; color: #1a202c; font-size: 24px; font-weight: 600;">
                    Xin chào ${fullName || 'bạn'}! 👋
                  </h2>
                  <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                    Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản BookChain của bạn. Nhấp vào nút bên dưới để đặt lại mật khẩu:
                  </p>
                  
                  <!-- CTA Button -->
                  <div style="text-align: center; margin: 35px 0;">
                    <a href="${resetUrl}" style="display: inline-block; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: #ffffff; text-decoration: none; padding: 16px 40px; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(240, 147, 251, 0.4);">
                      Đặt Lại Mật Khẩu 🔐
                    </a>
                  </div>
                  
                  <p style="margin: 20px 0; color: #718096; font-size: 14px; line-height: 1.6; text-align: center;">
                    Hoặc sao chép và dán liên kết sau vào trình duyệt của bạn:
                  </p>
                  <div style="background-color: #f7fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 15px; margin: 20px 0;">
                    <p style="margin: 0; word-break: break-all; color: #4a5568; font-size: 13px; font-family: 'Courier New', monospace;">
                      ${resetUrl}
                    </p>
                  </div>
                  
                  <div style="background-color: #fff5e6; border-left: 4px solid #ffa726; padding: 15px; margin: 25px 0; border-radius: 4px;">
                    <p style="margin: 0; color: #e65100; font-size: 14px; line-height: 1.5;">
                      ⏰ <strong>Lưu ý:</strong> Liên kết này sẽ hết hạn sau <strong>1 giờ</strong>. Vui lòng sử dụng ngay để đặt lại mật khẩu.
                    </p>
                  </div>
                  
                  <div style="background-color: #f0f9ff; border-left: 4px solid #3b82f6; padding: 15px; margin: 25px 0; border-radius: 4px;">
                    <p style="margin: 0; color: #1e40af; font-size: 14px; line-height: 1.5;">
                      🔐 <strong>Bảo mật:</strong> Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này. Mật khẩu của bạn sẽ không thay đổi.
                    </p>
                  </div>
                </td>
              </tr>
              
              <!-- Footer -->
              <tr>
                <td style="background-color: #f7fafc; padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                  <p style="margin: 0 0 10px 0; color: #4a5568; font-size: 14px; font-weight: 600;">
                    BookChain Team
                  </p>
                  <p style="margin: 0; color: #718096; font-size: 12px;">
                    © ${new Date().getFullYear()} BookChain. Tất cả quyền được bảo lưu.
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
  `;
  
  return await sendEmail(email, subject, html);
};
*/

const sendPasswordResetOTP = async (email, otp, fullName) => {
  const subject = "🔒 BookChain - Mã Đặt Lại Mật Khẩu";
  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
    </head>
    <body style="margin: 0; padding: 0; background-color: #f5f7fa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <table role="presentation" style="width: 100%; border-collapse: collapse; background-color: #f5f7fa; padding: 20px;">
        <tr>
          <td align="center">
            <table role="presentation" style="max-width: 600px; width: 100%; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
              <!-- Header with Gradient -->
              <tr>
                <td style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); padding: 40px 30px; text-align: center;">
                  <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">
                    🔒 BookChain
                  </h1>
                  <p style="margin: 10px 0 0 0; color: rgba(255, 255, 255, 0.9); font-size: 16px;">
                    Đặt Lại Mật Khẩu
                  </p>
                </td>
              </tr>
              
              <!-- Content -->
              <tr>
                <td style="padding: 40px 30px;">
                  <h2 style="margin: 0 0 20px 0; color: #1a202c; font-size: 24px; font-weight: 600;">
                    Xin chào ${fullName || 'bạn'}! 👋
                  </h2>
                  <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                    Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản BookChain của bạn. Vui lòng sử dụng mã bên dưới để đặt lại mật khẩu:
                  </p>
                  
                  <!-- OTP Box -->
                  <div style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; box-shadow: 0 8px 16px rgba(240, 147, 251, 0.3);">
                    <p style="margin: 0 0 10px 0; color: rgba(255, 255, 255, 0.9); font-size: 14px; font-weight: 500; text-transform: uppercase; letter-spacing: 1px;">
                      Mã Đặt Lại Mật Khẩu
                    </p>
                    <h1 style="margin: 0; color: #ffffff; font-size: 48px; font-weight: 700; letter-spacing: 8px; font-family: 'Courier New', monospace;">
                      ${otp}
                    </h1>
                  </div>
                  
                  <div style="background-color: #ffeef0; border-left: 4px solid #f5576c; padding: 15px; margin: 25px 0; border-radius: 4px;">
                    <p style="margin: 0; color: #c53030; font-size: 14px; line-height: 1.5;">
                      ⏰ <strong>Lưu ý:</strong> Mã này sẽ hết hạn sau <strong>10 phút</strong>. Vui lòng sử dụng ngay để đặt lại mật khẩu của bạn.
                    </p>
      </div>
                  
                  <div style="background-color: #f0f9ff; border-left: 4px solid #3b82f6; padding: 15px; margin: 25px 0; border-radius: 4px;">
                    <p style="margin: 0; color: #1e40af; font-size: 14px; line-height: 1.5;">
                      🔐 <strong>Bảo mật:</strong> Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này. Mật khẩu của bạn sẽ không thay đổi.
                    </p>
    </div>
                  
                  <p style="margin: 20px 0 0 0; color: #718096; font-size: 14px; line-height: 1.6;">
                    Nếu bạn gặp bất kỳ vấn đề nào, vui lòng liên hệ với đội ngũ hỗ trợ của chúng tôi.
                  </p>
                </td>
              </tr>
              
              <!-- Footer -->
              <tr>
                <td style="background-color: #f7fafc; padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                  <p style="margin: 0 0 10px 0; color: #4a5568; font-size: 14px; font-weight: 600;">
                    BookChain Team
                  </p>
                  <p style="margin: 0; color: #718096; font-size: 12px;">
                    © ${new Date().getFullYear()} BookChain. Tất cả quyền được bảo lưu.
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
  `;
  
  return await sendEmail(email, subject, html);
};

// Initialize on module load
initializeEmailService();

module.exports = { 
  sendEmail, 
  sendEmailVerificationOTP,
  sendWelcomeEmail,
  // sendPasswordResetEmail, // DEPRECATED: Không còn sử dụng, đã chuyển sang OTP-based reset
  sendPasswordResetOTP,
  initializeEmailService, 
  isInitialized: () => isInitialized 
};