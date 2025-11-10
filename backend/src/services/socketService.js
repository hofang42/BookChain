const jwt = require("jsonwebtoken");
const User = require("../models/User");
const Message = require("../models/Message");
const Conversation = require("../models/Conversation");

let io = null;

/**
 * Initialize Socket.IO server
 */
const initializeSocket = (server) => {
  const { Server } = require("socket.io");
  
  io = new Server(server, {
    cors: {
      origin: "*", // In production, specify your frontend URL
      methods: ["GET", "POST"],
      credentials: true,
    },
  });

  // Socket authentication middleware
  io.use(async (socket, next) => {
    try {
      const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.replace("Bearer ", "");
      
      if (!token) {
        return next(new Error("Authentication error: No token provided"));
      }

      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      const user = await User.findById(decoded.userId).select("-passwordHash");

      if (!user || user.status !== "active") {
        return next(new Error("Authentication error: Invalid user"));
      }

      socket.userId = user._id.toString();
      socket.userRole = user.role;
      socket.user = user;
      next();
    } catch (error) {
      next(new Error("Authentication error: " + error.message));
    }
  });

  io.on("connection", async (socket) => {
    // Join user's personal room
    socket.join(`user_${socket.userId}`);

    // If admin, join admin room
    if (socket.userRole === "admin") {
      socket.join("admin_room");
    }

    // Handle getting or creating conversation
    socket.on("get_conversation", async () => {
      try {
        if (socket.userRole === "admin") {
          // Admin can see all active and pending conversations
          // This allows admin to see new conversations that need to be assigned
          const conversations = await Conversation.find({ 
            status: { $in: ["active", "pending"] } 
          })
            .populate("userId", "fullName email")
            .populate("adminId", "fullName email")
            .sort({ lastMessageAt: -1 });

          socket.emit("conversations_list", conversations);
        } else {
          // Customer gets their own conversation
          let conversation = await Conversation.findOne({
            userId: socket.userId,
            status: { $in: ["active", "pending"] },
          })
            .populate("userId", "fullName email")
            .populate("adminId", "fullName email");

          if (!conversation) {
            // Create new conversation
            conversation = new Conversation({
              userId: socket.userId,
              status: "pending",
            });
            await conversation.save();
            await conversation.populate("userId", "fullName email");
          }

          socket.emit("conversation_data", conversation);
        }
      } catch (error) {
        socket.emit("error", { message: "Failed to get conversation" });
      }
    });

    // Handle sending message
    socket.on("send_message", async (data) => {
      try {
        const { conversationId, content, messageType = "text" } = data;

        if (!content || !content.trim()) {
          socket.emit("error", { message: "Message content is required" });
          return;
        }

        let conversation;

        if (conversationId) {
          conversation = await Conversation.findById(conversationId);
          if (!conversation) {
            socket.emit("error", { message: "Conversation not found" });
            return;
          }

          // Check if user has permission to send to this conversation
          const conversationUserId = conversation.userId.toString();
          const socketUserId = socket.userId.toString();
          
          if (
            socket.userRole !== "admin" &&
            conversationUserId !== socketUserId
          ) {
            socket.emit("error", { message: "Permission denied: You can only send messages to your own conversation" });
            return;
          }
        } else {
          // Create new conversation if doesn't exist
          if (socket.userRole === "admin") {
            socket.emit("error", { message: "Admin must specify conversationId" });
            return;
          }

          conversation = await Conversation.findOne({
            userId: socket.userId,
            status: { $in: ["active", "pending"] },
          });

          if (!conversation) {
            conversation = new Conversation({
              userId: socket.userId,
              status: "pending",
            });
            await conversation.save();
          }
        }

        // If admin joins conversation, assign admin and activate
        if (socket.userRole === "admin" && !conversation.adminId) {
          conversation.adminId = socket.userId;
          conversation.status = "active";
        }

        // Create message
        const message = new Message({
          conversationId: conversation._id,
          senderId: socket.userId,
          content: content.trim(),
          messageType,
        });

        await message.save();

        // Update conversation
        conversation.lastMessage = content.trim();
        conversation.lastMessageAt = new Date();

        // Update unread counts
        if (socket.userRole === "admin") {
          conversation.unreadCountForUser += 1;
        } else {
          conversation.unreadCountForAdmin += 1;
        }
        conversation.unreadCount = conversation.unreadCountForUser + conversation.unreadCountForAdmin;

        await conversation.save();

        // Populate message with sender info
        await message.populate("senderId", "fullName email role");

        // Emit to conversation participants
        const messageData = {
          _id: message._id,
          conversationId: message.conversationId,
          senderId: message.senderId,
          content: message.content,
          messageType: message.messageType,
          isRead: message.isRead,
          createdAt: message.createdAt,
          updatedAt: message.updatedAt,
        };

        // Send to user
        io.to(`user_${conversation.userId}`).emit("new_message", messageData);

        // Send to admin if assigned
        if (conversation.adminId) {
          io.to(`user_${conversation.adminId}`).emit("new_message", messageData);
        }

        // Also notify admin room about new conversation/message
        if (socket.userRole !== "admin") {
          io.to("admin_room").emit("conversation_updated", {
            conversationId: conversation._id,
            userId: conversation.userId,
            lastMessage: conversation.lastMessage,
            lastMessageAt: conversation.lastMessageAt,
            unreadCountForAdmin: conversation.unreadCountForAdmin,
          });
        }

        socket.emit("message_sent", { messageId: message._id });
      } catch (error) {
        socket.emit("error", { message: "Failed to send message" });
      }
    });

    // Handle marking messages as read
    socket.on("mark_as_read", async (data) => {
      try {
        const { conversationId } = data;

        if (!conversationId) {
          socket.emit("error", { message: "Conversation ID is required" });
          return;
        }

        const conversation = await Conversation.findById(conversationId);
        if (!conversation) {
          socket.emit("error", { message: "Conversation not found" });
          return;
        }

        // Update unread counts
        if (socket.userRole === "admin") {
          await Message.updateMany(
            {
              conversationId: conversation._id,
              senderId: { $ne: socket.userId },
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
              conversationId: conversation._id,
              senderId: { $ne: socket.userId },
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

        // Notify other participant
        if (socket.userRole === "admin") {
          io.to(`user_${conversation.userId}`).emit("messages_read", {
            conversationId: conversation._id,
          });
        } else if (conversation.adminId) {
          io.to(`user_${conversation.adminId}`).emit("messages_read", {
            conversationId: conversation._id,
          });
        }
      } catch (error) {
        socket.emit("error", { message: "Failed to mark as read" });
      }
    });

    // Handle disconnect
    socket.on("disconnect", () => {
    });
  });

  return io;
};

/**
 * Get Socket.IO instance
 */
const getIO = () => {
  if (!io) {
    throw new Error("Socket.IO not initialized");
  }
  return io;
};

module.exports = {
  initializeSocket,
  getIO,
};

