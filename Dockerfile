# JDK 21 builder. The project's Scala/sbt versions come from build.sbt/build.properties;
# this tag only needs to provide JDK 21 + a compatible sbt. Confirm the exact tag string
# against https://hub.docker.com/r/sbtscala/scala-sbt/tags if the build cannot pull it.
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21.0.5_11_1.10.7_2.13.16 as builder

COPY . /PSF-LoginServer

WORKDIR /PSF-LoginServer

RUN sbt server/pack

FROM eclipse-temurin:21-jre

COPY --from=builder /PSF-LoginServer/server/target/pack/ /usr/local

EXPOSE 51000/udp
EXPOSE 51001/udp
EXPOSE 51002/tcp

CMD ["psforever-server"]
