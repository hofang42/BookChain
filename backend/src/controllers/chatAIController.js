const chatAIService = require('../services/chatAIService');

class ChatAIController {
  /**
   * POST /api/chat/message
   * Gửi tin nhắn text đến AI chatbot
   */
  async sendMessage(req, res) {
    try {
      const { message, context, conversationHistory } = req.body;

      if (!message || message.trim() === '') {
        return res.status(400).json({
          success: false,
          message: 'Tin nhắn không được để trống'
        });
      }

      const result = await chatAIService.sendMessage(
        message,
        context || {},
        conversationHistory || []
      );

      if (result.success) {
        return res.status(200).json({
          success: true,
          message: result.message,
          usage: result.usage
        });
      } else {
        return res.status(500).json({
          success: false,
          message: result.error,
          details: result.details
        });
      }

    } catch (error) {
      console.error('ChatAI Controller Error:', error);
      return res.status(500).json({
        success: false,
        message: 'Lỗi server khi xử lý tin nhắn',
        error: error.message
      });
    }
  }

  /**
   * POST /api/chat/image
   * Gửi ảnh để AI nhận diện sách
   */
  async sendImage(req, res) {
    try {
      const { imageBase64, message } = req.body;

      if (!imageBase64) {
        return res.status(400).json({
          success: false,
          message: 'Vui lòng gửi ảnh'
        });
      }

      // Loại bỏ prefix data:image nếu có
      const base64Data = imageBase64.replace(/^data:image\/\w+;base64,/, '');

      const result = await chatAIService.sendImage(
        base64Data,
        message || 'Đây là sách gì?'
      );

      if (result.success) {
        return res.status(200).json({
          success: true,
          message: result.message,
          usage: result.usage
        });
      } else {
        return res.status(500).json({
          success: false,
          message: result.error,
          details: result.details
        });
      }

    } catch (error) {
      console.error('ChatAI Image Controller Error:', error);
      return res.status(500).json({
        success: false,
        message: 'Lỗi server khi xử lý ảnh',
        error: error.message
      });
    }
  }

  /**
   * POST /api/chat/search-book
   * Tìm kiếm sách theo gợi ý của AI
   */
  async searchBook(req, res) {
    try {
      const { query } = req.body;

      if (!query || query.trim() === '') {
        return res.status(400).json({
          success: false,
          message: 'Vui lòng nhập từ khóa tìm kiếm'
        });
      }

      const books = await chatAIService.searchBookByAI(query);

      return res.status(200).json({
        success: true,
        count: books.length,
        books: books
      });

    } catch (error) {
      console.error('ChatAI Search Book Error:', error);
      return res.status(500).json({
        success: false,
        message: 'Lỗi khi tìm kiếm sách',
        error: error.message
      });
    }
  }
}

module.exports = new ChatAIController();
