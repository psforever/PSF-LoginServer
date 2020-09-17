FROM mozilla/sbt as builder

COPY . /PSF-LoginServer

WORKDIR /PSF-LoginServer

RUN sbt server/pack

FROM openjdk:8-slim

COPY --from=builder /PSF-LoginServer/server/target/pack/ /usr/local

EXPOSE 51000
EXPOSE 51001
EXPOSE 51002

CMD ["psforever-server"]
