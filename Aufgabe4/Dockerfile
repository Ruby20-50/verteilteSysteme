# Multi-stage build for absolute minimum production image
# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy source file
COPY proxy.java .

# Compile the Java application
RUN javac proxy.java

# Stage 2: Runtime stage - minimal Alpine-based JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the compiled class file from builder stage
COPY --from=builder /app/proxy.class .

# Expose port 80
EXPOSE 80

# Run the application
CMD ["java", "proxy"]
