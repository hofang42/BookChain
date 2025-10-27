BookChain backend (Node.js + Express)

Quick start:

1. From `backend` folder install deps:
   npm install

2. Start in development:
   npm run dev

Database:

Set `MONGODB_URI` in a `.env` file (see `.env.example`). Example:

MONGODB_URI=mongodb://localhost:27017/bookchain

The app will connect to MongoDB on start. If the connection fails, the process will exit.

Endpoints:

- GET /health -> simple health check
- GET /api/users -> example users route
- GET /api/books -> list books (supports ?q=search)
- GET /api/branches -> list branches

Android client notes:

- When running the Android emulator, use `http://10.0.2.2:3000` as host for the backend (Android emulator maps 10.0.2.2 to your host machine).
- For Genymotion use `http://10.0.3.2:3000`.
- For a physical device, set `MONGODB_URI`/server to an IP reachable by the device (e.g. `http://192.168.1.100:3000`).
- For quick public access from device, consider using `ngrok` and point Android app to the generated HTTPS URL.

Files created by scaffold:

- `package.json`, `.gitignore`, `.env.example`, `README.md`
- `src/index.js`, `src/routes/`, `src/controllers/`, `src/models/`, `src/config/`, `src/middleware/`, `src/utils/`
