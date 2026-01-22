# --- STAGE 1: Build Angular ---
FROM node:20-alpine AS angular-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ .
# Note: Ensure "production" build is defined in your angular.json
RUN npm run build --configuration=production

# --- STAGE 2: Build Spring Boot (Gradle Multi-module) ---
FROM eclipse-temurin:21-jdk-alpine AS java-build
WORKDIR /app

# 1. Copy Gradle wrapper and root configuration
COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle ./

# 2. Copy the backend specific configuration
# This creates the backend folder inside the container
COPY backend/build.gradle ./backend/

# 3. Prepare dependencies (This caches them so builds are faster later)
RUN chmod +x gradlew
RUN ./gradlew :backend:dependencies --no-daemon

# 4. Copy the actual backend source code
COPY backend/src ./backend/src

# 5. Move Angular build into the Spring static resources
# We use /dist/frontend/browser/ because 'frontend' is your project name
COPY --from=angular-build /app/frontend/dist/frontend/browser/ ./backend/src/main/resources/static/

# 6. Build the JAR using the root wrapper
RUN ./gradlew :backend:bootJar --no-daemon

# --- STAGE 3: Final Production Image ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Gradle puts the jar inside the subproject's build folder
COPY --from=java-build /app/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]