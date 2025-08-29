# Quiz Application Backend

This is a Spring Boot application that provides a RESTful API for managing quizzes, including CRUD (Create, Read, Update, Delete) operations. It also integrates Google OAuth2 for user authentication, ensuring that only authenticated users can access the API.

## Features

* **Quiz Management:**
    * Create new quizzes.
    * Retrieve quizzes by ID or list all quizzes.
    * Update existing quizzes.
    * Delete quizzes.
* **Google OAuth2 Login:** Secure user authentication using Google as an OAuth2 provider.
* **RESTful API:** Clean and well-structured API endpoints.
* **Database Integration:** Configurable for  PostgreSQL (for production/persistent data).

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
    * Configure "Authorized JavaScript origins" (e.g., `http://localhost:8081~~~~~~~~`) and "Authorized redirect URIs" (e.g., `http://localhost:8080/login/oauth2/code/google`).
    * Note down your **Client ID** and **Client Secret**.

### 1. Clone the Repository

```bash
git clone https://github.com/hankimthuy/reflectly-be.git
cd reflectly-be
````

### 2. Database Configuration

1.  **Add PostgreSQL dependency to `pom.xml`:**
    ```xml
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    ```
2.  **Update `src/main/resources/application.properties`:**
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/quiz_db
    spring.datasource.username=quizuser
    spring.datasource.password=quizpassword
    spring.datasource.driver-class-name=org.postgresql.Driver
    spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
    spring.jpa.hibernate.ddl-auto=update # Use 'create' or 'create-drop' for fresh starts, 'update' for existing schema
    ```
3.  **Run PostgreSQL with Docker (if not already running):**
    ```bash
    docker run --name some-postgres -e POSTGRES_USER=quizuser -e POSTGRES_PASSWORD=quizpassword -e POSTGRES_DB=quiz_db -p 5432:5432 -d postgres:13
    ```

### 3. Google OAuth Configuration

Add your Google OAuth2 credentials to `src/main/resources/application.properties`:

```properties
# Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=<YOUR_GOOGLE_CLIENT_ID>
spring.security.oauth2.client.registration.google.client-secret=<YOUR_GOOGLE_CLIENT_SECRET>
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

Replace `<YOUR_GOOGLE_CLIENT_ID>` and `<YOUR_GOOGLE_CLIENT_SECRET>` with your actual credentials.

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

The application will start on `http://localhost:8081`.

## API Endpoints

The Quiz API provides the following RESTful endpoints. All endpoints require authentication (via Google OAuth2).

### Quiz Endpoints (`/api/quizzes`)

| Method | Endpoint | Description | Request Body (JSON) | Response Body (JSON) |
| :----- | :------- | :---------- | :------------------ | :------------------- |
| `POST` | `/api/quizzes` | Create a new quiz. | `{ "title": "New Quiz", "description": "..." }` | `Quiz` object (with generated ID) |
| `GET` | `/api/quizzes` | Get all quizzes. | None | List of `Quiz` objects |
| `GET` | `/api/quizzes/{id}` | Get a quiz by its ID. | None | `Quiz` object |
| `PUT` | `/api/quizzes/{id}` | Update an existing quiz. | `{ "title": "Updated Title", "description": "..."}` | Updated `Quiz` object |
| `DELETE` | `/api/quizzes/{id}` | Delete a quiz by its ID. | None | No Content (204) |