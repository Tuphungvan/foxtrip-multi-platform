# Foxtrip - Tour Booking And Management System

[Tiếng Việt](README_vi.md) | [English](README_en.md)

## System Overview
Foxtrip is a comprehensive platform for booking and managing tours, featuring a REST API backend, a web administration dashboard, and a native Android mobile application designed for customers and tour guides.

## Project Architecture
The project is built on a Client-Server architecture:
- **Backend**: Spring Boot 3.x Gradle Multi-module serving REST APIs.
- **Web Frontend**: React 19 SPA (Single Page Application) for administrators to manage configurations and view metrics.
- **Mobile Client**: Native Android application for customers to book tours and guides to handle check-ins.

![Project Architecture](picture/architecture.png)

## Core Technologies
- **Backend**: Java 21, Spring Boot 3.4.5, Spring Security (JWT, OAuth2 Google), Spring Data JPA, Liquibase, MapStruct.
- **Web Dashboard**: React 19, Vite, Tailwind CSS 4, Axios, Zustand, Mapbox GL.
- **Mobile Android**: Java/Kotlin, ViewBinding, Retrofit, OkHttp, Glide, Mapbox SDK.
- **Infrastructure & Services**: PostgreSQL 17 (PostGIS), Redis 7, RabbitMQ 3, Docker, Cloudinary (Media hosting), Groq AI (Llama-3.1), VNPay Sandbox (Payments), Gmail SMTP.

## Main Features
- **Customers**: Search tours by price/category/province, view routing/itineraries on map, book tours and service add-ons, make payments via VNPay, and interact with the Groq AI chatbot assistant.
- **Tour Guides**: View assigned tour itineraries, view passenger checklists, and scan QR codes for passenger check-in.
- **Administrators**: Design tours and day-by-day itineraries, manage locations with coordinate maps, approve cancellation/refund requests, manage users, and monitor multidimensional revenue reports.

## Application Interfaces
Below are the screenshots representing the main interfaces of the system:

* **User Interface (User Mobile)**:
  ![User Interface](screenshort/user_explore.png)
* **Administrator Dashboard (Web Dashboard)**:
  ![Admin Interface](screenshort/dashboard.png)
* **Tour Guide Interface (Guide Mobile)**:
  ![Guide Interface](screenshort/guide.png)

> [!NOTE]
> All other detailed interface screenshots (e.g., Tour management, locations, orders, chatbot assistant, etc.) can be viewed in the [screenshort](screenshort) directory.

## Backend Module Structure
- `foxtrip-app`: The main runnable Spring Boot application holding endpoints and global configurations.
- `foxtrip-user`: User accounts, guide profiles, role configurations, and authentication.
- `foxtrip-tour`: Core tour details, day-by-day itineraries, and service add-ons.
- `foxtrip-location`: Geographic location listings and PostGIS spatial queries.
- `foxtrip-order`: Booking processing, VNPay gateway integration, order cancellations, and refunds.
- `foxtrip-cart`: User shopping cart state management.
- `foxtrip-review`: Customer reviews and star rating metrics post-tour.
- `foxtrip-common`: Security configs, global exceptions, and shared DTOs.

## Database Overview
The system relies on PostgreSQL coupled with the PostGIS extension to store and query location coordinates (`geometry` Point).
Key entities include:
- `users`: Customer, Guide, and Admin account credentials.
- `tours`, `tour_itineraries`, `tour_addons`: Technical details of the tours.
- `locations`: Place definitions and map coordinates.
- `orders`, `order_items`, `order_addons`: Invoices and snapshotted details of purchases.
- `refunds`: Refund request states for cancellation.
- `carts`, `cart_items`: Cart items awaiting checkout.
- `reviews`: Customer reviews.
- `revenue_reports`: Monthly aggregated revenue statistics.

## Environment Configuration (Required)
To run the project, you must set up the following credentials, configuration files, and API keys:

### 1. Android Application:
- **Mapbox SDK Downloads (`local.properties`)**: Provide your secret download token in `MAPBOX_DOWNLOADS_TOKEN` inside [android/local.properties](android/local.properties).
- **Google Client ID & Mapbox Access Token (`config.xml`)**: Configure these inside [android/app/src/main/res/values/config.xml](android/app/src/main/res/values/config.xml).
- **Google Services Configuration (`google-services.json`)**: Download from your Firebase Console and place it in the [android/app](android/app) folder.

### 2. Backend Application:
Set up the service integration API keys inside [backend/foxtrip-app/src/main/resources/application.yml](backend/foxtrip-app/src/main/resources/application.yml):
- Google OAuth2 Client ID (`application.auth.google.client-id`)
- Cloudinary Storage (`application.cloudinary` cloud-name, api-key, api-secret)
- RSA JWT public/private key pairs (`jwt.public-key` and `jwt.private-key` as RS256 Base64/PEM)
- VNPay Sandbox merchant credentials (`foxtrip.vnpay` tmn-code and hash-secret)
- QR Check-in digital signature key (`foxtrip.qr.secret-key`)
- Groq AI Chatbot API key (`foxtrip.groq.api-key`)
- Goong Maps API key and map key (`foxtrip.goong` api-key and map-key)
- SMTP Mail Account (`spring.mail` username and application password)

---

## Links & Diagrams
- **Database Schema/Script**: Detailed database design in [doc/db_design.md](doc/db_design.md) and Liquibase scripts in [changelog](backend/foxtrip-app/src/main/resources/config/liquibase/changelog).
- **Diagrams & Screenshots**:
  - System Architecture: [architecture.png](picture/architecture.png)
  - Main Thread flow: [main_thread.png](picture/main_thread.png)
  - Directory containing all app screenshots: [screenshort](screenshort)
- **API Documentation**: Interactive swagger documentations available at `http://<host>:8080/swagger-ui/index.html` (while backend is running) or via Docker container `swagger-ui` at `http://localhost:8083`.
