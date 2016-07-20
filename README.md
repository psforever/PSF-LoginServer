# PlanetSide Login Server [![Build Status](https://travis-ci.org/psforever/PSF-LoginServer.svg?branch=master)](https://travis-ci.org/psforever/PSF-LoginServer)
This project contains the code to run and manage the login server role for PlanetSide 1.

![PSForever Login Server banner](https://i.imgur.com/EkbIv5x.png)

Currently there are no releases of the server. You will need to have a development environment set up in order to get it running.

## Build Requirements

* SBT 0.13.x
* Scala 2.11.7
* https://github.com/psforever/PSCrypto - binary DLL (Windows) or Shared Library (Linux) placed in the root directory of the project.

## Setting up a Build Environment
PSF-LoginServer is built using Simple Build Tool (SBT), which allows it to be built on any platform. SBT is the Scala version of Make, but is more powerful as build definitions are written in Scala. SBT is distributed as a Java JAR and the only dependency it has is a JDK.

### Getting the Java Development Kit
This project is tested with the official [JDK 8 from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Download and install it for your system in order to start compiling Scala. If you are new to Scala, this may seem strange to be installing Java for Scala. Scala runs on top of the Java Virtual Machine, meaning it generates `.class` and `.jar` files and uses the `java` executable. Essentially, Scala is just a compiler that targets the JVM, which is its runtime.

### Downloading the Scala Development Kit
In order to use scala, you need the compiler `scalac`. This is equivalent to Java's `javac`, but for the Scala language. [Grab the 2.11.7 version from Scala-Lang.org](http://www.scala-lang.org/download/2.11.7.html).
Install this on to your system and the compiler and Scala REPL will be added to your PATH.

### Using an IDE
Scala code can be fairly complex and a good IDE helps you understand the code and what methods are available for certain types.
IntelliJ IDEA has some of the most mature support for Scala of any IDE today. It has advanced type introspection and excellent code completion. It's recommended for those who are new to Scala in order to get familiar with the syntax.

[Download the community edition of IDEA](https://www.jetbrains.com/idea/download/)  directly from IntelliJ's website.
Then [follow this tutorial](http://nanxiao.me/en/getting-started-with-scala-in-intellij-idea-14-1/) to get the required Scala plugin for IDEA. Stop at step 2a (project creation) as we will be importing the LoginServer project ourselves.

Next, you need to get a copy of the LoginServer code. It's recommended that you perform a `git clone https://github.com/psforever/PSF-LoginServer.git` using your favorite git tool. You can also work from a ZIP ball, but you cannot develop from it.

Once you have the code downloaded, you will need to import the project into the IDE. Follow these instructions from [IntelliJ to import an SBT project](https://www.jetbrains.com/help/idea/2016.1/getting-started-with-sbt.html#import_project).
Once you have successfully imported the project, navigate to the `pslogin/src/main/scala/PsLogin.scala` file, right click and 'Run PsLogin'. This will boot up the login server.

### Using SBT and a Text Editor
If you are not a fan of big clunky IDEs (IDEA is definitely one of them), you can opt to use your favorite text editor (Sublime, ViM, Notepad++, Atom, etc.) and use SBT to build the project. The only dependency you will need is SBT itself. [Download SBT](http://www.scala-sbt.org/download.html) for your platform, install or extract, and open up a command line (cmd.exe, bash, CYGWIN, Git Bash) that has the Java Development Kit in its path.

At the command line run the following commands
```
git clone https://github.com/psforever/PSF-LoginServer.git
cd PSF-LoginServer
sbt pslogin/run
```
This will clone the repository and SBT will compile and run the login server. Note: SBT is quite slow at starting up. It's recommended you have an open SBT console in order to avoid this startup time.

## Connecting to the Server through the Client
To get PlanetSide to connect to your custom server, you will have to navigate to the `client.ini` file (located within the PlanetSide game directory) and modify the IP addresses.

Check to see what IP the server is listening on (look for the `login-udp-endpoint` line) and copy that IP, followed by port 51000 to the the second line of the `client.ini`, which should initially say `login0=64.37.158.81:45000`. Your new line should say `login0=YourIP:51000`.  Delete all of the other lines in the file except `[network]` at the top of the file. Save and enjoy!

The file should now look like this

```ini
[network]
login0=your.local.ip:your-port
```

**You must restart PlanetSide when changing `client.ini`**

## Creating a Release
If you want to test the project without an IDE or deploy it to a server for run, you can run `sbt pack` to create a release.
First make sure you have the [SBT tool](http://www.scala-sbt.org/download.html) on your command line. Then get a copy of the source directory (either in ZIP or cloned form). Then do the below

```
cd PSF-LoginServer
sbt pack
```

This will use the sbt-pack plugin to create a JAR file and some helper scripts to run the server. The output for this will be in `PSF-LoginServer\pslogin\target\pack`. Copy or ZIP up that entire pack directory and copy it to the server you want to run it on. You will need the Java 8 runtime (JRE) to run this. Navigate to the `bin/` directory in the pack folder and run the correct file for your platform (.BAT for Windows and other for Unix).

Note: you *will* need the pscrypto.dll or libpscrypto.so placed in your `bin/` directory in order to run this.

Automatic or nightly releases coming in the future.

## Troublshooting

#### Unable to initialize pscrypto
If you get an error like below
```
12:17:28.037 [main] ERROR PsLogin - Unable to initialize pscrypto
java.lang.UnsatisfiedLinkError: Unable to load library 'pscrypto': Native library (win32-x86-64/pscrypto.dll) not found in resource path 
```
Then you are missing the native library required to provide cryptographic functions to the login server. To fix this, you need a binary build of the https://github.com/psforever/PSCrypto project. Automatic builds are not set up at the moment, so get in touch with Chord for a working binary or attempt to build it yourself: YMMV.

## Contributing
Please fork the project and provide a pull request to contribute code. Coding guidelines and contribution checklists coming soon.

## Get in touch

* Website: http://psforever.net
* Discord (chat with us): https://discord.gg/0nRe5TNbTYoUruA4
  - Join the #psforever-code channel and ask any questions you have there

Chord is the lead developer and you can contact him on Discord as Chord or by email [chord@tuta.io](mailto:chord@tuta.io). Discord is preferred.

## License
GNU GPLv3. See LICENSE.md for the full copy.
