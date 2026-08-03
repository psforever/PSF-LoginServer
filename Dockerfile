# JDK 21 builder. The project's Scala/sbt versions come from build.sbt/build.properties;
# this tag only needs to provide JDK 21 + an sbt launcher (which fetches sbt 1.10.7 /
# Scala 2.13.18 per the project). No published sbtscala tag bundles 1.10.7_2.13.18, so we
# use the newest available JDK-21 launcher tag. See https://hub.docker.com/r/sbtscala/scala-sbt/tags
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21.0.2_13_1.10.4_2.13.15 as builder

COPY . /PSF-LoginServer

WORKDIR /PSF-LoginServer

RUN sbt server/pack

FROM eclipse-temurin:21-jre

COPY --from=builder /PSF-LoginServer/server/target/pack/ /usr/local

EXPOSE 51000/udp
EXPOSE 51001/udp
EXPOSE 51002/tcp

CMD ["psforever-server"]
