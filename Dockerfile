FROM maven:3.9.9-eclipse-temurin-17

ENV HEADLESS=true \
    MAVEN_OPTS="-Dmaven.repo.local=/opt/m2" \
    HOME=/tmp

WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN chmod -R a+rwX /app /opt/m2

CMD ["mvn", "-B", "test"]
