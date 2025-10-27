# Integrating the Backend (BE) with the Android Frontend (FE)

This document explains how to connect the Android app to the Node.js + MongoDB backend included in this repository. It covers: starting the backend, available endpoints, emulator host addresses, how to configure the app, and example Retrofit code for calls.

## Overview

- Backend: `/BookChain/backend` (Node.js + Express + Mongoose)
- Frontend: `/BookChain/app` (Android app)

This guide assumes you run the backend locally on port `3000` for development.

## Backend — Quick start

1. Open a terminal in `backend` folder.
2. Create a `.env` file or copy `.env.example` and set `MONGODB_URI`.

```powershell
cd e:\Code\AndroidStudio\BookChain\backend
npm install
# create .env with MONGODB_URI; example is in .env.example
npm run dev   # starts nodemon (development)
```

3. Confirm API is reachable on the host machine:

```powershell
curl http://localhost:3000/health
curl http://localhost:3000/api/users
```

If using MongoDB Atlas, ensure `MONGODB_URI` points to your Atlas cluster and backend can connect.

## Important backend endpoints (development)

- `GET /health` — health check
- `GET /api/users` — list users (development example returns array)
- `GET /api/books` — list books
- `GET /api/branches` — list branches

> Note: For production or authenticated flows you'll want endpoints such as `POST /api/auth/login` and `GET /api/users/me`.

## Android — emulator networking notes

- Android Emulator (AVD, standard): use `http://10.0.2.2:3000` to reach your host machine.
- Genymotion emulator: use `http://10.0.3.2:3000`.
- Physical device: use your host machine LAN IP (e.g. `http://192.168.1.100:3000`) and ensure firewall allows incoming connections.
- For easy public URLs (testing on remote devices), use `ngrok http 3000` and use the generated HTTPS URL.

## Configure base URL in the app

We added a central `ApiConfig` class in the project:

`app/src/main/java/com/hofang/bookchainfe/network/ApiConfig.java`

Edit `BASE_URL` in `ApiConfig` when switching between emulator / device / production.

## Recommended approach: Retrofit + OkHttp

Using Retrofit simplifies JSON parsing, threading and error handling. Add these Gradle dependencies in `app/build.gradle.kts` (inside `dependencies`):

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
```

Sync the project after adding dependencies.

### Retrofit client (example)

Create a Retrofit singleton (suggested path: `app/src/main/java/com/hofang/bookchainfe/network/NetworkClient.java`):

```java
package com.hofang.bookchainfe.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class NetworkClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
}
```

### API interface (example)

Create `UserService`:

```java
package com.hofang.bookchainfe.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface UserService {
    @GET("/api/users")
    Call<List<UserDto>> listUsers();
}
```

### User DTO (example)

Create a simple `UserDto` model matching backend JSON (path: `app/src/main/java/.../model/UserDto.java`):

```java
package com.hofang.bookchainfe.model;

public class UserDto {
    public String _id;
    public String username;
    public String fullName;
    public String email;
    public String phone;
    // add other fields used in frontend
}
```

### Using Retrofit in the fragment

Example usage in `AccountFragment` (replace existing HttpURLConnection approach):

```java
UserService svc = NetworkClient.getRetrofit().create(UserService.class);
svc.listUsers().enqueue(new Callback<List<UserDto>>() {
    @Override
    public void onResponse(Call<List<UserDto>> call, Response<List<UserDto>> response) {
        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
            UserDto u = response.body().get(0);
            // update UI on main thread
        }
    }
    @Override
    public void onFailure(Call<List<UserDto>> call, Throwable t) {
        // handle error
    }
});
```

## CORS & backend config

- Backend currently sets `cors()` for development (open to all origins). In production, restrict allowed origins.
- Ensure backend is running and connected to the correct DB (`.env -> MONGODB_URI`).

## Testing with Postman / Browser

- From host machine: `http://localhost:3000/api/users` should return JSON.
- From Android emulator (browser): `http://10.0.2.2:3000/api/users` should return the same JSON.

## Debugging checklist

1. Backend logs: check console where you started `npm run dev` — requests should be visible.
2. Android Logcat: look for tag `AccountFragment` or OkHttp `HTTP` logs when using the logging interceptor.
3. If the app shows stale or default values:
   - Rebuild & reinstall the app on the emulator.
   - Confirm emulator is hitting the host (use emulator browser).

## Security notes

- Never commit `.env` with credentials. Use environment variables or secret storage.
- Use HTTPS for production by deploying backend behind TLS (nginx, cloud provider) or using ngrok HTTPS for testing.
- Add authentication (JWT) for user-specific endpoints, and store tokens securely on the device (EncryptedSharedPreferences).

## Next steps & examples

- I can convert the app networking to Retrofit and add the `NetworkClient`, `UserService`, and DTO classes for you.
- I can also add a sample `login` flow and a `GET /api/users/me` endpoint in the backend for authenticated user profile retrieval.

---

If you want, I can now implement the Retrofit client and sample DTOs in the Android project (I recommend this). Tell me to proceed and I'll create the files and update `AccountFragment` to use Retrofit.
