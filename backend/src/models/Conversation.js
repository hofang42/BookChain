const mongoose = require("mongoose");
const { Schema } = mongoose;

const conversationSchema = new Schema(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      required: true,
      index: true,
    },
    adminId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      index: true,
    },
    status: {
      type: String,
      enum: ["active", "closed", "pending"],
      default: "active",
    },
    lastMessage: {
      type: String,
    },
    lastMessageAt: {
      type: Date,
    },
    unreadCount: {
      type: Number,
      default: 0,
    },
    unreadCountForUser: {
      type: Number,
      default: 0,
    },
    unreadCountForAdmin: {
      type: Number,
      default: 0,
    },
  },
  { timestamps: true }
);

// Index for efficient querying
conversationSchema.index({ userId: 1, status: 1 });
conversationSchema.index({ adminId: 1, status: 1 });
conversationSchema.index({ lastMessageAt: -1 });

module.exports = mongoose.model("Conversation", conversationSchema);

