import axios from "axios";

const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:3000";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Add token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("adminToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("adminToken");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export const chatAPI = {
  getConversations: (status = "active", page = 1, limit = 20) =>
    api.get(`/api/chat/conversations`, { params: { status, page, limit } }),

  getMessages: (conversationId, page = 1, limit = 50) =>
    api.get(`/api/chat/conversation/${conversationId}/messages`, {
      params: { page, limit },
    }),

  assignAdmin: (conversationId) =>
    api.post(`/api/chat/conversation/${conversationId}/assign`),

  closeConversation: (conversationId) =>
    api.post(`/api/chat/conversation/${conversationId}/close`),
};

export const authAPI = {
  login: (email, password) =>
    api.post("/api/auth/login", { login: email, password }),
};

export default api;

