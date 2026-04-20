# GED Tutor

A Spring Boot + Thymeleaf web app for GED video tutoring. Students can register, log in, watch video lessons (YouTube / Vimeo), and submit homework. Admins can manage videos, users, and homework.

## Stack

- **Spring Boot 3.3** (Java 17)
- **Spring MVC** + **Thymeleaf** views
- **Spring Security** (BCrypt, form login, role-based authorization)
- **Spring Data JPA** / Hibernate
- **PostgreSQL** (prod) or **H2** (dev profile)
- Plain CSS (no front-end framework)

## Features

**Public / student**
- Landing page
- Register + log in
- Browse videos by subject (Math, Science, Social Studies, Language Arts)
- Watch embedded YouTube / Vimeo lessons
- View homework, submit / resubmit answers, see grades & feedback

**Admin (`ROLE_ADMIN`)**
- Dashboard with counts
- Videos: create, edit, delete, toggle visibility (`PUBLIC` / `PRIVATE` / `DRAFT`)
- Users: change role, enable/disable, reset password, delete
- Homework: create/edit/delete/publish, link to a video, view all submissions, grade each

## How videos work

Admins paste a YouTube or Vimeo link. The app parses the embed id and provider, then renders a responsive `<iframe>` on the video-detail page — no actual file upload.

Supported URL forms:
- `https://www.youtube.com/watch?v=ID`
- `https://youtu.be/ID`
- `https://www.youtube.com/embed/ID`
- `https://vimeo.com/ID` or `https://vimeo.com/video/ID`

## Running the app

### Option A — H2 in-memory (fastest)

```bash
cd ged-tutor
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Visit http://localhost:8080 — DB resets every restart. H2 console at http://localhost:8080/h2 (JDBC URL `jdbc:h2:mem:gedtutor`).

### Option B — PostgreSQL

1. Create a database:

   ```sql
   CREATE DATABASE gedtutor;
   CREATE USER postgres WITH PASSWORD 'postgres';  -- or edit application.properties
   GRANT ALL PRIVILEGES ON DATABASE gedtutor TO postgres;
   ```

2. (Optional) Edit `src/main/resources/application.properties` to match your credentials.

3. Run:

   ```bash
   ./mvnw spring-boot:run
   ```

Hibernate will auto-create the tables (`spring.jpa.hibernate.ddl-auto=update`).

## Default accounts (seeded on first run)

| Role    | Username | Password     |
|---------|----------|--------------|
| Admin   | `admin`  | `admin123`   |
| Student | `student`| `student123` |

Change these immediately in production.

## Project layout

```
src/main/java/com/gedtutor/
├── GedTutorApplication.java      # Spring Boot entry point
├── config/
│   ├── SecurityConfig.java       # Form login + role rules
│   └── DataSeeder.java           # Default admin/student + sample content
├── controller/
│   ├── HomeController.java       # "/"
│   ├── AuthController.java       # /login, /register
│   ├── VideoController.java      # /videos, /videos/{id}
│   ├── HomeworkController.java   # /homework, /homework/{id}, submit
│   └── AdminController.java      # /admin/** (videos, users, homework)
├── dto/                          # Form-binding objects
├── model/                        # JPA entities + enums
├── repository/                   # Spring Data JPA interfaces
├── security/CustomUserDetailsService.java
└── service/
    ├── UserService.java
    ├── VideoService.java
    ├── HomeworkService.java
    └── VideoUrlParser.java       # Parses YouTube/Vimeo URLs

src/main/resources/
├── application.properties        # PostgreSQL defaults
├── application-dev.properties    # H2 dev profile
├── static/css/styles.css
└── templates/
    ├── fragments/layout.html     # Shared nav + footer
    ├── index.html, login.html, register.html
    ├── videos.html, video-detail.html
    ├── homework.html, homework-detail.html
    └── admin/
        ├── dashboard.html
        ├── videos.html, video-form.html
        ├── users.html
        ├── homework.html, homework-form.html
        └── submissions.html
```

## URL map

| Path                                   | Who      | Purpose                        |
|----------------------------------------|----------|--------------------------------|
| `/`                                    | public   | Landing page                   |
| `/register`                            | public   | Create student account         |
| `/login`, `/logout`                    | public   | Auth                           |
| `/videos`                              | public   | Video list (public only)       |
| `/videos/{id}`                         | public   | Watch a public video           |
| `/homework`                            | logged in| My homework + submission state |
| `/homework/{id}`                       | logged in| Homework detail + submit form  |
| `/admin`                               | admin    | Dashboard                      |
| `/admin/videos`                        | admin    | List, edit, delete, change visibility |
| `/admin/videos/new`                    | admin    | Create video                   |
| `/admin/users`                         | admin    | Manage users                   |
| `/admin/homework`                      | admin    | Manage homework                |
| `/admin/homework/{id}/submissions`     | admin    | Grade submissions              |

## Notes / next steps

- Passwords are BCrypt-hashed; never stored in plain text.
- `spring.jpa.open-in-view=false`, so controllers fetch what they need and services are `@Transactional`.
- For production: disable DevTools & the H2 driver dependency (both are `runtime`), pin the admin password, run behind HTTPS, and switch `ddl-auto` to `validate` + use Flyway/Liquibase migrations.
