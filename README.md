# Sakol Life — Backend API

Spring Boot REST API for the Sakol Life major recommendation platform.

## Tech Stack
- **Java 21** + **Spring Boot 3.2.5**
- **Spring Data JPA** + **PostgreSQL** (Supabase)
- **Spring Security** (Supabase JWT / HS256)
- **Maven**

---

## Project Structure
```
src/main/java/com/sakollife/
├── config/                  # SecurityConfig (CORS + Supabase JWT filter)
├── controller/              # Public + user-facing controllers
│   ├── QuizController
│   ├── MajorController
│   ├── UniversityController
│   ├── ProfileController
│   └── SavedMajorController
├── controller/admin/        # Admin-only controllers (ROLE_ADMIN)
│   ├── AdminMajorController
│   ├── AdminQuestionController
│   ├── AdminUniversityController
│   └── AdminUserController
├── dto/request/             # Request bodies
├── dto/response/            # Response bodies
├── entity/                  # JPA entities
│   └── enums/               # Language, Role, QuestionFormat, UniversityType
├── exception/               # GlobalExceptionHandler
├── repository/              # Spring Data JPA repositories
├── service/impl/            # QuizService, VectorService
└── util/                    # CosineSimilarityCalculator
```

---

## Setup

### 1. Configure credentials
Open `src/main/resources/application.properties` and fill in:

```properties
spring.datasource.url=jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require
spring.datasource.username=postgres.your-project-ref
spring.datasource.password=your-db-password
supabase.jwt.secret=your-jwt-secret
cors.allowed-origins=http://localhost:3000
```

Where to find these in Supabase:
- URL: Project Settings → Database → Connection String → JDBC tab (add jdbc: prefix)
- Username: The full postgres.xxxx string shown in the JDBC URL
- Password: Your project database password
- JWT Secret: Project Settings → API → JWT Secret

### 2. Start the app
```bash
mvn spring-boot:run
```
Hibernate will auto-create all tables on first run.

### 3. Seed the database
Run both scripts in Supabase Dashboard → SQL Editor in this order:

Step 1: src/main/resources/seed.sql — 9 majors + sample universities

Step 2: src/main/resources/seed-questions.sql — all 20 quiz questions + answer options
(The VectorService will throw an error if questions are not seeded)

### 4. Create your first admin user
After registering via Supabase Auth:

Step 1 — Update profiles table:
```sql
UPDATE profiles SET role = 'ADMIN' WHERE id = 'your-user-uuid';
```

Step 2 — Set app_metadata in Supabase Dashboard → Authentication → Users → select user → edit App Metadata:
```json
{ "app_role": "ADMIN" }
```
This encodes the role in the JWT so Spring Security reads it without a DB call on every request.

---

## Complete API Reference

All endpoints prefixed with /api/v1.

### Public (no auth required)

| Method | Endpoint | Description |
|---|---|---|
| POST | /quiz/submit | Submit quiz. Body: flat map { "Q1": "A", "Q2": "3", ... } |
| GET | /majors | List all majors |
| GET | /majors/{id} | Get single major |
| GET | /majors/results/{attemptId} | Ranked results for an attempt (>=50% only) |
| GET | /universities?majorId= | Universities for a major. Optional filters: type, city, maxFee, durationYears |

### Authenticated users (Supabase JWT required)

| Method | Endpoint | Description |
|---|---|---|
| POST | /quiz/merge-guest-attempt | Save guest quiz as attempt #1 after registration |
| GET | /quiz/history | Total attempt count |
| GET | /profile | Profile + attempt count + latest quiz answers |
| PUT | /profile | Update display name, picture, language |
| POST | /saved-majors | Save a major. Body: { "majorId": "uuid" } |
| DELETE | /saved-majors/{majorId} | Unsave a major |
| GET | /saved-majors | List saved majors |

### Admin only (ROLE_ADMIN)

#### Majors — full CRUD, extensible beyond 9
| Method | Endpoint | Description |
|---|---|---|
| GET | /admin/majors | List all majors with RIASEC vectors |
| GET | /admin/majors/{id} | Get single major |
| POST | /admin/majors | Create a new major |
| PUT | /admin/majors/{id} | Update name, description, or RIASEC vector |
| DELETE | /admin/majors/{id} | Delete a major |

#### Questions & Answer Options — fully data-driven
| Method | Endpoint | Description |
|---|---|---|
| GET | /admin/questions | All questions including inactive |
| GET | /admin/questions/{id} | Single question with options |
| POST | /admin/questions | Create question |
| PUT | /admin/questions/{id} | Update text, weight, RIASEC flags, active status |
| DELETE | /admin/questions/{id} | Soft-delete (active=false, preserves history) |
| GET | /admin/questions/{id}/options | List answer options |
| POST | /admin/questions/{id}/options | Add answer option |
| PUT | /admin/questions/{qId}/options/{optId} | Update option text, scoreValue, RIASEC |
| DELETE | /admin/questions/{qId}/options/{optId} | Delete option |

#### Universities
| Method | Endpoint | Description |
|---|---|---|
| GET | /admin/universities | List all |
| GET | /admin/universities/{id} | Get one |
| POST | /admin/universities | Create |
| PUT | /admin/universities/{id} | Update |
| DELETE | /admin/universities/{id} | Delete |
| GET | /admin/universities/{id}/majors | Majors offered by university |
| POST | /admin/universities/{id}/majors | Link major to university (with tuition + duration) |
| PUT | /admin/universities/{uId}/majors/{umId} | Update tuition or duration |
| DELETE | /admin/universities/{uId}/majors/{umId} | Remove major from university |

#### Users
| Method | Endpoint | Description |
|---|---|---|
| GET | /admin/users?page=0&size=20 | Paginated user list |
| GET | /admin/users/{userId} | Get user profile |
| GET | /admin/users/{userId}/quiz-history | All attempts + top matches |
| PUT | /admin/users/{userId}/role | Change role. Body: { "role": "ADMIN" } |

---

## How the Recommendation Engine Works

1. User answers all 20 questions (14 shown, Q4 split into 7 sub-questions)
2. VectorService loads question weights and RIASEC mappings from the database
3. For each answer: contribution = score x weight x riasecFlag[dimension]
4. The 6 accumulated values form the Student Vector [R, I, A, S, E, C]
5. CosineSimilarityCalculator computes similarity against each major's vector
6. Results ranked 1-N, filtered to >=50%, returned to frontend
7. For authenticated users: attempt + vector + all 9 results are persisted

---

## Deployment

Render (dev/test):
- Build: mvn clean package -DskipTests
- Start: java -jar target/sakol-life-backend-0.0.1-SNAPSHOT.jar

Digital Ocean (production):
- Same commands. Change ddl-auto to validate in production.
