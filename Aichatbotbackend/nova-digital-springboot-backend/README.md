# NOVA DIGITAL – Spring Boot Backend

Java 17 + Spring Boot + MySQL + Gemini API backend starter for the NOVA DIGITAL React/TanStack frontend.

## Endpoints

- `GET http://localhost:8080/api/health`
- `POST http://localhost:8080/api/chat`
- `POST http://localhost:8080/api/contact`

### Chat request
```json
{
  "message": "What services do you offer?",
  "history": [
    {"role": "user", "content": "Hello"},
    {"role": "assistant", "content": "Hello! How can I help?"}
  ]
}
```
Response:
```json
{"reply":"NOVA DIGITAL offers ..."}
```

### Contact request
```json
{
  "name":"Arun",
  "email":"arun@example.com",
  "subject":"Website enquiry",
  "message":"I need a company website."
}
```

## Setup

1. Install JDK 17+, Maven 3.9+, and MySQL 8+.
2. Create database (optional; `createDatabaseIfNotExist=true` is enabled):
   ```sql
   CREATE DATABASE nova_digital;
   ```
3. Copy `.env.example` values into your IDE Run Configuration environment variables or Windows environment variables. Set your actual `DB_PASSWORD` and `GEMINI_API_KEY`.
4. Get a Gemini key from https://aistudio.google.com/apikey. Restrict it to Gemini API. Do not put it in React, VITE_ variables, Git, or a public file.
5. Run:
   ```bash
   mvn spring-boot:run
   ```

## Connect existing frontend

The current ZIP's frontend calls `sendChatMessage()` and `submitContactMessage()` from Base44 server functions. This backend is separate, so those calls must be changed to HTTP requests to Spring Boot.

Chat request from the browser:
```js
const response = await fetch("http://localhost:8080/api/chat", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    "X-Chat-Session": sessionId
  },
  body: JSON.stringify({ message: text, history })
});
if (!response.ok) throw new Error("Chat request failed");
const result = await response.json();
```

Contact request:
```js
await fetch("http://localhost:8080/api/contact", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(values)
});
```

Set `FRONTEND_URL` to the actual frontend origin in development/production. For production, configure HTTPS, stronger rate limiting (Redis or gateway), abuse protection, request logging without secrets, and database migrations (Flyway/Liquibase). Do not expose an admin endpoint to read contact/chat records without authentication.

## Notes

- The Gemini model is configurable through `GEMINI_MODEL`; if the selected model is unavailable for your key/project, set it to a currently supported Flash model in Google AI Studio.
- This starter stores contact submissions and chatbot exchanges in MySQL.
- It does not include an admin dashboard, email notifications, authentication, or production-grade distributed rate limiting yet.
