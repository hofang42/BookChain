# BookChain Admin Panel

React admin panel for managing BookChain system, focusing on chat functionality.

## Features

- **Dashboard**: View statistics (users, books, orders, revenue, conversations)
- **Chat System**: Real-time chat with users using Socket.IO
  - View all active conversations
  - Assign yourself to conversations
  - Send and receive messages in real-time
  - Mark messages as read
  - Close conversations

## Setup

1. Install dependencies:
```bash
npm install
```

2. Start development server:
```bash
npm start
```

The app will run on `http://localhost:3001`

## Configuration

By default, the app connects to:
- API: `http://localhost:3000`
- Socket.IO: `http://localhost:3000`

To change these, create a `.env` file:
```
REACT_APP_API_URL=http://localhost:3000
REACT_APP_SOCKET_URL=http://localhost:3000
```

## Login

Use an admin account to login. The admin account must have `role: "admin"` in the database.

## Build for Production

```bash
npm run build
```

The built files will be in the `dist/` folder.

