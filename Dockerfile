FROM hseeberger/scala-sbt

EXPOSE 51000
EXPOSE 51001

# Download Login Server and pscrypto
RUN git clone https://github.com/psforever/PSF-LoginServer.git /PSF-LoginServer && \
    cd /PSF-LoginServer && \
    wget https://github.com/psforever/PSCrypto/releases/download/v1.1/pscrypto-lib-1.1.zip && \
    unzip pscrypto-lib-1.1.zip && rm pscrypto-lib-1.1.zip

WORKDIR /PSF-LoginServer

# Download Scala Deps
RUN sbt compile

# Run login server
CMD ["sbt", "pslogin/run"]
