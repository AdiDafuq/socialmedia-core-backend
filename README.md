## 🧱 Architecture Overview

This project is a Spring Boot microservice built as a backend API gateway with persistence, caching, and request protection layers.

### Core Components:

- **Spring Boot (REST API Layer)**
  - Handles incoming HTTP requests
  - Business logic + validation layer

- **PostgreSQL (Primary Database)**
  - Stores persistent application data
  - Runs in Docker container

- **Redis (Guardrails + Cache Layer)**
  - Rate limiting
  - Request throttling
  - Temporary data storage (cache / cooldowns)

### Flow:

Client → Spring Boot API → Redis (check limits) → PostgreSQL (store/fetch data)

If Redis says “you’ve done enough damage for now”, request is blocked.

---

## 🛡️ Redis Guardrails System

Redis is used as a real-time control layer to prevent abuse and overload.

### 1. Rate Limiting

- Each user/request is tracked using Redis keys
- Example key format:

user:{id}:requests


- Requests increment counters in Redis
- TTL (expiry) resets limits automatically

### 2. Cooldown System

- Prevents spam-like repeated actions
- Example:

user:{id}:cooldown


- After a request, user enters cooldown window (e.g., 15 minutes)
- Redis TTL handles automatic reset

### 3. Why Redis?

Because storing “how often someone is abusing your API” in a database would be slow, expensive, and emotionally draining.

---

## ⚙️ Concurrency Handling

This system assumes users are not polite.

### 1. Atomic Operations in Redis
- Redis `INCR` is used for counters
- Ensures race conditions don’t break limits

### 2. Single Source of Truth
- Redis handles real-time decisions
- PostgreSQL is only used for persistence

### 3. Transaction Safety (DB layer)
- Spring `@Transactional` used where needed
- Prevents partial writes in case of failures

### 4. Stateless API Design
- Each request is independent
- No server-side session dependency

This allows horizontal scaling without breaking logic.

---

## 🐳 Infrastructure

Docker Compose is used to run dependencies:

- PostgreSQL → `localhost:5432`
- Redis → `localhost:6379`

---

## 🚀 Summary

This backend is designed to:
- Handle concurrent requests safely
- Prevent abuse using Redis guardrails
- Scale horizontally without state issues

In short:
It works, it resists spam, and it doesn’t collapse under basic load. Which is already better than most production systems pretending to be fine.
