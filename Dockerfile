# ============================================================
# Stage 1 : Build du monorepo
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# 1. Copier le POM parent et l'installer dans le cache local Maven
COPY pom.xml .
RUN mvn install -N -q

# 2. Copier le POM du module 'card'
COPY services/card/pom.xml services/card/pom.xml

# 3. Se placer dans le module et télécharger les dépendances
WORKDIR /app/services/card
RUN mvn dependency:go-offline -q

# 4. Copier le code source et compiler le JAR final
COPY services/card/src ./src
RUN mvn clean package -DskipTests -q

# ============================================================
# Stage 2 : Image Runtime ultra-légère
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copier uniquement le JAR compilé depuis l'étape de build
COPY --from=builder /app/services/card/target/*.jar app.jar

# Optimisations mémoire strictes pour Render (512 Mo / 0.1 CPU)
ENV JAVA_OPTS="-Xms64m -Xmx192m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

EXPOSE 8082

# Healthcheck pour Render
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -q --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]