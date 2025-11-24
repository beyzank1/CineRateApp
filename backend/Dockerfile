# --- Stage 1: Build the Application ---
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy files
COPY pom.xml .
COPY src ./src

# Build the code
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
FROM eclipse-temurin:17-jre

# We still set WORKDIR to /app so movies_15.json is saved there (and appears on your laptop)
WORKDIR /app

# ✅ FIXED: Copy the jar to the ROOT (/) instead of /app so the volume doesn't hide it
COPY --from=build /app/target/MovieRatingApp-1.0-SNAPSHOT.jar /app.jar

# ✅ FIXED: Run the jar from the root
CMD ["java", "-jar", "/app.jar"]