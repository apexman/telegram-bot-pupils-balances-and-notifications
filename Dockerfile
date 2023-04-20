FROM openjdk:17
RUN echo 'alias ll="ls -Ghl"' >> ~/.bashrc
COPY . /app
WORKDIR /app
RUN chmod +x ./gradlew
RUN microdnf install findutils
RUN ./gradlew clean build --no-daemon > /dev/null 2>&1 || true
EXPOSE 8080
ENV PORT=8080
ENV ENV_NAME="local"
ENV DB_URL="jdbc:postgresql://localhost:5432/db"
ENV DB_USERNAME="postgres"
ENV DB_PASSWORD="postgres"
ENV ADMINS_CHAT_ID=-1
ENV COLLECTING_RECEIPTS_CHAT_ID=-1
ENV IS_MONITORING="true"
ENV MONITORING_CHAT_ID=-1
ENV TOKEN="MOCK"
ENV GOOGLE_SHEET_ID="MOCK"
ENV USER_TIME_ZONE="UTC"
ENV INFORM_OVERDUE_START_TIME="00:00"
ENV INFORM_OVERDUE_INTERVAL_MINUTES=1
ENV BALANCE_DECREASER_START_TIME="00:00"
ENV BALANCE_DECREASER_INTERVAL_MINUTES=1

ENTRYPOINT java -Dspring.profiles.active=${ENV_NAME} -jar build/libs/botpupilsbalances-0.0.1-SNAPSHOT.jar

