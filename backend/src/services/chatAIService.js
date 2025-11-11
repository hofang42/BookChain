const axios = require('axios');
const chatAIConfig = require('../config/chatai');
const Book = require('../models/Book');
const Inventory = require('../models/Inventory');
const Branch = require('../models/Branch');

class ChatAIService {
  /**
   * Gửi tin nhắn text đến AI với context từ database
   * @param {string} message - Tin nhắn của user
   * @param {object} context - Context của screen hiện tại (screenName, bookId, etc)
   * @param {array} conversationHistory - Lịch sử chat trước đó
   */
  async sendMessage(message, context = {}, conversationHistory = []) {
    try {
      // Tìm sách liên quan đến câu hỏi
      let relevantBooks = [];
      const searchKeywords = this.extractKeywords(message);
      
      if (searchKeywords.length > 0) {
        // Tìm sách và kết hợp với inventory để kiểm tra còn hàng
        const books = await Book.find({
          $or: [
            { title_unaccented: { $regex: searchKeywords.join('|'), $options: 'i' } },
            { author_unaccented: { $regex: searchKeywords.join('|'), $options: 'i' } },
            { description_unaccented: { $regex: searchKeywords.join('|'), $options: 'i' } }
          ]
        })
        .populate('categoryId')
        .limit(10);
        
        // Lọc chỉ lấy sách có tồn kho ở ít nhất một chi nhánh
        for (const book of books) {
          const totalStock = await Inventory.aggregate([
            { $match: { bookId: book._id } },
            { $group: { _id: null, total: { $sum: '$stock' } } }
          ]);
          
          if (totalStock.length > 0 && totalStock[0].total > 0) {
            relevantBooks.push(book);
            if (relevantBooks.length >= 5) break;
          }
        }
      }
      
      // Nếu không tìm thấy sách, lấy top sách bán chạy còn hàng để gợi ý
      if (relevantBooks.length === 0) {
        const topBooks = await Book.find()
          .populate('categoryId')
          .sort({ salesCount: -1 })
          .limit(10);
        
        for (const book of topBooks) {
          const totalStock = await Inventory.aggregate([
            { $match: { bookId: book._id } },
            { $group: { _id: null, total: { $sum: '$stock' } } }
          ]);
          
          if (totalStock.length > 0 && totalStock[0].total > 0) {
            relevantBooks.push(book);
            if (relevantBooks.length >= 5) break;
          }
        }
      }
      
      // Lấy danh sách tất cả chi nhánh
      const allBranches = await Branch.find({ isActive: true });
      
      // Lấy thông tin context từ database nếu có bookId
      let contextInfo = '';
      if (context.bookId) {
        const book = await Book.findById(context.bookId)
          .populate('categoryId');
        
        if (book) {
          // Lấy thông tin tồn kho từ các chi nhánh
          const inventory = await Inventory.find({ bookId: book._id })
            .populate('branchId');
          
          const totalStock = inventory.reduce((sum, inv) => sum + inv.stock, 0);
          const availableBranches = inventory.filter(inv => inv.stock > 0);
          
          contextInfo = `\n\nThông tin sách hiện tại:\n` +
            `- Tên sách: ${book.title}\n` +
            `- Tác giả: ${book.author || 'Không rõ'}\n` +
            `- Thể loại: ${book.categoryId?.name || 'Không rõ'}\n` +
            `- Mô tả: ${book.description || 'Không có'}\n` +
            `- Giá: ${book.price?.toLocaleString('vi-VN')} VNĐ\n` +
            `- Tổng số lượng còn: ${totalStock} cuốn`;
          
          if (availableBranches.length > 0) {
            contextInfo += `\n- Có sẵn tại các chi nhánh:\n`;
            availableBranches.forEach(inv => {
              contextInfo += `  + ${inv.branchId?.name || 'N/A'} (${inv.branchId?.address || 'N/A'}): ${inv.stock} cuốn\n`;
            });
          } else {
            contextInfo += `\n- Hiện tại hết hàng tại tất cả chi nhánh`;
          }
        }
      }
      
      // Thêm thông tin sách tìm được kèm theo tồn kho chi nhánh
      if (relevantBooks.length > 0) {
        contextInfo += `\n\nCác sách có sẵn trong BookChain:\n`;
        for (const [index, book] of relevantBooks.entries()) {
          const inventory = await Inventory.find({ bookId: book._id })
            .populate('branchId');
          
          const branchInfo = inventory
            .filter(inv => inv.stock > 0)
            .map(inv => `${inv.branchId?.name}(${inv.stock})`)
            .join(', ');
          
          contextInfo += `${index + 1}. [BOOK:${book._id}] ${book.title} - ${book.author} (${book.price?.toLocaleString('vi-VN')} VNĐ, Thể loại: ${book.categoryId?.name || 'N/A'}) - Có tại: ${branchInfo}\n`;
        }
      }
      
      // Thêm danh sách chi nhánh để AI có thể tham chiếu
      if (allBranches.length > 0) {
        contextInfo += `\n\nDanh sách chi nhánh BookChain:\n`;
        allBranches.forEach((branch, index) => {
          contextInfo += `${index + 1}. ${branch.name}${branch.address ? ' - ' + branch.address : ''}${branch.city ? ', ' + branch.city : ''}\n`;
        });
      }

      // Tạo system prompt với context
      const systemPrompt = `Bạn là trợ lý AI thông minh và thân thiện của BookChain - ứng dụng mua bán sách.

NHIỆM VỤ CHÍNH:
1. Nhận diện sách từ văn bản/hình ảnh người dùng gửi
2. Giới thiệu CHI TIẾT về sách đó:
   - Tên đầy đủ, tác giả
   - Nội dung chính, cốt truyện (nếu biết)
   - Thông tin về tập/chương (nếu có)
   - Đối tượng độc giả phù hợp
   - Đánh giá của bạn về cuốn sách

3. SAU KHI giới thiệu xong, kiểm tra danh sách BookChain bên dưới:
   - Nếu KHÔNG TÌM THẤY sách đó trong danh sách:
     + Viết: "Rất tiếc, hiện tại BookChain chưa có sách này hoặc đã hết hàng tại tất cả chi nhánh."
     + Tiếp: "Tuy nhiên, bạn có thể tham khảo các sách cùng thể loại sau đây:"
     + List sách có trong danh sách với format: * [BOOK:id] Tên - Tác giả
   
   - Nếu TÌM THẤY sách trong danh sách:
     + Viết: "Tin vui! BookChain hiện có sẵn sách này tại các chi nhánh:"
     + List với format: * [BOOK:id] Tên - Tác giả

QUAN TRỌNG - Format khi list sách:
* Mỗi sách trên 1 dòng
* Bắt đầu bằng dấu * (sao)
* Format: * [BOOK:bookId] Tên Đầy Đủ - Tác Giả
* Ví dụ: "* [BOOK:6911f282b36373cacdfdad8b] Sherlock Holmes Toàn Tập - Arthur Conan Doyle"

LƯU Ý VỀ TỒN KHO VÀ CHI NHÁNH:
- BookChain có nhiều chi nhánh/cơ sở (danh sách bên dưới)
- Mỗi sách có inventory riêng tại từng chi nhánh
- Trong danh sách sách bên dưới, sau thông tin sách sẽ có "Có tại: [Tên chi nhánh(số lượng)]"
- Khi người dùng HỎI VỀ CHI NHÁNH CỤ THỂ (ví dụ: "sách A còn ở chi nhánh X không?"):
  + Kiểm tra thông tin tồn kho chi tiết trong danh sách
  + Trả lời CỤ THỂ về chi nhánh đó: "Có/Không, tại chi nhánh X còn Y cuốn"
  + Nếu hết ở chi nhánh đó nhưng còn ở chi nhánh khác, gợi ý: "Tại chi nhánh X đã hết, nhưng bạn có thể mua tại chi nhánh Z"
- Nếu người dùng hỏi chung chung về tồn kho, trả lời tổng quan về tất cả chi nhánh

Hãy trả lời bằng tiếng Việt, nhiệt tình và CHI TIẾT như một người bán sách chuyên nghiệp!

Người dùng đang ở: ${context.screenName || 'Trang chủ'}${contextInfo}`;

      // Chuẩn bị messages cho API
      const messages = [
        { role: 'system', content: systemPrompt },
        ...conversationHistory,
        { role: 'user', content: message }
      ];

      // Gọi OpenRouter API
      const response = await axios.post(
        chatAIConfig.baseUrl,
        {
          model: chatAIConfig.model,
          messages: messages,
          max_tokens: 1000,
          temperature: 0.7
        },
        {
          headers: {
            'Authorization': `Bearer ${chatAIConfig.apiKey}`,
            'Content-Type': 'application/json'
          }
        }
      );

      return {
        success: true,
        message: response.data.choices[0].message.content,
        usage: response.data.usage
      };

    } catch (error) {
      console.error('ChatAI Service Error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.',
        details: error.response?.data || error.message
      };
    }
  }

  /**
   * Gửi ảnh để AI nhận diện sách
   * @param {string} imageBase64 - Ảnh dạng base64
   * @param {string} message - Tin nhắn kèm theo (optional)
   */
  async sendImage(imageBase64, message = 'Đây là sách gì?') {
    try {
      const systemPrompt = `Bạn là chuyên gia nhận diện sách của BookChain.
Nhiệm vụ:
- Nhận diện tên sách, tác giả từ ảnh bìa sách
- Cung cấp thông tin về sách nếu biết
- Gợi ý sách tương tự
- Trả lời bằng tiếng Việt`;

      const messages = [
        { 
          role: 'system', 
          content: systemPrompt 
        },
        {
          role: 'user',
          content: [
            {
              type: 'text',
              text: message
            },
            {
              type: 'image_url',
              image_url: {
                url: `data:image/jpeg;base64,${imageBase64}`
              }
            }
          ]
        }
      ];

      // Gọi OpenRouter API với vision
      const response = await axios.post(
        chatAIConfig.baseUrl,
        {
          model: chatAIConfig.model,
          messages: messages,
          max_tokens: 1000,
          temperature: 0.7
        },
        {
          headers: {
            'Authorization': `Bearer ${chatAIConfig.apiKey}`,
            'Content-Type': 'application/json'
          }
        }
      );

      return {
        success: true,
        message: response.data.choices[0].message.content,
        usage: response.data.usage
      };

    } catch (error) {
      console.error('ChatAI Image Service Error:', error.response?.data || error.message);
      return {
        success: false,
        error: 'Không thể nhận diện ảnh. Vui lòng thử lại.',
        details: error.response?.data || error.message
      };
    }
  }

  /**
   * Tìm kiếm sách dựa trên mô tả của AI
   * @param {string} searchQuery - Tên sách hoặc tác giả
   */
  async searchBookByAI(searchQuery) {
    try {
      const books = await Book.find({
        $or: [
          { title: { $regex: searchQuery, $options: 'i' } },
          { description: { $regex: searchQuery, $options: 'i' } }
        ]
      })
        .populate('author')
        .populate('category')
        .limit(5);

      return books;
    } catch (error) {
      console.error('Search Book Error:', error);
      return [];
    }
  }

  /**
   * Trích xuất keywords từ câu hỏi
   * @param {string} message - Tin nhắn của user
   */
  extractKeywords(message) {
    // Loại bỏ stopwords tiếng Việt
    const stopwords = ['tôi', 'tui', 'mình', 'có', 'không', 'là', 'của', 'và', 'thì', 'cho', 'được', 'một', 'các', 'này', 'đó', 'nào', 'về', 'với', 'sách', 'cuốn', 'quyển', 'trong', 'ngoài', 'trên', 'dưới', 'từ', 'đến', 'bởi', 'như', 'để', 'khi', 'nếu', 'mà', 'hay', 'hoặc'];
    
    // Normalize và split
    const words = message.toLowerCase()
      .replace(/[^\w\sáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ]/gi, ' ')
      .split(/\s+/)
      .filter(word => word.length > 2 && !stopwords.includes(word));
    
    // Thêm các keywords đặc biệt nếu phát hiện
    const specialKeywords = [];
    const lowerMessage = message.toLowerCase();
    
    // Phát hiện thể loại
    const genres = {
      'trinh tham': 'trinh-tham',
      'kinh di': 'kinh-di', 
      'khoa hoc vien tuong': 'khoa-hoc-vien-tuong',
      'fantasy': 'fantasy-ky-ao',
      'van hoc': 'van-hoc-co-dien',
      'thieu nhi': 'sach-thieu-nhi',
      'phat trien ban than': 'phat-trien-ban-than',
      'kinh doanh': 'kinh-doanh-tai-chinh',
      'lich su': 'lich-su'
    };
    
    for (const [keyword, slug] of Object.entries(genres)) {
      if (lowerMessage.includes(keyword)) {
        specialKeywords.push(keyword.replace(/\s+/g, ''));
      }
    }
    
    return [...words, ...specialKeywords];
  }
}

module.exports = new ChatAIService();
