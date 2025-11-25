# 🎬 Movie Rating App 

A full-stack **Movie Rating Application** built with a **Java backend**, **PostgreSQL database**, and **Android mobile client**. The system fetches movie data from an external API, caches it locally, and serves it to an Android app via REST.

---

## 🏗️ Architecture Overview

This project uses a **Docker-based microservice architecture**:

### **1. Backend (Java + Javalin)**

Runs inside a Docker container on **port 5000**.

**Responsibilities:**

* Fetch movie data from the **OMDb API**
* Cache results in PostgreSQL for offline support
* Serve movie data and search results to Android clients via **REST API**

---

### **2. Database (PostgreSQL)**

Runs in a separate Docker container on **port 5432**.

**Responsibilities:**

* Store movie records
* Store user accounts
* Store movie reviews

---

### **3. Frontend (Android App)**

Located in a separate project directory.

**Responsibilities:**

* Connects to backend through Retrofit
* Displays movie data
* Allows user interaction (search, ratings, etc.)

---

## 🚀 Running the Backend + Database

### **Prerequisites**

* Docker Desktop installed & running
* Ports **5000** and **5432** free

---

### **Steps**

#### **1. Navigate to the project root**

```bash
cd path/to/MovieRatingApp
```

#### **2. Build & run the containers**

```bash
docker-compose up --build
```

#### **3. Wait for confirmation**

Look for:

```
✅ Server is ready! Waiting for connections...
```

Your backend is now live at:

👉 **[http://localhost:5000](http://localhost:5000)**

---

## 🔌 API Endpoints

| Method | Endpoint  | Description            | Example                                                                              |
| ------ | --------- | ---------------------- | ------------------------------------------------------------------------------------ |
| GET    | `/`       | Health check           | [http://localhost:5000/](http://localhost:5000/)                                     |
| GET    | `/search` | Search movies by title | [http://localhost:5000/search?q=Inception](http://localhost:5000/search?q=Inception) |

---

## 📱 Android App Connection Guide

To connect from the **Android emulator**, use:

### **Base URL**

```
http://10.0.2.2:5000/
```

### **Allow HTTP Traffic**

Add to `AndroidManifest.xml`:

```xml
<application
    android:usesCleartextTraffic="true">
```

---

## 📂 Project Structure

```
backend/
│
├── src/main/java/org/example/
│   ├── MovieDataCollector.java   # Main server & API logic
│   └── DatabaseManager.java      # DB connection & SQL logic
│
├── Dockerfile                    # Builds Java backend image (Temurin 17)
└── docker-compose.yml            # Runs backend + PostgreSQL containers
```

---
