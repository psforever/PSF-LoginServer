FROM mozilla/sbt as builder

COPY . /PSF-LoginServer

WORKDIR /PSF-LoginServer

RUN wget https://github.com/psforever/PSCrypto/releases/download/v1.1/pscrypto-lib-1.1.zip && \
    unzip pscrypto-lib-1.1.zip && rm pscrypto-lib-1.1.zip && \
    sbt server/pack

FROM openjdk:8-slim

COPY --from=builder /PSF-LoginServer/server/target/pack/ /usr/local

EXPOSE 51000
EXPOSE 51001
EXPOSE 51002

CMD ["psf-server"]
