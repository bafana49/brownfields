
# Robot World Project — Team JHB29 | TEAM GRG SCC 6

## A Java-Based Multi-Client Robot Simulation

---

## Table of Contents:

* [Project Description](#-project-description)
* [Client/Server Architecture](#-clientserver-architecture)
* [Web API](#-web-api)
* [Server GUI Features](#-server-gui-features)
* [Game Features](#-game-features)
* [Configuration](#-configuration)
* [Docker Support](#-docker-support)
* [CI/CD Pipeline](#-cicd-pipeline)
* [Completed Features](#-completed-features)
* [Outstanding Tasks](#-outstanding-tasks)
* [Future Improvements](#-future-improvements)
* [Team Members](#-team-members)
* [How to Run](#-how-to-run)
* [Build Automation](#-build-automation)
* [Testing](#-testing)
* [Notes](#-notes)

---

## Project Description

**Robot World** is a Java-based multiplayer robot simulation game where users control robots in a shared 2D world. The usual client is a CLI over TCP. The same Domain `World` can also be inspected and launched into over HTTP through a Javalin Web API. The socket server handles game logic and world state, and includes a Swing **graphical interface** for live visual feedback on robot activity and combat.

---

## Client/Server Architecture

Domain (`World`) sits in the middle. Two adapters talk to it; they do not talk to each other:

```text
CLI client  ──TCP/JSON──►  Socket server  ──►  World  ◄──  Web API  ◄──HTTP──  curl / browser
```

### Socket server:

* Multi-threaded TCP server managing concurrent robot clients
* Generates a 2D map with random obstacles
* Handles game logic: movement, turns, shooting, damage, and repairs
* Hosts a real-time **Swing GUI** to visually represent the robot world
* Started by `MultiServers` on `Config.PORT` (see `config.properties` / `-p`)

### Web API:

* Separate HTTP layer (`WebApiServer` + `WebApiHandler`) using Javalin
* Reads and changes the same Domain `World` (and named worlds via `WorldRepository`)
* Binds **`HTTP_PORT`** (default **8080**), not the socket port
* Started by `MultiServers` together with the socket server
* Shares a `WorldGate` with the socket server so only one command touches the world at a time

### Client:

* Text-based CLI that sends JSON-formatted commands to the socket server
* Handles full input-output lifecycle from the user to the server
* Displays parsed responses (state, status, damage, errors, etc.)

### Communication Protocol:

* The CLI uses **JSON messages** over TCP sockets
* Structure example:

```json
{
  "robot": "hal",
  "command": "forward",
  "arguments": { "steps": 5 }
}
```

---

## Web API

HTTP endpoints live in `za.co.wethinkcode.robots.web`.

| Method | Path | Behaviour |
| --- | --- | --- |
| `GET` | `/world` | Current in-memory world: size and obstacles (not robots) |
| `GET` | `/world/{world}` | Load that named saved world, restore it into the live `World`, return the same JSON |
| `POST` | `/robot/{name}` | Launch that robot into the current world (`LaunchCommand`). JSON body: `{"make":"sniper"}` |

Successful world GETs return **200** with `width`, `height`, and `obstacles`. Unknown world names return **404** and leave the live world unchanged. Launch returns **200** with `result: "OK"`, or **400** with `result: "ERROR"` (duplicate name or no space).

`MultiServers` starts the Web API on **`HTTP_PORT`** (8080 in `config.properties`). Use a different value from `PORT`. Override with the `HTTP_PORT` environment variable.

After `mvn exec:java -Dexec.mainClass="za.co.wethinkcode.robots.server.MultiServers"`:

```bash
curl http://localhost:8080/world

curl http://localhost:8080/world/mars

curl -X POST http://localhost:8080/robot/HAL \
  -H "Content-Type: application/json" \
  -d "{\"make\":\"sniper\"}"
```

On PowerShell, use `^` instead of `\` for line continuation.

---

## Server GUI Features

* **Real-time grid** showing robots and obstacles
* **Dynamic updates** with every client action
* Built using **Java Swing**

> Note: Only the server GUI is graphical. Each client runs in its own terminal using a CLI interface.

---

## Game Features

### Robot Types & Combat System:
- **Multiple Robot Types**: Launch different robot types (e.g., sniper, tank) with varying characteristics
- **Combat Mechanics**: 
  - `fire` - Shoot in the direction the robot is facing
  - `repair` - Restore shield health (takes time to complete)
  - `reload` - Replenish ammunition (takes time to complete)
- **Shield System**: Robots have shield health that decreases when hit
- **Damage & Blocking**: Mountains block line of sight and projectiles

### World & Obstacles:
- **Multiple Obstacle Types**:
  - **MOUNTAIN** - Blocks movement, vision, and projectiles
  - **LAKE** - Blocks movement but allows vision
  - **BOTTOMLESS_PIT** - Blocks movement with deadly consequences
- **Dynamic Visibility**: Robots can see obstacles, other robots, and world edges within configurable range
- **Line of Sight**: Mountains block vision of objects behind them

### Look Command:
- **Scanning**: The `look` command reveals objects in all cardinal directions
- **Distance Detection**: Shows exact distance to detected objects
- **Object Types**: Identifies robots, obstacles (with type), and world edges
- **Smart Filtering**: Mountains block vision of objects behind them

### Help System:
- **Built-in Help**: Type `help` to see all available commands
- **Command Usage**: Shows proper syntax for each command
- **Context-aware**: Available from any robot client

---

## Configuration

The server can be configured through a `config.properties` file or command-line arguments:

### Configuration File (`config.properties`):
- **HEIGHT/WIDTH** - World dimensions (default: 200x200)
- **HOST** - Server host address (default: localhost)
- **PORT** - Socket server port (default in file: 5050; CLI default: 5000)
- **HTTP_PORT** - Web API HTTP port (default: 8080; must differ from PORT)
- **VISIBILITY** - Robot visibility range (default: 10)
- **REPAIR_DURATION** - Time to repair shields in seconds
- **RELOAD_DURATION** - Time to reload weapon in seconds
- **MAX_SHIELD** - Maximum shield health
- **MAX_SHOTS** - Maximum ammunition
- **OBSTACLE_MODE** - Obstacle generation mode

### Command-Line Arguments:
If a flag is omitted, the default below is used.

| Name | Argument | Value | Default | Example |
|---|---|---|---|---|
| Server port | `-p` | integer 0..9999 | `5000` | `-p 5000` |
| Size of world (one side) | `-s` | integer 1..9999 | `1` | `-s 100` means a 100x100 world |
| Obstacles | `-o` | `x,y` or `none` or typed cells | `none` | `-o 10,5` places a mountain at `[10,5]` |

Typed cells (repeat as many as you like; a second obstacle on the same cell is skipped).
On an `-s 25` world, valid x and y are **0 to 24** — `M-25,3` is outside the map:

- `L-x,y` — lake
- `M-x,y` — mountain
- `B-x,y` — bottomless pit

```bash
# Port 5000, 1x1 world, no obstacles
java -jar target/my-server.jar

# 100x100 world with one mountain at [10,5]
java -jar target/my-server.jar -p 5000 -s 100 -o 10,5

# Several typed obstacles (quote values in PowerShell)
java -jar target/my-server.jar -p 5000 -s 100 -o "L-2,2" -o "M-2,3" -o "B-3,3"
```

### Environment Variables:
- **PORT** - Override socket server port (takes precedence over config file)
- **HTTP_PORT** - Override Web API port

---

## Docker Support

The project includes full Docker support for containerized deployment:

### Docker Features:
- **Lightweight Image**: Based on Eclipse Temurin JRE 21 Alpine
- **Optimized Build**: Multi-stage build process for minimal image size
- **Configuration**: Built-in config.properties included
- **Data Persistence**: Volume mount for data persistence
- **Port Exposure**: Exposes port 5050 by default
- **CI Publishing**: GitLab CI publishes an image for every commit to `main` that passes tests

### Docker Setup & Permissions:
**Important**: Docker commands require proper permissions. If you encounter "permission denied" errors:

```bash
# Add your user to the docker group (requires sudo)
sudo usermod -aG docker jadxs

# Then either log out and log back in, OR use:
newgrp docker

# For single commands without permanent group change:
sg docker -c "your docker command"
```

### Docker Commands:
```bash
# Build Docker image
make docker-build

# Run Docker container
make docker-run

# Stop Docker container
make docker-stop

# Clean up Docker resources
make docker-clean

# Run acceptance tests against Docker
make docker-acceptance-test
```

### Manual Docker Usage:
```bash
# Build image
docker build -t robot-worlds-server:latest .

# Run container
docker run -d -p 5050:5050 -p 8080:8080 --name robot-world robot-worlds-server:latest

# View logs
docker logs robot-world

# Stop container
docker stop robot-world

# Check running containers
docker ps -a

# Clean up unused resources
docker system prune -f
```

### Troubleshooting Docker Issues:
- **Port conflicts**: If port 5050 is already in use, stop existing containers with `docker stop robot-world`
- **Permission errors**: Use `sg docker -c "command"` or add user to docker group as shown above
- **Build failures**: Ensure the JAR file exists at `target/my-server.jar` by running `make package` first
- **Container not starting**: Check logs with `docker logs robot-world` for error messages

### Docker Verification:
To verify your Docker setup is working correctly:

```bash
# Check if Docker is accessible
sg docker -c "docker ps"

# Verify the container is running
sg docker -c "docker ps | grep robot-world"

# Check container logs for successful startup
sg docker -c "docker logs robot-world"

# Expected log output: "Server running on port 5050 & waiting for client connections..."
```

### Published CI Images:
This is a trunk-based workflow: the pipeline runs on every commit to `main`. After tests pass, GitLab Container Registry publishes:

- `$CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA` for that commit
- `$CI_REGISTRY_IMAGE:latest` for the newest passing build on `main`

```bash
# Pull the latest image published from main
docker pull registry.gitlab.wethinkco.de/leletscc-g-025/brownfields_robot_worlds_3:latest

# Run the published image
docker run -d -p 5050:5050 -p 8080:8080 --name robot-world registry.gitlab.wethinkco.de/leletscc-g-025/brownfields_robot_worlds_3:latest
```

---

## CI/CD Pipeline

The project uses GitLab CI/CD for automated testing and deployment. This is trunk-based development on a single `main` branch: the pipeline runs on every commit to `main`. The **Release** stage publishes a Docker image only after compile, unit tests, acceptance tests, and packaging have all succeeded.

### Pipeline Stages:
1. **Compile** - Verifies code compiles successfully
2. **Unit Test** - Runs unit tests (excludes acceptance tests)
3. **Acceptance Test** - Runs acceptance tests against local server
4. **Package** - Creates deployment JAR file
5. **Release** - Builds and publishes a Docker image to GitLab Container Registry

### Pipeline Features:
- **Trunk-based**: Runs on every commit to `main`
- **Caching**: Maven dependencies cached between stages
- **Artifacts**: Build artifacts preserved between stages
- **Test Reports**: JUnit test reports generated and stored
- **Docker Registry**: Automated image publishing for every `main` commit that passes tests
- **Image Tags**: Commit SHA and `latest` on every successful pipeline
- **Failed Tests Block Publish**: The release job depends on package, which depends on acceptance tests

### CI/CD Configuration:
- Uses Maven 3.9 with Eclipse Temurin 21
- Includes Make for build automation
- Docker-in-Docker for container builds
- Docker client talks to the `docker:dind` service on `tcp://docker:2375`
- Automatic image tagging per commit (`SHA` and `latest`)

---

## Completed Features

### Core Functionality:
- Fully functional server-side game engine
- CLI-based client for each user
- JSON message protocol
- Multi-client support with concurrency
- Server GUI for visualizing robot world
- Edge handling (map bounds, obstacles, collisions)
- World persistence during sessions
- HTTP Web API for current world, named restore, and launch (`MultiServers` starts it on `HTTP_PORT`)

### Combat & Movement:
- Combat system with `fire`, `repair`, `reload`
- Shield health and damage system
- ammunition management
- Time-based repair and reload mechanics

### Advanced Features:
- **Look command** with obstacle detection and distance calculation
- **Multiple obstacle types** (Mountain, Lake, Bottomless Pit)
- **Line of sight system** - mountains block vision
- **Help command** for user assistance
- **Configurable visibility range**
- **Command-line argument parsing**

### DevOps & Testing:
- **Docker containerization** with optimized images
- **GitLab CI/CD pipeline** with automated testing
- **Make-based build automation**
- **Unit and acceptance test suites**
- **Test reports generation**

---

## Outstanding Tasks

* GUI-based client was not implemented due to time constraints
* Limited fault tolerance testing under high concurrency
* WebSocket support for modern web clients
* Additional robot types with unique abilities

---

## Future Improvements

* Improve exception handling and test coverage
* Implement GUI-based clients (JavaFX or Swing)
* WebSocket support for modern frontends

---

## Team Members

**Team JHB29 | TEAM GRG SCC 6**

---

## How to Run

### Requirements:

* [Java 21+](https://www.oracle.com/java/technologies/javase-downloads.html)
* [Apache Maven 3.8+](https://maven.apache.org/download.cgi)

---

### Clone the Repository:

```bash
git clone git@gitlab.wethinkco.de:haazizjhb024/oop-ex-toy-robot-group-29-2025.git
cd oop-ex-toy-robot-group-29-2025
```

---

### Build the Project:

```bash
mvn clean install
```

---

### Run the Server:

```bash
mvn exec:java -Dexec.mainClass="za.co.wethinkcode.robots.server.MultiServers"
```

This starts the **socket** server and the **Web API** (HTTP on `HTTP_PORT`, default 8080).

---

### Run a Client (in a new terminal):

```bash
mvn exec:java -Dexec.mainClass="za.co.wethinkcode.robots.client.Client"
```

---

### Example Client Commands:

```
launch sniper hal
launch tank terminator
help
state
forward 10
back 10
turn right
turn left
look
fire
repair
reload
```

---

### Example Server Console Commands:

```
help
robots
dump
save
save <world-name>
restore
restore <world-name>
quit
```

The console talks to the socket process. HTTP is already listening; see [Web API](#-web-api).

---

## Build Automation

The project includes a comprehensive Makefile for build automation:

### Development Commands:
```bash
make compile          # Compile the project
make dev              # Compile and run unit tests (development build)
make unit-test        # Run unit tests only
make test             # Run all tests
make verify           # Run full verification including tests
make package          # Package JAR without tests
```

### Testing Commands:
```bash
make test-local       # Run tests against local server
make acceptance-test  # Run acceptance tests only
```

### Release Commands:
```bash
make set-release-version  # Prepare for release
make tag                  # Tag git release
make release              # Full release process
```

### Docker Commands:
```bash
make docker-build         # Build Docker image
make docker-run           # Run Docker container
make docker-stop          # Stop Docker container
make docker-clean         # Clean Docker resources
make docker-acceptance-test  # Run tests against Docker
```

---

## Testing

The project includes comprehensive test coverage with both unit and acceptance tests:

### Test Structure:
- **Unit Tests**: Individual component testing
  - Position tests
  - Command tests (Fire, Look, Move, Reload, Repair, State, Turn)
  - Maze and Obstacle tests
  - World tests
  - Server tests
  - Web API tests (`WebApiHandler`, `WebApiServer`, HTTP integration)
  - Client tests

- **Acceptance Tests**: End-to-end scenario testing
  - Launch robot tests
  - Movement tests
  - Look command tests
  - State command tests

### Running Tests:
```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="!za.co.wethinkcode.robots.acceptance.**"

# Run only acceptance tests
mvn test -Dtest="za.co.wethinkcode.robots.acceptance.**"

# Run specific test class
mvn test -Dtest="LookCommandTest"

# Run with Make
make test              # All tests
make unit-test         # Unit tests only
make acceptance-test   # Acceptance tests only
```

### Test Framework:
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework for unit tests
- **Maven Surefire** - Test runner plugin

---

## Notes

* The server must be started **before any clients connect**
* Server GUI opens automatically — no extra input required
* Each client should be run in its own terminal session
* Use `help` command in client to see all available commands
* Mountains block both vision and projectiles in the look command
* Docker is the recommended deployment method for production
* The CI/CD pipeline runs on every commit to `main` and publishes a Docker image after tests pass
* **Docker permissions**: Use `sg docker -c "command"` if you encounter permission errors, or add your user to the docker group
* **Container status**: The Docker container is configured to run on port 5050 and includes the latest built JAR file
* **Build verification**: Run `make package` before Docker build to ensure the JAR file is up to date

---
