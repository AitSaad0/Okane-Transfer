# ── Stage 1 : Build ──────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# ── Stage 2 : Runtime ────────────────────────────────────────
FROM tomcat:10.1-jre21-temurin-jammy

RUN rm -rf /usr/local/tomcat/webapps/ROOT

COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

EXPOSE 8080