# API Endpoints Specification

## Overview

All API endpoints follow RESTful conventions and return standard JSON responses.

### Base URL
```
http://localhost:8080/api/v1
```

### Standard Response Format

**Success Response:**
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": { ... }
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## EnergyController

Base path: `/api/v1/energy`

### POST /log
Log a new energy reading.

**Request Body:**
```json
{
  "level": 7,
  "contextTag": "work",
  "notes": "Feeling productive after morning meeting",
  "timestamp": "2024-01-15T09:30:00Z"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "uuid-here",
    "level": 7,
    "contextTag": "work",
    "notes": "Feeling productive after morning meeting",
    "timestamp": "2024-01-15T09:30:00Z",
    "createdAt": "2024-01-15T09:31:00Z"
  },
  "message": "Energy log created successfully"
}
```

**Validation:**
- `level`: Required, integer 1-10
- `contextTag`: Required, string 1-50 chars
- `notes`: Optional, max 1000 chars
- `timestamp`: Optional (defaults to current time)

---

### GET /history
Retrieve energy log history for the authenticated user.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `startDate` | ISO DateTime | No | Filter from this date |
| `endDate` | ISO DateTime | No | Filter until this date |
| `contextTag` | String | No | Filter by context |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 20, max: 100) |

**Example Request:**
```
GET /api/v1/energy/history?startDate=2024-01-01T00:00:00Z&contextTag=work&page=0&size=10
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid-1",
        "level": 7,
        "contextTag": "work",
        "notes": "...",
        "timestamp": "2024-01-15T09:30:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 45,
    "totalPages": 5
  }
}
```

---

## OrbitController

Base path: `/api/v1/orbit`

### GET /friends
Retrieve all social connections for the authenticated user.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `orbitDistance` | Integer | No | Filter by orbit (1-5) |
| `sortBy` | String | No | Sort field: `name`, `orbit`, `impact` (default: `orbit`) |
| `order` | String | No | `asc` or `desc` (default: `asc`) |

**Example Request:**
```
GET /api/v1/orbit/friends?orbitDistance=1&sortBy=impact&order=desc
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-1",
      "friendName": "Alice",
      "orbitDistance": 1,
      "impactScore": 0.85,
      "lastInteraction": "2024-01-14T18:00:00Z"
    },
    {
      "id": "uuid-2",
      "friendName": "Bob",
      "orbitDistance": 1,
      "impactScore": 0.72,
      "lastInteraction": "2024-01-10T12:00:00Z"
    }
  ]
}
```

---

### PUT /move-friend
Update a friend's orbit distance.

**Request Body:**
```json
{
  "friendId": "uuid-of-friend",
  "newOrbitDistance": 2
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "uuid-of-friend",
    "friendName": "Alice",
    "orbitDistance": 2,
    "impactScore": 0.85,
    "lastInteraction": "2024-01-14T18:00:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  },
  "message": "Friend moved to orbit 2 successfully"
}
```

**Validation:**
- `friendId`: Required, valid UUID
- `newOrbitDistance`: Required, integer 1-5

---

## Controller Structure (Java)

```java
@RestController
@RequestMapping("/api/v1/energy")
@RequiredArgsConstructor
public class EnergyController {
    
    private final EnergyService energyService;
    
    @PostMapping("/log")
    public ResponseEntity<ApiResponse<EnergyLogDTO>> logEnergy(
        @Valid @RequestBody CreateEnergyLogRequest request,
        @AuthenticationPrincipal UserPrincipal user
    ) { ... }
    
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<EnergyLogDTO>>> getHistory(
        @RequestParam(required = false) LocalDateTime startDate,
        @RequestParam(required = false) LocalDateTime endDate,
        @RequestParam(required = false) String contextTag,
        Pageable pageable,
        @AuthenticationPrincipal UserPrincipal user
    ) { ... }
}

@RestController
@RequestMapping("/api/v1/orbit")
@RequiredArgsConstructor
public class OrbitController {
    
    private final SocialConnectionService connectionService;
    
    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<SocialConnectionDTO>>> getFriends(
        @RequestParam(required = false) Integer orbitDistance,
        @RequestParam(defaultValue = "orbit") String sortBy,
        @RequestParam(defaultValue = "asc") String order,
        @AuthenticationPrincipal UserPrincipal user
    ) { ... }
    
    @PutMapping("/move-friend")
    public ResponseEntity<ApiResponse<SocialConnectionDTO>> moveFriend(
        @Valid @RequestBody MoveFriendRequest request,
        @AuthenticationPrincipal UserPrincipal user
    ) { ... }
}
```

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `CONFLICT` | 409 | Resource conflict (e.g., duplicate) |
| `INTERNAL_ERROR` | 500 | Server error |

---

## Authentication

All endpoints require a valid JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

Tokens are obtained via Google OAuth2 login flow.
