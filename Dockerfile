# First stage: Build the application
FROM maven:3.8.8-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy only the pom.xml first (for better Docker caching)
COPY pom.xml .

# Download Maven dependencies (this layer gets cached unless pom.xml changes)
RUN mvn dependency:go-offline

# Now copy the entire source code
COPY src ./src

# Now build the application
RUN mvn -T 4 clean package -DskipTests

# Second stage: Create a lightweight runtime image
FROM eclipse-temurin:21-alpine
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/jscbackend.jar ./jscbackend.jar

EXPOSE 9090
ENV SERVER_PORT=9090
ENV SPRING_PROFILES_ACTIVE=dev

CMD ["java", "--enable-preview", "-jar", "jscbackend.jar"]
