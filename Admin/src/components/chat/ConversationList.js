import React from "react";
import "./ConversationList.css";

const ConversationList = ({
  conversations,
  selectedConversation,
  onSelectConversation,
  loading,
}) => {
  const formatTime = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);

    if (minutes < 1) return "Just now";
    if (minutes < 60) return `${minutes}m ago`;
    if (minutes < 1440) return `${Math.floor(minutes / 60)}h ago`;
    return date.toLocaleDateString();
  };

  if (loading) {
    return (
      <div className="conversation-list">
        <div className="loading">Loading conversations...</div>
      </div>
    );
  }

  return (
    <div className="conversation-list">
      <div className="conversation-list-header">
        <h2>Conversations</h2>
        <span className="conversation-count">{conversations.length}</span>
      </div>
      <div className="conversations">
        {conversations.length === 0 ? (
          <div className="empty-state">No active conversations</div>
        ) : (
          conversations.map((conversation) => {
            const isSelected =
              selectedConversation &&
              selectedConversation._id === conversation._id;
            const unreadCount = conversation.unreadCountForAdmin || 0;

            return (
              <div
                key={conversation._id}
                className={`conversation-item ${isSelected ? "selected" : ""}`}
                onClick={() => onSelectConversation(conversation)}
              >
                <div className="conversation-avatar">
                  {conversation.userId?.fullName?.charAt(0)?.toUpperCase() ||
                    "U"}
                </div>
                <div className="conversation-info">
                  <div className="conversation-header">
                    <span className="conversation-name">
                      {conversation.userId?.fullName || "Unknown User"}
                    </span>
                    {conversation.lastMessageAt && (
                      <span className="conversation-time">
                        {formatTime(conversation.lastMessageAt)}
                      </span>
                    )}
                  </div>
                  <div className="conversation-preview">
                    <span className="conversation-message">
                      {conversation.lastMessage || "No messages yet"}
                    </span>
                    {unreadCount > 0 && (
                      <span className="unread-badge">{unreadCount}</span>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default ConversationList;

