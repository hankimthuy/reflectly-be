# Reflectly / MimoSe Backend

> **"Leading Self"** — Spring Boot REST API for the MimoSe personal journal application.

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL |
| Auth | Google OAuth (auth code) + username/password → backend JWT |
| API Docs | Springdoc OpenAPI — `/swagger-ui.html` |
| Build | Maven |

## Implemented API

| Area | Endpoints |
|------|-----------|
| Auth | `POST /api/auth/google`, `/login`, `/signup` |
| Users | `GET/PUT /api/users/profile`, `PUT /password`, `POST /avatar` |
| Entries | CRUD `/api/entries` |

> Legacy docs under `documentation/` may describe planned features (energy, orbit) that are **not implemented**. Trust the controllers and Swagger UI.

## Project Structure

```
src/main/java/org/mentorship/reflectly/
├── controller/     REST endpoints
├── service/        Business logic
├── repository/     Spring Data JPA
├── model/          UserEntity, EntryEntity
├── security/       JWT filters, CORS, Google auth
└── config/         Swagger, auditing, static files
```

## Local Setup

### Prerequisites

- JDK 21
- Docker (for PostgreSQL)
- Google Cloud OAuth **Web application** client (Client ID + Secret)

### 1. Start database

```bash
docker-compose up -d
```

Default credentials match [`.env.example`](./.env.example): `test_user` / `test_password`, database `reflectly`.

### 2. Configure environment

Copy the example file and fill in values:

```bash
cp .env.example .env
```

Export variables before running (Spring Boot reads env vars; `.env` is not auto-loaded):

```powershell
# PowerShell example
Get-Content .env | ForEach-Object {
  if ($_ -match '^([^#=]+)=(.*)$') { Set-Item -Path "env:$($matches[1])" -Value $matches[2] }
}
```

Required variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`.

### 3. Run

```bash
mvn spring-boot:run
```

API: `http://localhost:8080` · Swagger: `http://localhost:8080/swagger-ui.html`

### Google OAuth setup

1. Create a **Web application** OAuth client in [Google Cloud Console](https://console.cloud.google.com/).
2. Add **Authorized JavaScript origins**: `http://localhost:5173` (frontend dev server).
3. Use the same Client ID in backend (`GOOGLE_CLIENT_ID`) and frontend (`VITE_GOOGLE_CLIENT_ID`).
4. Backend exchanges the auth code using `GOOGLE_CLIENT_SECRET` with redirect URI `postmessage` (react-oauth/google).

## Branches & Deployment

| Branch | Deploy target | Workflow |
|--------|---------------|----------|
| `develop` | AWS EC2 (staging) | `aws-deploy-backend.yml` |
| `main` | Azure Web App `ReflectlyBE` | `main_reflectlybe.yml` |

**Release flow:** develop → test → PR to `main` → Azure production deploy.

### GitHub Secrets (Azure production)

| Secret | Purpose |
|--------|---------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL |
| `JWT_SECRET` | Backend JWT signing |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Google login |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated frontend URLs |
| `BE_HEALTH_URL` | Optional override for post-deploy health check |
| `AZUREAPPSERVICE_*` | Azure OIDC deploy credentials |

## Known Limitations

- **No automated tests** in backend yet — CI runs `mvn verify -DskipTests`.
- **Avatar storage** uses local filesystem (`uploads/avatars/`). On Azure App Service, files are lost on redeploy unless a persistent mount or blob storage is configured. Migrate to Azure Blob Storage before scaling.
- **CORS** is configurable via `APP_CORS_ALLOWED_ORIGINS` (defaults to localhost in dev, Azure SWA URL in prod profile).

## Documentation

| Document | Notes |
|----------|-------|
| [PRD Summary](./documentation/00-Product-Context/PRD-Summary.md) | Product vision |
| [Schema ERD](./documentation/01-Database/Schema-ERD.md) | Database design |
| [API Endpoints](./documentation/02-API-Specs/Endpoints.md) | Partially outdated — see Swagger for actual API |
