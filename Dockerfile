# ── Stage 1 : Build ──────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2 : Runtime ────────────────────────────────────────
FROM tomcat:10.1-jre21-temurin-jammy

RUN rm -rf /usr/local/tomcat/webapps/ROOT

COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080