# Entries API Documentation

## Overview
This document describes the RESTful API for managing entries (journal entries) in the Reflectly application.

## Base URL
```
http://localhost:8080/api/entries
```

## Authentication
All endpoints require authentication via Google OAuth token in the Authorization header:
```
Authorization: Bearer <google_id_token>
```

## Endpoints

### 1. Get All Entries
**GET** `/api/entries`

Get all entries for the authenticated user with optional filtering.

**Query Parameters:**
- `startDate` (optional): Start date for filtering in ISO format (e.g., `2024-01-01T00:00:00`)
- `endDate` (optional): End date for filtering in ISO format (e.g., `2024-01-31T23:59:59`)
- `emotion` (optional): Filter by specific emotion (e.g., `happy`, `sad`)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "userId": "uuid",
      "title": "Today's reflection",
      "reflection": "I felt grateful for...",
      "emotions": ["happy", "blessed"],
      "activities": [],
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ],
  "message": "Entries retrieved successfully"
}
```

### 2. Get Entry by ID
**GET** `/api/entries/{id}`

Get a specific entry by its ID.

**Path Parameters:**
- `id`: Entry ID (UUID string)

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "title": "Today's reflection",
    "reflection": "I felt grateful for...",
    "emotions": ["happy", "blessed"],
    "activities": [],
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  },
  "message": "Entry retrieved successfully"
}
```

### 3. Create Entry
**POST** `/api/entries`

Create a new entry.

**Request Body:**
```json
{
  "title": "Today's reflection",
  "reflection": "I felt grateful for the little things in life",
  "emotions": ["happy", "blessed", "good"],
  "activities": ["reading", "meditation"]
}
```

**Validation Rules:**
- `title`: Required, max 100 characters
- `reflection`: Required, max 1000 characters
- `emotions`: Required, at least one emotion
- `activities`: Optional, array of strings

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "title": "Today's reflection",
    "reflection": "I felt grateful for the little things in life",
    "emotions": ["happy", "blessed", "good"],
    "activities": ["reading", "meditation"],
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  },
  "message": "Entry created successfully"
}
```

### 4. Update Entry
**PUT** `/api/entries/{id}`

Update an existing entry.

**Path Parameters:**
- `id`: Entry ID (UUID string)

**Request Body:**
```json
{
  "title": "Updated reflection",
  "reflection": "Updated content...",
  "emotions": ["happy"],
  "activities": ["reading", "meditation"]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "title": "Updated reflection",
    "reflection": "Updated content...",
    "emotions": ["happy"],
    "activities": ["reading", "meditation"],
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T12:00:00Z"
  },
  "message": "Entry updated successfully"
}
```

### 5. Delete Entry
**DELETE** `/api/entries/{id}`

Delete an entry.

**Path Parameters:**
- `id`: Entry ID (UUID string)

**Response:**
```json
{
  "success": true,
  "message": "Entry deleted successfully"
}
```

## Error Responses

### Validation Error (400)
```json
{
  "success": false,
  "error": {
    "code": "400",
    "message": "Validation failed",
    "details": {
      "title": "Title is required",
      "emotions": "At least one emotion is required"
    }
  }
}
```

### Not Found (404)
```json
{
  "success": false,
  "error": {
    "code": "404",
    "message": "Entry not found",
    "details": null
  }
}
```

### Unauthorized (401)
```json
{
  "success": false,
  "error": {
    "code": "401",
    "message": "Unauthorized - Invalid or missing authentication token",
    "details": null
  }
}
```

### Internal Server Error (500)
```json
{
  "success": false,
  "error": {
    "code": "500",
    "message": "Internal server error occurred",
    "details": "Error details..."
  }
}
```

## Database Schema

The entries are stored in the `entries` table with the following structure:

```sql
CREATE TABLE entries (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    reflection TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE entry_emotions (
    entry_id VARCHAR(36),
    emotion VARCHAR(50),
    FOREIGN KEY (entry_id) REFERENCES entries(id)
);

CREATE TABLE entry_activities (
    entry_id VARCHAR(36),
    activity VARCHAR(100),
    FOREIGN KEY (entry_id) REFERENCES entries(id)
);
```

## Testing

You can test the API using:
1. **Swagger UI**: `http://localhost:8080/swagger-ui.html`
2. **Postman** or any REST client
3. **curl** commands

### Example curl commands:

```bash
# Get all entries
curl -X GET "http://localhost:8080/api/entries" \
  -H "Authorization: Bearer <your_google_token>"

# Create entry
curl -X POST "http://localhost:8080/api/entries" \
  -H "Authorization: Bearer <your_google_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Entry",
    "reflection": "This is a test reflection",
    "emotions": ["happy", "grateful"],
    "activities": ["reading"]
  }'
```
