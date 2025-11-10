const express = require("express");
const http = require("http");
const dotenv = require("dotenv");
const usersRoutes = require("./routes/users");
const booksRoutes = require("./routes/books");
const branchesRoutes = require("./routes/branches");
const authRoutes = require("./routes/auth");
const chatRoutes = require("./routes/chat");
const cors = require("cors");
const { errorHandler } = require("./middleware/errorHandler");
const connectDB = require("./services/db");
const { initializeSocket } = require("./services/socketService");

dotenv.config({ path: './.env' });

const app = express();
app.use(express.json());
// Enable CORS for all origins during development. Restrict in production.
app.use(cors());

app.get("/health", (req, res) => res.json({ status: "ok" }));

app.use("/api/auth", authRoutes);
app.use("/api/users", usersRoutes);
app.use("/api/books", booksRoutes);
app.use("/api/branches", branchesRoutes);
app.use("/api/chat", chatRoutes);

app.use(errorHandler);

const PORT = process.env.PORT || 3000;

(async () => {
  try {
    await connectDB();
    
    // Create HTTP server
    const server = http.createServer(app);
    
    // Initialize Socket.IO
    initializeSocket(server);
    
    server.listen(PORT, () =>
      console.log(`Server listening on http://localhost:${PORT}`)
    );
  } catch (err) {
    console.error("Failed to start server:", err);
    process.exit(1);
  }
})();
