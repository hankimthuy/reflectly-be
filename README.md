# Reflectly Application Backend

This is a Spring Boot application that provides a RESTful API for managing quizzes, including CRUD (Create, Read, Update, Delete) operations. It also integrates Google OAuth2 for user authentication, ensuring that only authenticated users can access the API.

## Features

* **Reflectly Management:**
    * Create new quizzes.
    * Retrieve quizzes by ID or list all quizzes.
    * Update existing quizzes.
    * Delete quizzes.
* **Google OAuth2 Login:** Secure user authentication using Google as an OAuth2 provider.
* **RESTful API:** Clean and well-structured API endpoints.
* **Database Integration:** Configurable for PostgreSQL (for production/persistent data).

## Technologies Used

* **Backend Framework:** Spring Boot 3.x
* **Web:** Spring Web (for RESTful APIs)
* **Data Persistence:** Spring Data JPA
* **Database:** PostgreSQL (for production)
* **Authentication:** Spring Security, Spring OAuth2 Client
* **API Documentation:** Springdoc OpenAPI (Swagger UI)
* **Build Tool:** Maven
* **Language:** Java 17+

## Getting Started

Follow these steps to set up and run the Quiz Application locally.

### Prerequisites

* **Java Development Kit (JDK) 17 or higher**
* **Maven** (or Gradle if you convert the project)
* **Google Cloud Project:**
    * Create a new project in the [Google Cloud Console](https://console.cloud.google.com/).
    * Enable the "Google People API" (or relevant APIs for user info).
    * Go to "APIs & Services" -> "Credentials".
    * Create an "OAuth client ID" of type "Web application".
    * Configure "Authorized JavaScript origins" (e.g., `http://localhost:8080~~~~~~~~~~~~`) and "Authorized redirect URIs" (e.g., `http://localhost:8080/login/oauth2/code/google`).
    * Note down your **Client ID** and **Client Secret**.

### 1. Clone the Repository

```bash
git clone https://github.com/hankimthuy/reflectly-be.git
cd reflectly-be
````

### 2. Environment Configuration

**IMPORTANT SECURITY NOTE:** This application uses environment variables for sensitive configuration to avoid hardcoded credentials.

1. **Create a `.env` file in the project root:**
   ```bash
   # Database Configuration
   DB_URL=jdbc:postgresql://localhost:5432/reflectly
   DB_USERNAME=postgres
   DB_PASSWORD=your_secure_password_here
   
   # JWT Configuration
   JWT_SECRET=your_jwt_secret_key_here_minimum_32_characters
   JWT_EXPIRATION=86400000
   
   # Spring Profile (optional)
   SPRING_PROFILES_ACTIVE=local
   ```

2. **Set up PostgreSQL database:**
   ```bash
   # Using Docker (recommended)
   docker run --name reflectly-postgres \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=your_secure_password_here \
     -e POSTGRES_DB=reflectly \
     -p 5432:5432 \
     -d postgres:13
   ```

3. **Security Best Practices:**
   - Never commit `.env` files to version control
   - Use strong, unique passwords for database access
   - Generate a secure JWT secret (minimum 32 characters)
   - Use different credentials for different environments (dev, staging, production)

### 3. Google Authentication

This application uses Google ID Token verification for authentication. The application validates Google ID tokens sent from the frontend without requiring OAuth2 client configuration.

```bash
docker-compose up -d
```

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## API Endpoints

The Quiz API provides the following RESTful endpoints. All endpoints require authentication (via Google OAuth2).