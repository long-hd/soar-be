# Stage 1: Build với Maven Wrapper
FROM docker.io/library/eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build

# Copy Maven Wrapper + Lombok config trước (cache layer)
# lombok.config CRITICAL — chứa lombok.accessors.chain=true
# Thiếu file này → setter return void → compile fail với method chaining
COPY .mvn/ .mvn/
COPY mvnw pom.xml lombok.config ./

# Copy module poms cho dependency resolution
COPY soar-dependencies/pom.xml soar-dependencies/
COPY soar-framework/pom.xml soar-framework/
COPY soar-framework/soar-common/pom.xml soar-framework/soar-common/
COPY soar-framework/soar-spring-boot-starter-biz-data-permission/pom.xml soar-framework/soar-spring-boot-starter-biz-data-permission/
COPY soar-framework/soar-spring-boot-starter-biz-tenant/pom.xml soar-framework/soar-spring-boot-starter-biz-tenant/
COPY soar-framework/soar-spring-boot-starter-excel/pom.xml soar-framework/soar-spring-boot-starter-excel/
COPY soar-framework/soar-spring-boot-starter-jpa/pom.xml soar-framework/soar-spring-boot-starter-jpa/
COPY soar-framework/soar-spring-boot-starter-redis/pom.xml soar-framework/soar-spring-boot-starter-redis/
COPY soar-framework/soar-spring-boot-starter-security/pom.xml soar-framework/soar-spring-boot-starter-security/
COPY soar-framework/soar-spring-boot-starter-web/pom.xml soar-framework/soar-spring-boot-starter-web/
COPY soar-module-infra/pom.xml soar-module-infra/
COPY soar-module-system/pom.xml soar-module-system/
COPY soar-server/pom.xml soar-server/

RUN chmod +x mvnw

# Copy source code
COPY soar-framework soar-framework/
COPY soar-module-infra soar-module-infra/
COPY soar-module-system soar-module-system/
COPY soar-server soar-server/

# Build (skip tests for speed; tests verify trong dev workflow)
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM docker.io/library/eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.source=https://github.com/long-hd/soar-be
LABEL org.opencontainers.image.description="Soar admin platform backend"
LABEL org.opencontainers.image.licenses=MIT

# Non-root user
RUN addgroup -S soar && adduser -S soar -G soar

WORKDIR /app

# Copy JAR
COPY --from=build /build/soar-server/target/soar-server.jar app.jar

# Create logs dir + chown
RUN mkdir -p /app/logs && chown -R soar:soar /app

USER soar
EXPOSE 48080

# JVM tuning cho 6GB VM
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=50.0 -XX:InitialRAMPercentage=25.0 -Duser.timezone=UTC"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]