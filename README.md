# Scaler Store :: Enterprise Microservices E-Commerce Platform

An enterprise-grade, event-driven Spring Boot microservices platform adhering to modern cloud-native architectures, distributed transactions, asynchronous Kafka messaging, multi-tier polyglot persistence, and intelligent caching.

Developed in fulfillment of the requirements for the Master of Science in Computer Science at **Scaler Neovarsity - Woolf**.

---

## 🏛️ System Architecture

```
                                   +--------------------------------+
                                   |         Client Devices         |
                                   |      (Web, Mobile, Postman)    |
                                   +---------------+----------------+
                                                   |
                                                   v [HTTP / 8080]
                                   +--------------------------------+
                                   |    API Gateway (Spring Cloud)  |
                                   |    - Route Matching            |
                                   |    - JWT Authentication Filter |
                                   |    - CORS & Rate Limiting      |
                                   +---------------+----------------+
                                                   |
         +-----------------+-----------------------+------------------------+-----------------+
         |                 |                       |                        |                 |
         v                 v                       v                        v                 v
+-----------------+ +-------------------+ +-------------------+  +--------------------+ +-------------------+
|  User Service   | | Product Service   | |   Cart Service    |  |   Order Service    | |  Payment Service  |
|  (Port: 8081)   | |  (Port: 8082)     | |   (Port: 8083)    |  |   (Port: 8084)     | |  (Port: 8085)     |
+--------+--------+ +---------+---------+ +---------+---------+  +---------+----------+ +---------+---------+
         |                    |                     |                      |                      |
         | MySQL / H2         | MySQL / H2          | MongoDB / In-Memory  | MySQL / H2           | MySQL / H2
         | user_db            | product_db          | cart_db              | order_db             | payment_db
         |                    | Redis Cache         | Redis Cache          |                      |
         |                    |                     |                      |                      |
         v                    v                     v                      v                      v
+-----------------------------------------------------------------------------------------------------------+
|                                    Apache Kafka Event Broker                                              |
|   Topics: [user-events]                  [order-events]                  [payment-events]                 |
+--------------------------------------------------+--------------------------------------------------------+
                                                   |
                                                   v
                                   +--------------------------------+
                                   |      Notification Service      |
                                   |          (Port: 8086)          |
                                   |  - Welcome Emails              |
                                   |  - Order Confirmations         |
                                   |  - Payment Receipts            |
                                   +--------------------------------+
```

---

## 🧠 Conceptual Deep-Dive: What is What?

### 1. What is Docker & Docker Compose?
- **Docker:** A platform that packages an application and all its system libraries, binaries, configuration files, and dependencies into an isolated, lightweight container. Unlike Virtual Machines (VMs) which emulate entire hardware and guest operating systems, Docker containers share the host Linux/Windows kernel, making them start in seconds and consume minimal memory.
- **Docker Compose:** A multi-container orchestration tool defined via a single YAML file (`docker-compose.yml`). It allows launching 14 distinct containers (MySQL, MongoDB, Redis, Kafka, Zookeeper, Elasticsearch, all 8 microservices, and Nginx frontend) with one command: `docker compose up -d`.
- **When is Docker needed?** Docker is ideal for production and containerized staging. However, if Docker Desktop is not installed on your local development machine, this project includes a **Dual-Mode Architecture**: every service automatically boots locally in standalone mode using embedded H2 in-memory MySQL mode and resilient in-memory stores with zero external downloads!

### 2. What is Netflix Eureka (Service Registry)?
- In microservices, services scale horizontally and get dynamic IP addresses and ephemeral ports. Rather than hardcoding IP addresses in client configurations, services register themselves upon boot with **Eureka Server** (`http://localhost:8761`).
- When a client sends a request to the **API Gateway**, the Gateway queries Eureka: *"Where is an instance of `product-service`?"* Eureka provides the live IP and port (`127.0.0.1:8082`), allowing dynamic client-side load balancing.

### 3. What is Spring Cloud API Gateway?
- The single entry point (`http://localhost:8080`) for all incoming traffic from web browsers, mobile apps, and third-party clients.
- It provides:
  1. **Route Forwarding:** Maps public routes like `/api/products/**` to `product-service` and `/api/cart/**` to `cart-service`.
  2. **JWT Security Filter (`JwtAuthenticationFilter`):** Intercepts requests, validates Bearer tokens, extracts `userId` and `roles`, and injects security headers (`X-User-Id`, `X-User-Email`) into downstream requests.
  3. **Global CORS:** Allows the web frontend on port 3000 to call backend APIs without cross-origin browser blocks.

### 4. What is Apache Kafka & Event-Driven Architecture?
- In a traditional monolithic or synchronous REST architecture, when a user checks out, `Order Service` would call `Payment Service` via HTTP, which calls `Notification Service` via HTTP. If `Payment Service` is slow or `Notification Service` is down, the checkout crashes!
- **Apache Kafka** decouples services:
  1. User checks out: `Order Service` saves the order and immediately publishes an `OrderPlacedEvent` to the Kafka topic `order-events`.
  2. `Payment Service` listens to `order-events`, charges the card, and emits `PaymentStatusEvent(SUCCESS)` on `payment-events`.
  3. `Order Service` receives the success event and confirms the order (`CONFIRMED`).
  4. `Notification Service` consumes the event and dispatches email receipts asynchronously.

### 5. Why Polyglot Persistence?
- **MySQL / Relational DB:** Used for Users, Orders, and Payments where strict ACID compliance, relational integrity, and foreign keys are mandatory to prevent financial inconsistency.
- **MongoDB / Document DB:** Used for the Shopping Cart because shopping carts have nested, polymorphic items and dynamic attributes that change frequently without requiring rigid table schema migrations.
- **Redis / In-Memory Cache:** Stores transient cart sessions and frequently viewed product catalog pages in RAM, delivering responses in under 2 milliseconds.
- **Elasticsearch:** A distributed JSON search engine indexing product titles and descriptions for fuzzy matching, typos, and high-speed faceted search.

---

## 🧰 Technology Stack Summary

| Layer | Component | Port | Description |
| :--- | :--- | :--- | :--- |
| **Service Registry** | Netflix Eureka | `8761` | Dynamic microservice discovery and health registry |
| **API Gateway** | Spring Cloud Gateway | `8080` | Non-blocking reactive gateway with JWT authentication filter |
| **User Service** | Spring Boot 3.3.4 | `8081` | Authentication, JWT HS512 tokens, BCrypt hashing |
| **Product Service** | Spring Boot 3.3.4 | `8082` | Product catalog, Redis caching, category management |
| **Cart Service** | Spring Boot 3.3.4 | `8083` | Shopping cart with MongoDB and Redis session fallback |
| **Order Service** | Spring Boot 3.3.4 | `8084` | SAGA checkout, order lifecycle tracking |
| **Payment Service** | Spring Boot 3.3.4 | `8085` | Payment ledger, digital receipts, SAGA compensation |
| **Notification Service** | Spring Boot 3.3.4 | `8086` | SES simulator, user & order event audit logs |
| **Web Frontend** | Vanilla HTML5 / CSS3 / JS | `3000` | Modern, responsive e-commerce shopping interface |

---

## 🚀 How to Run and Test the Application

### Option A: 1-Click Local Runner (Recommended - Zero Installation Needed!)

The project has been pre-packaged with self-contained executable JARs and in-memory database modes. You do **not** need to install MySQL, MongoDB, Redis, or Kafka to run the full application!

1. Open File Explorer and navigate to:
   ```
   d:\Scaler Store
   ```
2. Double-click **`start-all.bat`**:
   - Launches Eureka Service Registry on port `8761`.
   - Launches API Gateway on port `8080`.
   - Launches all 6 backend services on ports `8081`–`8086`.
   - Launches the Web Frontend on port `3000`.
   - Automatically opens `http://localhost:3000` in your default browser!
3. To stop all services cleanly at any time, double-click **`stop-all.bat`**.

---

### Option B: Visual Testing via Web Frontend (`http://localhost:3000`)

1. Open your browser at **`http://localhost:3000`**.
2. **Sign Up / Sign In:**
   - Click the **"Sign In"** button on the top right.
   - Switch to **"Create Account"** tab.
   - Enter your name, email (e.g. `alex@scalerstore.com`), and password (`Password@123`).
   - Click **Create Account** -> you are instantly authenticated with a secure JWT token!
3. **Browse Catalog:**
   - View seeded products (MacBook Pro M3 Max, Sony WH-1000XM5, Dell 4K Monitor, Nike Shoes, etc.).
   - Use the search bar to search for `"MacBook"` or filter by categories.
4. **Add to Cart:**
   - Click **"Add to Cart"** on any item.
   - The cart drawer badge updates in real-time.
5. **Checkout & SAGA Order:**
   - Click the Cart icon on the top right.
   - Adjust quantities if desired, and click **"Proceed to Checkout"**.
   - Enter your shipping address and select payment method (Credit Card, UPI, etc.).
   - Click **"Place Order & Pay"**.
   - Your order number (e.g. `ORD-20261114...`) is confirmed!
6. **Track Your Order:**
   - Click **"Track Order"** on the navbar and enter your order number to see live status (`PLACED` / `CONFIRMED`).
7. **View Notification Audit Log:**
   - Click the Bell icon on the top right to view simulated AWS SES email dispatches (Welcome emails, Order confirmations, Payment receipts).

---

### Option C: API Testing via Postman Collection

A complete Postman collection is included in the project root:
`d:\Scaler Store\Scaler_Store_API.postman_collection.json`

1. Open **Postman** and click **Import**.
2. Select `d:\Scaler Store\Scaler_Store_API.postman_collection.json`.
3. Execute the requests sequentially:
   - `1. Register User` -> returns user profile and JWT token.
   - `2. Login` -> saves Bearer token to collection variable.
   - `3. Get Categories & Products` -> returns catalog from port 8080.
   - `4. Add Item to Cart` -> updates MongoDB / in-memory cart.
   - `5. Checkout Order` -> triggers SAGA distributed transaction.
   - `6. Track Order` -> retrieves order state by order number.
   - `7. Process Payment` -> records payment transaction ledger.
   - `8. Notification History` -> retrieves email audit records.

---

### Option D: Running with Docker Desktop (Optional Full Container Stack)

If you have or want to install **Docker Desktop** for the multi-database containerized stack:

1. **What to Download:**
   - Download **Docker Desktop for Windows**: [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
   - Ensure **WSL 2 (Windows Subsystem for Linux)** is enabled during installation.
2. **Start Docker Desktop** and wait for the Docker engine icon to turn green.
3. Open a terminal in `d:\Scaler Store` and run:
   ```powershell
   docker compose up -d
   ```
4. Verify all containers are running:
   ```powershell
   docker ps
   ```
5. All 14 containers (MySQL, MongoDB, Redis, Kafka, Zookeeper, Elasticsearch, all 8 microservices, Nginx frontend) will run isolated in Docker!

---

## 📂 Project Directory Structure

```
d:\Scaler Store
├── .mvn/                       # Maven wrapper configuration
├── .tools/                     # Portable Apache Maven 3.9.9 binary
├── api-gateway/                # Spring Cloud Gateway (Port 8080)
├── cart-service/               # Cart Service with MongoDB & Redis (Port 8083)
├── common-events/              # Shared event models (Kafka schemas) and DTOs
├── frontend/                   # Modern responsive Web UI (Port 3000)
│   ├── index.html              # Single Page Application HTML
│   ├── styles.css              # Custom responsive stylesheet
│   ├── app.js                  # Gateway REST client & state management
│   └── start-frontend.bat      # 1-click frontend runner
├── init-scripts/               # MySQL database initialization script
├── notification-service/       # Notification Service & SES Simulator (Port 8086)
├── order-service/              # Order Service & SAGA orchestrator (Port 8084)
├── payment-service/            # Payment Service & Transaction Ledger (Port 8085)
├── product-service/            # Product Catalog Service (Port 8082)
├── report_assets/              # High-resolution architectural figures for report
├── service-registry/           # Netflix Eureka Service Discovery (Port 8761)
├── user-service/               # User Authentication & JWT Service (Port 8081)
├── docker-compose.yml          # 14-container Docker orchestration manifest
├── pom.xml                     # Multi-module Maven root project POM
├── README.md                   # Complete architectural guide & documentation
├── Scaler_Neovarsity_Applied_Project_Report.docx # Master's Applied Project Report (565 KB)
├── SCALER_PROJECT_REPORT.md    # Markdown dissertation report
├── Scaler_Store_API.postman_collection.json     # Ready-to-run Postman collection
├── start-all.bat               # 1-click launcher for all microservices & UI
└── stop-all.bat                # 1-click shutdown script for all ports
```

---

## 🎓 Academic Report Artifacts

In fulfillment of the Master of Science dissertation requirements at **Scaler Neovarsity - Woolf**:
- **Microsoft Word Dissertation:** `d:\Scaler Store\Scaler_Neovarsity_Applied_Project_Report.docx` (565 KB, Times New Roman, 1.5 line spacing, Woolf & Scaler cover page, Certification by Naman Bhalla, Declaration, Abstract, 8 chapters, 10 high-resolution architectural figures, 9 tables, references).
- **Markdown Dissertation:** `d:\Scaler Store\SCALER_PROJECT_REPORT.md`.
