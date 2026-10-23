
# ── Stage 1 : Build ──────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build
# Copie le POM parent et installe-le dans le cache local Maven
COPY pom.xml .
RUN mvn install -N -q  # -N = non-recursive, installe juste le POM parent

# Copie le POM du module card et ses dépendances
COPY services/card/pom.xml services/card/pom.xml
WORKDIR /app/services/card
RUN mvn dependency:go-offline -q
# Copie le code source et build
COPY services/card/src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2 : Optimiseur (Extraction des couches) ───────────────────────────
FROM eclipse-temurin:21-jre-alpine AS optimizer
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ── Stage 3 : Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S cardservice && adduser -S cardservice -G cardservice
USER cardservice

COPY --from=optimizer --chown=cardservice:cardservice /app/dependencies/ ./
COPY --from=optimizer --chown=cardservice:cardservice /app/spring-boot-loader/ ./
COPY --from=optimizer --chown=cardservice:cardservice /app/snapshot-dependencies/ ./
COPY --from=optimizer --chown=cardservice:cardservice /app/application/ ./

# Xmx192m est le sweet spot : suffisant pour Spring Boot 3, laisse ~300 Mo à l'OS/JVM
ENV JAVA_OPTS="\
  -XX:+UseSerialGC \
  -Xms64m \
  -Xmx192m \
  -XX:MaxMetaspaceSize=96m \
  -XX:+TieredCompilation \
  -XX:TieredStopAtLevel=1 \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.jmx.enabled=false"

ENV SERVER_PORT=8082
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -q --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]













# # ============================================================
# # card-service — Multi-stage Dockerfile
# # Stage 1 : build Maven
# # Stage 2 : image runtime JRE allégée
# # ============================================================

# # ── Stage 1 : Build ──────────────────────────────────────────────────────────
# FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# WORKDIR /build

# # Copier pom.xml en premier pour profiter du cache des dépendances Maven
# COPY pom.xml .
# RUN mvn dependency:go-offline -q

# # Copier le code source et compiler
# COPY src ./src
# RUN mvn clean package -DskipTests -q

# # ── Stage 2 : Runtime ─────────────────────────────────────────────────────────
# FROM eclipse-temurin:21-jre-alpine

# WORKDIR /app

# # Utilisateur non-root pour la sécurité
# RUN addgroup -S cardservice && adduser -S cardservice -G cardservice
# USER cardservice

# # Copier le JAR depuis le stage de build
# COPY --from=builder /build/target/*.jar app.jar

# # Variables d'environnement avec valeurs par défaut
# ENV SERVER_PORT=8082
# ENV SPRING_PROFILES_ACTIVE=prod
# ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# EXPOSE 8082

# # Health check
# HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
#   CMD wget -q --spider http://localhost:8082/actuator/health || exit 1

# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
