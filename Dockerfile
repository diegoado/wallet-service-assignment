FROM gradle:9.5-jdk25 AS builder

WORKDIR /app

ADD . .
ADD "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.28.0/opentelemetry-javaagent.jar" ./opentelemetry/opentelemetry-javaagent.jar

RUN touch settings.gradle.kts && \
    echo 'rootProject.name = "wallet-service-assignment"' > settings.gradle.kts && \
    ./gradlew build -x test -x check

FROM amazoncorretto:24-alpine3.20-jdk AS jre

RUN apk add --no-cache binutils && \
    $JAVA_HOME/bin/jlink \
    --verbose \
    --module-path $JAVA_HOME/jmods \
    --add-modules ALL-MODULE-PATH \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /customjre

FROM alpine:3.20

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV TZ=UTC

ARG APPLICATION_USER=appuser

COPY --from=jre /customjre $JAVA_HOME
COPY --from=builder /app/build/distributions/*.zip ./wallet-service-assignment.zip
COPY --from=builder /app/opentelemetry /opt/opentelemetry

RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER && \
    apk add --no-cache tzdata && \
    cp -r -f /usr/share/zoneinfo/$TZ /etc/localtime && \
    mkdir /app && \
    unzip wallet-service-assignment && \
    rm -rf *.zip && \
    mv wallet-service-assignment* /app/wallet-service-assignment && \
    chown -R $APPLICATION_USER /app && \
    chown -R $APPLICATION_USER /opt/opentelemetry

WORKDIR /app

USER $APPLICATION_USER

ENTRYPOINT [ "wallet-service-assignment/bin/wallet-service-assignment" ]
