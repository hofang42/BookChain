const express = require("express");
const router = express.Router();
const { authenticate, authorize } = require("../middleware/auth");
const {
  getConversations,
  getUserConversation,
  getMessages,
  assignAdmin,
  closeConversation,
  sendMessage,
} = require("../controllers/chatController");

// All routes require authentication
router.use(authenticate);

// Get conversations (admin only)
router.get("/conversations", authorize("admin"), getConversations);

// Get user's own conversation
router.get("/conversation", getUserConversation);

// Get messages in a conversation
router.get("/conversation/:conversationId/messages", getMessages);

// Send a message in a conversation
router.post("/conversation/:conversationId/messages", sendMessage);

// Admin assigns themselves to conversation
router.post("/conversation/:conversationId/assign", authorize("admin"), assignAdmin);

// Close conversation
router.post("/conversation/:conversationId/close", closeConversation);

module.exports = router;

