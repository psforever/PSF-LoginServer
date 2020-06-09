FROM mozilla/sbt:8u232_1.3.8 as builder

COPY . /PSF-LoginServer

WORKDIR /PSF-LoginServer

RUN wget https://github.com/psforever/PSCrypto/releases/download/v1.1/pscrypto-lib-1.1.zip && \
    unzip pscrypto-lib-1.1.zip && rm pscrypto-lib-1.1.zip && \
    sbt pack

FROM openjdk:8u252-slim

COPY --from=builder /PSF-LoginServer/target/pack/ /usr/local

EXPOSE 51000
EXPOSE 51001
EXPOSE 51002

CMD ["ps-login"]
