# Lightweight runtime image following guide's pattern
FROM eclipse-temurin:21-jre-alpine

# Set maintainer
MAINTAINER Robot Worlds Team <team@wethinkcode.co.za>

# Create working directory
WORKDIR /robot-world

# Copy the pre-built JAR (assumes JAR is built before Docker build)
COPY target/my-server.jar robot-world.jar

# Copy configuration file
COPY src/main/java/za/co/wethinkcode/robots/config/config.properties config.properties

# Expose the socket port (guide: 5050) and the Web API
EXPOSE 5050
EXPOSE 8080

# Create volume for data persistence
VOLUME /data

# Set environment variable for port
ENV PORT=6000
ENV HTTP_PORT=7000

# Run the server in background mode (no interactive terminal)
CMD ["java", "-jar", "robot-world.jar", "background"]