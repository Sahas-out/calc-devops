# calc-devops

A simple calculator REST API built with Spring Boot. It runs in Docker behind an nginx reverse proxy and is built, tested, published and deployed by a Jenkins CI/CD pipeline.

- **GitHub:** https://github.com/Sahas-out/calc-devops
- **Docker Hub:** https://hub.docker.com/r/sahas000/calc-devops

## Tech stack

| Component | Version / tool |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 (Web, Actuator) |
| Build | Maven |
| Tests | JUnit 5, Spring Boot Test |
| Container | Docker (multi-stage build, non-root runtime user) |
| Orchestration | Docker Compose |
| Reverse proxy | nginx 1.27 |
| CI/CD | Jenkins (declarative pipeline) |

## Architecture

```
            host :80
               │
        ┌──────▼──────┐        backend network        ┌─────────────┐
client ─►  calc-nginx  ├──────────── /api ────────────►  calc-app   │
        │  (nginx)    │                               │  :8080      │
        └─────────────┘                               └─────────────┘
```

- Only nginx publishes a port to the host (`80`).
- The Spring Boot app is reachable only from the internal `backend` network.
- nginx starts after the app's health check (`/actuator/health`) reports `healthy`.

## API

Base path: `/api/v1`. Each endpoint takes two query parameters, `a` and `b`.

| Method | Endpoint | Example |
|---|---|---|
| GET | `/api/v1/add` | `/api/v1/add?a=2&b=3` → `5.0` |
| GET | `/api/v1/subtract` | `/api/v1/subtract?a=5&b=3` → `2.0` |
| GET | `/api/v1/multiply` | `/api/v1/multiply?a=4&b=3` → `12.0` |
| GET | `/api/v1/divide` | `/api/v1/divide?a=6&b=3` → `2.0` |

Successful response:

```json
{ "operation": "add", "a": 2.0, "b": 3.0, "result": 5.0 }
```

Errors return HTTP `400`. For example, dividing by zero gives:

```json
{ "error": "Division by zero is not allowed" }
```

## Project structure

```
├── Dockerfile               # Multi-stage build: Maven build → JRE runtime
├── docker-compose.yml       # app + nginx services
├── Jenkinsfile              # CI/CD pipeline
├── nginx/default.conf       # Reverse proxy config
├── pom.xml
└── src
    ├── main/java/com/opsCo/calc_devops
    │   ├── controller/      # REST endpoints
    │   ├── service/         # Calculator logic
    │   ├── dto/             # Response record
    │   └── exception/       # Global error handling
    └── test/java/...        # Unit and integration tests
```

## Running locally

### Prerequisites

- JDK 21 and Maven
- Docker with the Compose plugin

### With Maven

```bash
mvn clean package
java -jar target/calc-devops-0.0.1-SNAPSHOT.jar
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

Run the tests:

```bash
mvn test
```

### With Docker Compose

```bash
docker compose up -d --build
curl "http://localhost/api/v1/add?a=2&b=3"
docker compose down
```

Without `IMAGE_TAG` set, Compose builds and tags the image as `sahas000/calc-devops:latest`.

## CI/CD pipeline

The `Jenkinsfile` defines these stages:

| Stage | What it does |
|---|---|
| Checkout | Fetches the repository from GitHub |
| Build | `mvn clean package -DskipTests` and archives the JAR |
| Test | `mvn test` and publishes the JUnit results |
| Docker Build | Builds `sahas000/calc-devops:<BUILD_NUMBER>` and `:latest` |
| Push | Logs in to Docker Hub and pushes both tags |
| Deploy | Pulls the tagged image and starts it with Docker Compose, then checks that the right image is running, the container is healthy, and a request through nginx succeeds |

After every build, Jenkins sends an email that says whether the build succeeded or failed.

### How the right image gets deployed

The `app` service in `docker-compose.yml` uses:

```yaml
image: ${DOCKER_IMAGE:-sahas000/calc-devops}:${IMAGE_TAG:-latest}
```

The pipeline sets `IMAGE_TAG` to the Jenkins build number. Deploy then runs `docker compose pull` and `docker compose up --no-build`, so the running container is exactly the image that was tested and pushed.

### Jenkins setup

1. **Docker access:** add the Jenkins user to the `docker` group, then restart Jenkins:
   ```bash
   sudo usermod -aG docker jenkins
   sudo systemctl restart jenkins
   ```
2. **JDK 21:** go to *Manage Jenkins → Tools → JDK installations*. Add a JDK named `jdk21` with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`.
3. **Docker Hub credentials:** go to *Manage Jenkins → Credentials*. Add a *Username with password* credential with ID `dockerhub-credentials`. Use your Docker Hub username and a personal access token as the password.
4. **Email:** go to *Manage Jenkins → System → E-mail Notification* and configure the SMTP server. Set *System Admin e-mail address* to the sending address. Recipients are set by `EMAIL_RECIPIENTS` in the `Jenkinsfile`.
5. **Job:** create a *Pipeline* job.
   - Definition: *Pipeline script from SCM*
   - SCM: Git, with repository `https://github.com/Sahas-out/calc-devops.git`
   - Script Path: `Jenkinsfile`

### Rolling back

Every build is kept on Docker Hub under its build number. To redeploy an earlier build:

```bash
IMAGE_TAG=15 COMPOSE_PROJECT_NAME=calc-devops docker compose up -d --no-build
```
