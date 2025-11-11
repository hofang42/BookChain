const express = require('express');
const router = express.Router();
const chatAIController = require('../controllers/chatAIController');

/**
 * @route   POST /api/chat/message
 * @desc    Gửi tin nhắn text đến AI chatbot
 * @body    { message: string, context: object, conversationHistory: array }
 */
router.post('/message', chatAIController.sendMessage);

/**
 * @route   POST /api/chat/image
 * @desc    Gửi ảnh để AI nhận diện sách
 * @body    { imageBase64: string, message: string }
 */
router.post('/image', chatAIController.sendImage);

/**
 * @route   POST /api/chat/search-book
 * @desc    Tìm kiếm sách theo gợi ý của AI
 * @body    { query: string }
 */
router.post('/search-book', chatAIController.searchBook);

module.exports = router;
