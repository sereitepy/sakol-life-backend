# Sakol Life Backend

Sakol Life platform: a study major guidance website that helps Cambodian students find the right university major based on a RIASEC personality quiz.

---

## What it does

- Runs the RIASEC quiz and calculates major compatibility scores using cosine similarity
- Returns ranked major recommendations based on a student's quiz answers
- Manages university and major data including tuition fees, scholarships, facilities, and admission requirements
- Handles user profiles and saved majors for both logged-in users and guests
- Validates Supabase JWTs for authentication
- Stores uploaded images (university banners, facility photos) on DigitalOcean Spaces

---

## Tech stack

- Java 21
- Spring Boot 3
- PostgreSQL via Supabase
- DigitalOcean Spaces for file storage
- Deployed on DigitalOcean App Platform

---

## Requirements

- Java 21
- Maven
- A Supabase project with the database schema applied
- A DigitalOcean Spaces bucket (or any S3-compatible storage)

---

## Environment variables

Create an `application.properties` file locally or set these as environment variables on your host.

```
PORT=8080

DATABASE_URL=jdbc:postgresql://your-supabase-host:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-db-password

SUPABASE_PROJECT_REF=your-supabase-project-ref

CORS_ALLOWED_ORIGINS=http://localhost:3000,https://yourdomain.com

DO_SPACES_ENDPOINT=https://sgp1.digitaloceanspaces.com
DO_SPACES_BUCKET=sakollife-media
DO_SPACES_ACCESS_KEY=your-access-key
DO_SPACES_SECRET_KEY=your-secret-key
DO_SPACES_CDN_BASE_URL=https://sakollife-media.sgp1.cdn.digitaloceanspaces.com
```

A few notes on these:

`DATABASE_URL` should use the Supabase connection pooler URL on port 6543 in production, not the direct connection on port 5432. This avoids hitting the free tier connection limit.

`SUPABASE_PROJECT_REF` is the short ID from your Supabase project URL, for example if your URL is `https://abcdefgh.supabase.co` then the ref is `abcdefgh`. This is used to validate JWT tokens issued by Supabase.

`CORS_ALLOWED_ORIGINS` is a comma-separated list. Make sure your frontend domain is in here, otherwise browser requests will be blocked.

---

## Running locally

```bash
git clone https://github.com/your-org/sakollife-backend.git
cd sakollife-backend

# set your environment variables first, then:
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Deployment on DigitalOcean App Platform

1. Push your code to GitHub.
2. Create a new App on DigitalOcean and connect your repository.
3. Add all the environment variables listed above in the App settings.
4. Deploy.

The app listens on whatever port is set in the `PORT` environment variable. DigitalOcean sets this automatically, so you do not need to hardcode it.
