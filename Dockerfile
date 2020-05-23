FROM mozilla/sbt:8u232_1.3.8

EXPOSE 51000
EXPOSE 51001
EXPOSE 51002

COPY . /PSF-LoginServer

WORKDIR /PSF-LoginServer

RUN wget https://github.com/psforever/PSCrypto/releases/download/v1.1/pscrypto-lib-1.1.zip && \
    unzip pscrypto-lib-1.1.zip && rm pscrypto-lib-1.1.zip

RUN sbt compile

CMD ["sbt", "pslogin/run"]
