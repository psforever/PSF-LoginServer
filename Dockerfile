FROM hseeberger/scala-sbt

EXPOSE 51000
EXPOSE 51001

# Download Login Server and pscrypto
RUN wget https://github.com/psforever/PSF-LoginServer/archive/master.zip && \
    unzip master.zip && rm master.zip && \
    cd PSF-LoginServer-master && \
    wget https://github.com/psforever/PSCrypto/releases/download/v1.1/pscrypto-lib-1.1.zip && \
    unzip pscrypto-lib-1.1.zip && rm pscrypto-lib-1.1.zip

WORKDIR /root/PSF-LoginServer-master

# Download Scala Deps
RUN sbt compile

# Run login server
CMD ["sbt", "pslogin/run"]
