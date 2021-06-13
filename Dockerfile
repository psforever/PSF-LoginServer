FROM mozilla/sbt as builder

COPY . /PSF-LoginServer

WORKDIR /PSF-LoginServer

RUN sbt server/pack

FROM openjdk:17-slim

COPY --from=builder /PSF-LoginServer/server/target/pack/ /usr/local

EXPOSE 51000/udp
EXPOSE 51001/udp
EXPOSE 51002/tcp

CMD ["psforever-server"]
