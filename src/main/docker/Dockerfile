FROM openjdk:8-alpine
MAINTAINER Martin Panzer <postremus1996@googlemail.com>
ENTRYPOINT java -Djava.net.preferIPv4Stack=true -jar /opt/${file.runner}
HEALTHCHECK --retries=5 CMD wget -O health.json -q 127.0.0.1:8080/health || exit 1
EXPOSE 8080

ADD /maven/${file.runner} /opt/