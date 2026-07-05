# Azure Production Secrets Setup

The backend Azure deploy **requires** these GitHub repository secrets before the app will start healthy.

Go to: **GitHub repo → Settings → Secrets and variables → Actions → New repository secret**

## reflectly-be (required)

| Secret | Example / notes |
|--------|-----------------|
| `DB_URL` | `jdbc:postgresql://<host>:5432/reflectly?sslmode=require` |
| `DB_USERNAME` | PostgreSQL user |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Random string, **minimum 32 characters** |
| `GOOGLE_CLIENT_ID` | Same Web client ID as frontend |
| `GOOGLE_CLIENT_SECRET` | From Google Cloud Console |
| `APP_CORS_ALLOWED_ORIGINS` | Azure SWA URL, e.g. `https://gray-island-018b47d00.1.azurestaticapps.net` |
| `BE_HEALTH_URL` | Optional: `https://reflectlybe-dgcnc3fxdkb0eccc.eastasia-01.azurewebsites.net` |

## reflectly-fe (required)

| Secret | Example / notes |
|--------|-----------------|
| `VITE_API_URL` | `https://reflectlybe-dgcnc3fxdkb0eccc.eastasia-01.azurewebsites.net/api` |
| `VITE_GOOGLE_CLIENT_ID` | Same as backend `GOOGLE_CLIENT_ID` |
| `FE_HEALTH_URL` | Optional: Azure Static Web Apps URL |

## After adding secrets

1. Re-run the failed workflow: **Actions → Build and deploy JAR app → Re-run all jobs**
2. Verify: `curl https://reflectlybe-dgcnc3fxdkb0eccc.eastasia-01.azurewebsites.net/actuator/health`
3. Open the frontend URL and test login

## AWS EC2 staging (develop branch)

| Secret | Purpose |
|--------|---------|
| `EC2_HOST` | EC2 public IP or hostname |
| `EC2_USER` | SSH user (e.g. `ubuntu`) |
| `EC2_SSH_KEY` | Private key for SCP/SSH deploy |

Frontend EC2 build also needs `VITE_API_URL` and `VITE_GOOGLE_CLIENT_ID` secrets.
