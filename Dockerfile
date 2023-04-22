FROM sbtscala/scala-sbt:eclipse-temurin-focal-11.0.17_8_1.8.2_2.13.10 as builder

COPY . /PSF-LoginServer

WORKDIR /PSF-LoginServer

RUN sbt server/pack

FROM openjdk:18-slim

COPY --from=builder /PSF-LoginServer/server/target/pack/ /usr/local

EXPOSE 51000/udp
EXPOSE 51001/udp
EXPOSE 51002/tcp

CMD ["psforever-server"]
