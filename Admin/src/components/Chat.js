import React, { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { connectSocket, disconnectSocket, getSocket } from "../services/socket";
import { chatAPI } from "../services/api";
import ConversationList from "./chat/ConversationList";
import ChatWindow from "./chat/ChatWindow";
import "./Chat.css";

const Chat = ({ setIsAuthenticated }) => {
  const navigate = useNavigate();
  const [conversations, setConversations] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const socketRef = useRef(null);

  useEffect(() => {
    const token = localStorage.getItem("adminToken");
    if (!token) {
      navigate("/login");
      return;
    }

    try {
      // Connect socket
      socketRef.current = connectSocket(token);
      const socket = socketRef.current;

      if (!socket) {
        console.error("Failed to connect socket");
        setLoading(false);
        return;
      }

      // Load conversations via HTTP API first
      loadConversations();

      // Setup socket event listeners
      const handleConversationsList = (data) => {
        console.log("Received conversations_list:", data);
        if (Array.isArray(data)) {
          setConversations(data);
        } else {
          setConversations([]);
        }
        setLoading(false);
      };

      const handleConversationUpdated = () => {
        console.log("Conversation updated, reloading...");
        loadConversations();
      };

      const handleError = (error) => {
        console.error("Socket error:", error);
        setLoading(false);
      };

      const handleConnect = () => {
        console.log("Socket connected, requesting conversations...");
        // Request conversations list when socket is connected
        if (socket.connected) {
          socket.emit("get_conversation");
        }
      };

      // Listen for socket events
      socket.on("connect", handleConnect);
      socket.on("conversations_list", handleConversationsList);
      socket.on("conversation_updated", handleConversationUpdated);
      socket.on("error", handleError);

      // If socket is already connected, request conversations immediately
      if (socket.connected) {
        socket.emit("get_conversation");
      }

      return () => {
        if (socket) {
          socket.off("connect", handleConnect);
          socket.off("conversations_list", handleConversationsList);
          socket.off("conversation_updated", handleConversationUpdated);
          socket.off("error", handleError);
        }
        disconnectSocket();
      };
    } catch (error) {
      console.error("Error initializing chat:", error);
      setLoading(false);
    }
  }, [navigate, loadConversations]);

  // Separate useEffect for new_message listener with selectedConversation dependency
  useEffect(() => {
    const socket = socketRef.current;
    if (!socket) return;

    const handleNewMessage = (message) => {
      if (
        selectedConversation &&
        (message.conversationId === selectedConversation._id ||
         message.conversationId?.toString() === selectedConversation._id?.toString())
      ) {
        setMessages((prev) => [...prev, message]);
      }
      // Update conversation list
      loadConversations();
    };

    socket.on("new_message", handleNewMessage);

    return () => {
      socket.off("new_message", handleNewMessage);
    };
  }, [selectedConversation, loadConversations]);

  useEffect(() => {
    if (selectedConversation) {
      loadMessages(selectedConversation._id);
      // Mark as read
      const socket = getSocket();
      if (socket) {
        socket.emit("mark_as_read", { conversationId: selectedConversation._id });
      }
    }
  }, [selectedConversation]);

  const loadConversations = useCallback(async () => {
    try {
      console.log("Loading conversations via API...");
      const response = await chatAPI.getConversations("active");
      console.log("API response:", response.data);
      if (response.data.success) {
        const conversations = response.data.data?.conversations || [];
        console.log("Loaded conversations:", conversations.length);
        setConversations(conversations);
      } else {
        console.warn("API returned unsuccessful response:", response.data);
        setConversations([]);
      }
    } catch (error) {
      console.error("Error loading conversations:", error);
      if (error.response) {
        console.error("Error response:", error.response.data);
      }
      setConversations([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const loadMessages = async (conversationId) => {
    try {
      const response = await chatAPI.getMessages(conversationId);
      if (response.data.success) {
        setMessages(response.data.data?.messages || []);
      }
    } catch (error) {
      console.error("Error loading messages:", error);
      setMessages([]);
    }
  };

  const handleSelectConversation = async (conversation) => {
    // If conversation doesn't have admin assigned, assign current admin
    if (!conversation.adminId) {
      try {
        const response = await chatAPI.assignAdmin(conversation._id);
        if (response.data.success) {
          conversation = response.data.data;
          loadConversations();
        }
      } catch (error) {
        console.error("Error assigning admin:", error);
      }
    }
    setSelectedConversation(conversation);
  };

  const handleSendMessage = (content) => {
    const socket = getSocket();
    if (socket && selectedConversation) {
      socket.emit("send_message", {
        conversationId: selectedConversation._id,
        content,
        messageType: "text",
      });
    }
  };

  const handleCloseConversation = async () => {
    if (selectedConversation) {
      try {
        await chatAPI.closeConversation(selectedConversation._id);
        loadConversations();
        setSelectedConversation(null);
        setMessages([]);
      } catch (error) {
        console.error("Error closing conversation:", error);
      }
    }
  };

  const handleLogout = () => {
    disconnectSocket();
    localStorage.removeItem("adminToken");
    localStorage.removeItem("adminUser");
    setIsAuthenticated(false);
  };

  return (
    <div className="chat-container">
      <header className="chat-header">
        <div className="header-content">
          <h1>Admin Chat</h1>
          <div className="header-actions">
            <button onClick={() => navigate("/")} className="btn-secondary">
              Dashboard
            </button>
            <button onClick={handleLogout} className="btn-secondary">
              Logout
            </button>
          </div>
        </div>
      </header>

      <div className="chat-main">
        <ConversationList
          conversations={conversations}
          selectedConversation={selectedConversation}
          onSelectConversation={handleSelectConversation}
          loading={loading}
        />
        <ChatWindow
          conversation={selectedConversation}
          messages={messages}
          onSendMessage={handleSendMessage}
          onCloseConversation={handleCloseConversation}
        />
      </div>
    </div>
  );
};

export default Chat;

