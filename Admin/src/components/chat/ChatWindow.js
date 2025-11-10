import React, { useState, useEffect, useRef } from "react";
import MessageList from "./MessageList";
import MessageInput from "./MessageInput";
import "./ChatWindow.css";

const ChatWindow = ({
  conversation,
  messages,
  onSendMessage,
  onCloseConversation,
}) => {
  const messagesEndRef = useRef(null);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  if (!conversation) {
    return (
      <div className="chat-window empty">
        <div className="empty-state">
          <div className="empty-icon">💬</div>
          <h3>Select a conversation</h3>
          <p>Choose a conversation from the list to start chatting</p>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-window">
      <div className="chat-window-header">
        <div className="user-info">
          <div className="user-avatar">
            {conversation.userId?.fullName?.charAt(0)?.toUpperCase() || "U"}
          </div>
          <div>
            <div className="user-name">
              {conversation.userId?.fullName || "Unknown User"}
            </div>
            <div className="user-email">
              {conversation.userId?.email || ""}
            </div>
          </div>
        </div>
        <button
          onClick={onCloseConversation}
          className="close-conversation-btn"
          title="Close conversation"
        >
          ✕
        </button>
      </div>

      <MessageList messages={messages} />
      <div ref={messagesEndRef} />

      <MessageInput onSendMessage={onSendMessage} />
    </div>
  );
};

export default ChatWindow;

