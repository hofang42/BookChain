const Message = require("../models/Message");
const Conversation = require("../models/Conversation");
const User = require("../models/User");

/**
 * Get conversations for admin
 * GET /api/chat/conversations
 */
const getConversations = async (req, res, next) => {
  try {
    const { status = "active", page = 1, limit = 20 } = req.query;
    const skip = (parseInt(page) - 1) * parseInt(limit);

    // For admin, when status is "active", include both "active" and "pending" conversations
    // This allows admin to see new conversations that need to be assigned
    const query = status === "active" 
      ? { status: { $in: ["active", "pending"] } }
      : { status };

    const conversations = await Conversation.find(query)
      .populate("userId", "fullName email username")
      .populate("adminId", "fullName email username")
      .sort({ lastMessageAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Conversation.countDocuments(query);

    res.json({
      success: true,
      data: {
        conversations,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total,
          pages: Math.ceil(total / parseInt(limit)),
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get user's conversation
 * GET /api/chat/conversation
 */
const getUserConversation = async (req, res, next) => {
  try {
    const conversation = await Conversation.findOne({
      userId: req.user._id,
      status: { $in: ["active", "pending"] },
    })
      .populate("userId", "fullName email username")
      .populate("adminId", "fullName email username");

    if (!conversation) {
      // Create new conversation if doesn't exist
      const newConversation = new Conversation({
        userId: req.user._id,
        status: "pending",
      });
      await newConversation.save();
      await newConversation.populate("userId", "fullName email username");

      return res.json({
        success: true,
        data: newConversation,
      });
    }

    res.json({
      success: true,
      data: conversation,
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get messages in a conversation
 * GET /api/chat/conversation/:conversationId/messages
 */
const getMessages = async (req, res, next) => {
  try {
    const { conversationId } = req.params;
    const { page = 1, limit = 50 } = req.query;
    const skip = (parseInt(page) - 1) * parseInt(limit);

    const conversation = await Conversation.findById(conversationId);
    if (!conversation) {
      return res.status(404).json({
        success: false,
        error: "Conversation not found",
      });
    }

    // Check permission
    if (
      req.user.role !== "admin" &&
      conversation.userId.toString() !== req.user._id.toString()
    ) {
      return res.status(403).json({
        success: false,
        error: "Permission denied",
      });
    }

    const messages = await Message.find({ conversationId })
      .populate("senderId", "fullName email username role")
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Message.countDocuments({ conversationId });

    // Mark messages as read if user is viewing
    if (req.user.role === "admin") {
      await Message.updateMany(
        {
          conversationId,
          senderId: { $ne: req.user._id },
          isRead: false,
        },
        {
          $set: { isRead: true, readAt: new Date() },
        }
      );
      conversation.unreadCountForAdmin = 0;
    } else {
      await Message.updateMany(
        {
          conversationId,
          senderId: { $ne: req.user._id },
          isRead: false,
        },
        {
          $set: { isRead: true, readAt: new Date() },
        }
      );
      conversation.unreadCountForUser = 0;
    }

    conversation.unreadCount = conversation.unreadCountForUser + conversation.unreadCountForAdmin;
    await conversation.save();

    res.json({
      success: true,
      data: {
        messages: messages.reverse(), // Reverse to show oldest first
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total,
          pages: Math.ceil(total / parseInt(limit)),
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Admin assigns themselves to a conversation
 * POST /api/chat/conversation/:conversationId/assign
 */
const assignAdmin = async (req, res, next) => {
  try {
    const { conversationId } = req.params;

    const conversation = await Conversation.findById(conversationId);
    if (!conversation) {
      return res.status(404).json({
        success: false,
        error: "Conversation not found",
      });
    }

    if (conversation.adminId) {
      return res.status(400).json({
        success: false,
        error: "Conversation already has an admin assigned",
      });
    }

    conversation.adminId = req.user._id;
    conversation.status = "active";
    await conversation.save();

    await conversation.populate("userId", "fullName email username");
    await conversation.populate("adminId", "fullName email username");

    res.json({
      success: true,
      data: conversation,
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Close a conversation
 * POST /api/chat/conversation/:conversationId/close
 */
const closeConversation = async (req, res, next) => {
  try {
    const { conversationId } = req.params;

    const conversation = await Conversation.findById(conversationId);
    if (!conversation) {
      return res.status(404).json({
        success: false,
        error: "Conversation not found",
      });
    }

    // Check permission - only admin or the user themselves can close
    if (
      req.user.role !== "admin" &&
      conversation.userId.toString() !== req.user._id.toString()
    ) {
      return res.status(403).json({
        success: false,
        error: "Permission denied",
      });
    }

    conversation.status = "closed";
    await conversation.save();

    res.json({
      success: true,
      message: "Conversation closed",
      data: conversation,
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Send a message in a conversation
 * POST /api/chat/conversation/:conversationId/messages
 */
const sendMessage = async (req, res, next) => {
  try {
    const { conversationId } = req.params;
    const { content, messageType = "text" } = req.body;

    if (!content || content.trim().length === 0) {
      return res.status(400).json({
        success: false,
        error: "Message content is required",
      });
    }

    const conversation = await Conversation.findById(conversationId);
    if (!conversation) {
      return res.status(404).json({
        success: false,
        error: "Conversation not found",
      });
    }

    // Check permission
    if (
      req.user.role !== "admin" &&
      conversation.userId.toString() !== req.user._id.toString()
    ) {
      return res.status(403).json({
        success: false,
        error: "Permission denied",
      });
    }

    // Create message
    const message = new Message({
      conversationId,
      senderId: req.user._id,
      content: content.trim(),
      messageType,
    });

    await message.save();
    await message.populate("senderId", "fullName email username role");

    // Update conversation
    conversation.lastMessage = content.trim();
    conversation.lastMessageAt = new Date();
    
    if (req.user.role === "admin") {
      conversation.unreadCountForUser += 1;
    } else {
      conversation.unreadCountForAdmin += 1;
    }
    
    conversation.unreadCount = conversation.unreadCountForUser + conversation.unreadCountForAdmin;
    await conversation.save();

    res.json({
      success: true,
      data: message,
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getConversations,
  getUserConversation,
  getMessages,
  assignAdmin,
  closeConversation,
  sendMessage,
};

