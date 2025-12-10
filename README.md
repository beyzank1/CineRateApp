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

## Running the System (Backend)

The entire backend (Java server and PostgreSQL database) runs inside Docker containers.

### Important Note: Preventing Database Errors (Race Condition Fix)

We have implemented a Docker Health Check to ensure the database is ready before the Java server starts. You should use the following commands to ensure a clean startup.

1.  **Stop and Delete Old Data (REQUIRED on first run or if database schema changes):**
    If you have changed the database schema (e.g., added a column), you must run this command to remove the old, potentially corrupt, database volume.
    ```bash
    docker-compose down -v
    ```

2.  **Build and Start the System:**
    This command builds the Java code, starts the PostgreSQL database, and waits for the database to be healthy before starting the backend server.
    ```bash
    docker-compose up --build
    ```

### Accessing the Server

* **From your local browser (for testing):** `http://localhost:5000`
* **From the Android Emulator (for the mobile app):** `http://10.0.2.2:5000`
---

## API Documentation

The backend exposes a REST API on port `5000`.
Base URL for Android Emulator: `http://10.0.2.2:5000`
Base URL for Local Testing: `http://localhost:5000`

### 1. Search Movies (Hybrid)
Searches for movies by title.
* **Method:** `GET`
* **Endpoint:** `/search?q={title}`
* **Description:**
    * **Online:** Fetches from OMDb, caches the result (including full plot), and returns it.
    * **Offline:** Searches the local PostgreSQL database for previously cached movies.
* **Example:** `/search?q=Inception`

### 2. Get Movie Details
Fetches full details for a specific movie.
* **Method:** `GET`
* **Endpoint:** `/movie/{imdbId}`
* **Description:** Retrieves the full movie object, including the plot description. Works offline if the movie is cached.
* **Example:** `/movie/tt1375666`

### 3. Submit a Review
Saves a user review to the local database.
* **Method:** `POST`
* **Endpoint:** `/reviews`
* **Body (JSON):**
    ```json
    {
      "movieId": "tt1375666",
      "authorId": 1,
      "value": 5,
      "review": "Amazing movie!"
    }```

### 4. Get Reviews
Retrieves all reviews for a specific movie.
* **Method:** `GET`
* **Endpoint:** `/reviews/{movieId}`
* **Description:** Returns a list of all user reviews stored locally for the given movie ID.
* **Example:** `/reviews/tt1375666`

### 5. Health Check
* **Method:** `GET`
* **Endpoint:** `/`
* **Description:** Returns "Server is Online!" to verify connectivity.
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
    android:usesCleartextTraffic="true"
    android:label="@string/app_name"
    android:icon="@mipmap/ic_launcher">
</application>


---


