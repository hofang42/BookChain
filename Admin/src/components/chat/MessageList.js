import React from "react";
import "./MessageList.css";

const MessageList = ({ messages }) => {
  const adminUser = JSON.parse(localStorage.getItem("adminUser") || "{}");
  const adminId = adminUser._id;

  const formatTime = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="message-list">
      {messages.length === 0 ? (
        <div className="empty-messages">
          <p>No messages yet. Start the conversation!</p>
        </div>
      ) : (
        messages.map((message) => {
          const senderId = typeof message.senderId === 'object' 
            ? message.senderId?._id 
            : message.senderId;
          const isAdmin = senderId === adminId || 
                         message.senderId?.role === "admin";
          return (
            <div
              key={message._id}
              className={`message ${isAdmin ? "message-sent" : "message-received"}`}
            >
              <div className="message-content">
                <div className="message-text">{message.content}</div>
                <div className="message-time">
                  {formatTime(message.createdAt)}
                </div>
              </div>
            </div>
          );
        })
      )}
    </div>
  );
};

export default MessageList;

