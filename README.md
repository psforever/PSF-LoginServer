# PSForever Server [![Build Status](https://travis-ci.org/psforever/PSF-LoginServer.svg?branch=master)](https://travis-ci.com/psforever/PSF-LoginServer) [![Code coverage](https://codecov.io/gh/psforever/PSF-LoginServer/coverage.svg?branch=master)](https://codecov.io/gh/psforever/PSF-LoginServer/) [![Documentation](https://img.shields.io/badge/documentation-master-lightgrey)](https://psforever.github.io/docs/master/index.html)

<img src="https://psforever.net/index_files/logo_crop.png" align="left"
     title="PSForever" width="120">
Welcome to the recreated login and world servers for PlanetSide 1. We are a community of players and developers who took it upon ourselves to preserve PlanetSide 1's unique gameplay and history *forever*.

The login and world servers (this repo runs both by default) are built to work with PlanetSide version 3.15.84.0. Anything older is not guaranteed to work.
Currently there are no binary releases of the server as the state is pre-alpha. You will need to have a development environment set up in order to get it running.
If you just want to play, you don't need to set up a development environment. Join the public test server
by following the *[PSForever Server Connection Guide](https://docs.google.com/document/d/1ZMx1NUylVZCXJNRyhkuVWT0eUKSVYu0JXsU-y3f93BY/edit)*, which has the instructions on downloading the game and using the PSForever launcher to start the game. 

<p align="center">
  <kbd>
<img src="https://i.imgur.com/EkbIv5x.png"
     title="PSForever Server Banner">
  </kbd>
</p>

## Server Requirements

* SBT (Scala build tool)
* Java Development Kit (JDK) 8.0
* PSCrypto v1.1 - binary DLL (Windows) or Shared Library (Linux) placed in the root directory of the project. See [Downloading PSCrypto](#downloading-pscrypto) to get it set up.
* PostgreSQL

## Setting up a Build Environment
PSF-LoginServer is writen in [Scala](https://www.scala-lang.org/) and built using SBT, which allows it to be built on any platform. SBT is the Scala version of Make, but is more powerful as build definitions are written in Scala. SBT is distributed as a Java JAR and the only dependency it has is a JDK. [Follow the quick instructions on Scala's home page](https://www.scala-lang.org/download/) to get a working development environment and come back when you are done.

In order to compile scala, `scalac` is used behind the scenes. This is equivalent to Java's `javac`, but for the Scala language.
Scala runs on top of the Java Virtual Machine, meaning it generates `.class` and `.jar` files and uses the `java` executable. Essentially, Scala is just a compiler that targets the JVM, which is its runtime. All of this runs in the background and is packaged automatically by your IDE or SBT, which automatically downloads the right version of the Scala compiler for you.

### Using an IDE
Scala code can be fairly complex and a good IDE helps you understand the code and what methods are available for certain types, especially as you are learning the language.
IntelliJ IDEA has some of the most mature support for Scala of any IDE today. It has advanced type introspection and excellent code completion. It's recommended for those who are new to Scala in order to get familiar with the syntax. 

[Download the community edition of IDEA](https://www.jetbrains.com/idea/download/) directly from IntelliJ's website.
[Then get the required Scala plugin for IDEA](https://www.jetbrains.com/help/idea/managing-plugins.html).

Next, you need to get a copy of the LoginServer code. It's recommended that you perform a `git clone https://github.com/psforever/PSF-LoginServer.git` using your favorite git tool. You can also work from a downloaded ZIP of the source, but you cannot track/commit your changes.

Once you have the code downloaded, you will need to import the project into the IDE. Follow these instructions from [IntelliJ to import an SBT project](https://docs.scala-lang.org/getting-started/intellij-track/building-a-scala-project-with-intellij-and-sbt.html).
Once you have successfully imported the project ([and setup the DB](#setting-up-the-database)), navigate to the `pslogin/src/main/scala/PsLogin.scala` file, right click on the `object PsLogin` and hit 'Run PsLogin' from the context menu. This will boot up the login+world server.

### Using SBT and a Text Editor
If you are not a fan of big clunky IDEs (IDEA is definitely one of them), you can opt to use your favorite text editor (VSCode, Sublime, ViM, Notepad++, Atom, etc.) and use SBT to build the project. The only dependency you will need is SBT itself. [Download SBT](http://www.scala-sbt.org/download.html) for your platform, install or extract, and open up a command line (cmd.exe, bash, CYGWIN, Git Bash) that has the Java Development Kit in its path.

At the command line run the following commands:
```
git clone https://github.com/psforever/PSF-LoginServer.git
cd PSF-LoginServer
sbt pslogin/run
```
This will clone the repository and SBT will compile and run the login server ([make sure you have set up the DB](#setting-up-the-database)). Note: SBT is quite slow at starting up (JVM/JIT warmup). It's recommended you have an open SBT console (just run `sbt` without any arguments) in order to avoid this startup time.
With a SBT console you can run tests (and you should) using `sbt test`.

### Downloading PSCrypto
**The server requires binary builds of PSCrypto in order to run.** [Download the latest release](https://github.com/psforever/PSCrypto/releases/download/v1.1/pscrypto-lib-1.1.zip) and extract the the approprate dll for your operating system to the top level of your source directory (the root directory, not /pslogin/src/main/scala). SBT, IDEA, and Java will automatically find the required libraries when running the server.
If you are not comfortable with compiled binaries, you can [build the libraries yourself](https://github.com/psforever/PSCrypto).

If you have any issues with PSCrypto being detected when trying to run the server try adding `-Djava.library.path=` (no path necessary) to your preferred IDE's build configuration, for example with IDEA: Run -> Edit Configuration -> VM Options

## Setting up the Database
The Login and World servers require PostgreSQL for persistence.

* Windows - [Official Downloads](https://www.postgresql.org/download/windows/)
* Linux - [Debian](https://www.postgresql.org/download/linux/debian/) or [Ubuntu](https://www.postgresql.org/download/linux/ubuntu/)
* macOS - Application https://www.postgresql.org/download/ (or `brew install postgresql && brew services start postgresql`)

The default database is named `psforever` and the credentials are `psforever:psforever`. To change these, make a copy of [`config/worldserver.ini.dist`](config/worldserver.ini.dist) to `config/worldserver.ini` and change the corresponding fields in the database section. This database user will need ALL access to tables, sequences, and functions.
The permissions required can be summarized by the SQL below.
Loading this in requires access to a graphical tool such as [pgAdmin](https://www.pgadmin.org/download/) (highly recommended) or a PostgreSQL terminal (`psql`) for advanced users.

To get started using pgAdmin, run the binary. This will start the pgAdmin server and pop-up a tab in your web browser with the interface. Upon first run, enter your connection details that you created during the PostgreSQL installation. When connected, right click the "Databases" menu -> Create... -> Database: psforever -> Save.
Next, right click on the newly created database (psforever) -> Query Tool... -> Copy and paste the commands below -> Hit the "Play/Run" button. The user should be created and granted the right permissions on all future objects.

```sql
CREATE USER psforever;
ALTER USER psforever WITH PASSWORD 'psforever';
ALTER DEFAULT PRIVILEGES IN SCHEMA PUBLIC GRANT ALL ON TABLES TO psforever;
ALTER DEFAULT PRIVILEGES IN SCHEMA PUBLIC GRANT ALL ON SEQUENCES TO psforever;
ALTER DEFAULT PRIVILEGES IN SCHEMA PUBLIC GRANT ALL ON FUNCTIONS TO psforever;
```

**NOTE:** applying default privileges *after* importing the schema will not apply them to existing objects. To fix this, you must drop all objects and try again or apply permissions manually using the Query Tool / `psql`.

Now you need to synchronize the schema. This is currently available in [`schema.sql`](schema.sql).
To do this right click on the psforever database -> Query Tool... -> Copy and paste / Open the `schema.sql` file into the editor -> Hit the "Play/Run" button. The schema should be loaded into the database.
Once you have the schema loaded in, the LoginServer will automatically create accounts on first login. If you'd like a nice account management interface, check out the [PSFPortal](https://github.com/psforever/PSFPortal) web interface.

### Becoming a GM
By default users are not granted GM access. To grant a created user GM access execute the following query:

```sql
UPDATE accounts SET gm=true WHERE id=your_id;
```

You can find your account id by viewing the accounts table.

## Running the Server

To run a headless, non-interactive server, run

```
sbt pslogin/run
```

PlanetSide can now connect to your server.

To run your custom server with an interactive `scala>` REPL, run

```
sbt pslogin/console
```

![image](https://cloud.githubusercontent.com/assets/16912082/18024110/7b48dba8-6bc8-11e6-81d8-4692bc9d48a8.png)

To start the server and begin listening for connections, enter the following expression into the REPL:

```
PsLogin.run
```

![image](https://cloud.githubusercontent.com/assets/16912082/18024137/1167452a-6bc9-11e6-8765-a86fb465de61.png)

This process is identical to running the headless, non-interactive server: PlanetSide clients can connect, logging output will be printed to the screen, etc. The advantage is that you now have an interactive REPL that will evaluate any Scala expression you type into it.

![image](https://cloud.githubusercontent.com/assets/16912082/18024339/62197f66-6bcd-11e6-90f7-5569d33472a7.png)

The REPL supports various useful commands. For example, to see the type of an arbitrary expression `foo`, run `:type foo`. To print all members of a type, run `:javap -p some-type`. You can run `:help` to see a full list of commands.

![image](https://cloud.githubusercontent.com/assets/16912082/18024371/e0b72f9e-6bcd-11e6-9de5-421ec3eff994.png)


## Creating a Release
If you want to test the project without an IDE or deploy it to a server for run, you can use sbt-pack to create a release (included with the repository).
First make sure you have the [SBT tool](http://www.scala-sbt.org/download.html) on your command line (or create a new task in IntelliJ IDEA). Then get a copy of the source directory (either in ZIP or cloned form). Then do the below

```
cd PSF-LoginServer
sbt packArchiveZip # creates a single zip with resources
```

This will use the sbt-pack plugin to create a JAR file and some helper scripts to run the server. The output for this will be in the `PSF-LoginServer\target` directory. Now you can copy the ZIP file to a server you want to run it on. You will need the Java 8 runtime (JRE only) on the target to run this. In the ZIP file, there is a `bin/` directory with some helper scripts. Run the correct file for your platform (.BAT for Windows and shell script for Unix).

### Generating Documentation
Using SBT, you can generate documentation for both the common and pslogin projects using `sbt unidoc`.

Current documentation is available at [https://psforever.github.io/docs/master/index.html](https://psforever.github.io/docs/master/index.html)

## Troubleshooting

#### Unable to initialize pscrypto
If you get an error like below
```
12:17:28.037 [main] ERROR PsLogin - Unable to initialize pscrypto
java.lang.UnsatisfiedLinkError: Unable to load library 'pscrypto': Native library (win32-x86-64/pscrypto.dll) not found in resource path 
```
Then you are missing the native library required to provide cryptographic functions to the login server. To fix this, you need a binary build of [PSCrypto](#downloading-pscrypto).

If you are still having trouble on Linux, try putting the library in `root directory/pscrypto-lib/libpscrypto.so`.

## Contributing
Please fork the project and provide a pull request to contribute code. Coding guidelines and contribution checklists coming soon.

## Get in touch

* Website: http://psforever.net
* Discord (chat with us): https://discord.gg/0nRe5TNbTYoUruA4
  - Join the #code channel and ask any questions you have there

Chord is the lead developer and you can contact him on Discord as Chord or by email [chord@tuta.io](mailto:chord@tuta.io). Discord is preferred.

## License
GNU GPLv3. See LICENSE.md for the full copy.
