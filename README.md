# DevAssist — Agentic AI Developer Assistant

DevAssist imports a public GitHub repository, indexes supported source files into PostgreSQL, and uses an OpenRouter tool-calling loop to search code, read files with line numbers, and inspect Git history before answering engineering questions.

## Features

- Public GitHub repository cloning and file indexing
- Repository-scoped code search and file reading
- Git history search
- OpenRouter function/tool calling with a bounded agent loop
- React chat UI with Markdown rendering, per-repository browser-local conversations, retry/stop controls
- Docker Compose deployment with PostgreSQL persistence and service health checks

## Stack

- Java 21, Spring Boot, Spring Data JPA, JGit
- React 18, Vite, React Markdown
- PostgreSQL 17
- OpenRouter Chat Completions API
- Docker Compose, Nginx

## Quick start (Docker Compose — recommended)

Prerequisites: Docker Desktop (or Docker Engine) with the Compose plugin, and an OpenRouter API key for AI chat.

1. From the project root, create your local environment file:

   **Windows PowerShell**
   ```powershell
   Copy-Item .env.example .env
   notepad .env
   ```

   **macOS/Linux**
   ```bash
   cp .env.example .env
   ${EDITOR:-nano} .env
   ```

2. Replace `POSTGRES_PASSWORD` with a strong local password. Add your OpenRouter key to `OPENROUTER_API_KEY`. Keep `.env` private; it is git-ignored.
3. Build, test, and start the application:

   ```bash
   docker compose up --build
   ```

4. Open **http://localhost:3000**. The backend API is available at **http://localhost:8080/api** and health is at **http://localhost:8080/api/health**.
5. Import a public repository URL such as `https://github.com/spring-projects/spring-petclinic`, then ask a question. Initial cloning/indexing can take time for large repositories.

The backend image runs `mvn verify` during its build, so the Java test suite must pass before the image is produced. PostgreSQL data is stored in the named `postgres_data` volume.

### Useful Compose commands

```bash
docker compose ps
docker compose logs -f backend
docker compose logs -f frontend
docker compose down
```

To remove the database volume too (this permanently deletes imported repository data):

```bash
docker compose down -v
```

### Ports

Defaults are frontend `3000`, backend `8080`, PostgreSQL `5432`. Override with `FRONTEND_PORT`, `BACKEND_PORT`, and `POSTGRES_PORT` in `.env` if they conflict with other services.

## Run locally without Docker

Prerequisites: Java 21, Node.js 22+, Maven (or a working Maven wrapper), and PostgreSQL 17.

1. Create a database named `devassist` and set the following environment variables for the backend:

   ```text
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/devassist
   SPRING_DATASOURCE_USERNAME=devassist
   SPRING_DATASOURCE_PASSWORD=<your database password>
   OPENROUTER_API_KEY=<your OpenRouter key>
   ```

2. Start the backend from `backend/`:

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows PowerShell run `cd backend` then `./mvnw.cmd spring-boot:run`.

3. In a second terminal, start the frontend:

   ```bash
   cd frontend
   npm ci
   npm run dev
   ```

4. Open **http://localhost:5173**. Vite proxies `/api` requests to `http://localhost:8080`.

## Configuration

| Variable | Purpose | Default |
|---|---|---|
| `OPENROUTER_API_KEY` | OpenRouter secret; required for AI chat | empty (chat reports configuration error) |
| `OPENROUTER_MODEL` | OpenRouter model identifier | `openai/gpt-4o-mini` |
| `OPENROUTER_BASE_URL` | OpenRouter-compatible API base URL | `https://openrouter.ai/api/v1` |
| `SPRING_DATASOURCE_URL` | JDBC URL | local PostgreSQL URL |
| `SPRING_DATASOURCE_USERNAME` | Database user | `devassist` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `devassist` locally; set explicitly for Compose |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Schema management | `update` |

Never commit `.env` or publish an API key. For a public deployment, use the host's secret/environment-variable manager, strong database credentials, HTTPS, and restrictive network rules. The Compose setup is a single-host deployment baseline, not a hardened multi-tenant production service.

## API

### Import a repository

```http
POST /api/repositories?url=https%3A%2F%2Fgithub.com%2Fowner%2Frepo&name=repo
```

Only HTTPS GitHub repository URLs are accepted. The response contains `id`, `name`, `url`, `createdAt`, and `indexedFiles`.

### Chat with the agent

```http
POST /api/agent/chat
Content-Type: application/json

{"repositoryId": 1, "message": "Explain the architecture and cite relevant files."}
```

### Health check

```http
GET /api/health
```

Returns `200` when the backend can connect to PostgreSQL; otherwise returns `503`.

## Tests and verification

From `backend/`:

```bash
./mvnw verify
```

The Spring context test uses an in-memory H2 database under the `test` profile, so tests do not require a running PostgreSQL server or a real OpenRouter key. The frontend production build is:

```bash
cd frontend
npm ci
npm run build
```

