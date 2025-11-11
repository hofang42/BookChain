require("dotenv").config();

const chatAIConfig = {
  apiKey: process.env.CHATAI_API_KEY,
  baseUrl: process.env.CHATAI_BASE_URL,
  model: process.env.CHATAI_MODEL || 'gemini-2.0-flash'
};

module.exports = chatAIConfig;
