# PSForever Server [![Build Status](https://travis-ci.org/psforever/PSF-LoginServer.svg?branch=master)](https://travis-ci.com/psforever/PSF-LoginServer) [![Code coverage](https://codecov.io/gh/psforever/PSF-LoginServer/coverage.svg?branch=master)](https://codecov.io/gh/psforever/PSF-LoginServer/) [![Documentation](https://img.shields.io/badge/documentation-master-lightgrey)](https://psforever.github.io/docs/master/index.html)

<img src="https://psforever.net/index_files/logo_crop.png" align="left" title="PSForever" width="120">

Welcome to the recreated login and world servers for PlanetSide 1. We are a community of players and developers who took
it upon ourselves to preserve PlanetSide 1's unique gameplay and history _forever_.

The login and world servers (this repo runs both by default) are built to work with PlanetSide version 3.15.84.0.
Anything older is not guaranteed to work. Currently, there are no binary releases of the server as the state is
~~pre-alpha~~ ~~alpha~~ ~~beta~~ scare. To contribute, you will need to have a development environment in order to get it running.
If you just want to play, you don't need the development environment.  Join the public test server by following
the _[PSForever Server Connection Guide](https://docs.google.com/document/d/1ZMx1NUylVZCXJNRyhkuVWT0eUKSVYu0JXsU-y3f93BY/edit)_,
which has the instructions on downloading the game and using the PSForever launcher to start the game.

<p align="center">
  <kbd>
<img src="https://i.imgur.com/EkbIv5x.png"
     title="PSForever Server Banner">
  </kbd>
</p>

## Server Requirements
- Running
  - [Java](https://www.java.com/en/) Development Kit 8.0
    - JDK 1.8_251
  - [Scala](https://www.scala-lang.org/)
    - 2.13.3 (set in `build.sbt`)
  - [sbt](http://www.scala-sbt.org) (Scala build tool)
    - 1.4.5+?
    - Up to date
  - [PostgreSQL](https://www.postgresql.org/)
    - 10+
- Development (+Running) 
  - [Git](https://en.wikipedia.org/wiki/Git)
  - IDE or Text Editor

### Git
Git means "Global Information Tracker" and is used for project revision control, keeping track of changes made to
files. Git is not actually necessary to acquire the project as GitHub allows for downloading a ZIP
archive of the source code that can be deployed anywhere.  Development on the project, however, will require the
use of GitHub and, as recommended, a fork of the main project repository.

Though git is traditionally a command line interface (CLI) application, GitHub offers a [Desktop](https://desktop.github.com/)
GUI that introduces graphical components and menus for normal operations, as well as visualization of the revision
hierarchy itself. Depending on the Linux distro, users have access to the git CLI through a [variety](https://git-scm.com/download/linux)
of commands.  For Windows, use an [appropriate installer](https://git-scm.com/download/win) for the CLI.  Additionally,
[posh-git](https://git-scm.com/book/en/v2/Appendix-A%3A-Git-in-Other-Environments-Git-in-PowerShell) exists to make
the CLI prettier and more informative for Windows PowerShell users.

Your IDE of choice may also be integrated with git.  Check its documentation.

Use `git clone https://github.com/[user]/PSF-LoginServer.git`, where "user" is your GitHub account or `psforever` for
the main project, to create a local copy.  If the main project, you should not use the produced local repository
for developmental purposes - fork it first and `git clone` that.  One way or another, an installation directory of
the project will have been created.

### Languages
PSF-LoginServer is written in Scala and built using sbt which allows it to be built on
any platform. sbt is the Scala version of Make, but is more powerful as build definitions are written in Scala. sbt is
distributed as a Java JAR and the only dependency it has is a JDK. In order to compile scala, `scalac` is used behind
the scenes. This is equivalent to Java's `javac`, and the language itself runs on top of the Java Virtual
Machine, meaning it generates `.class` and `.jar` files and uses the `java` executable. Essentially, Scala is just a
compiler that targets a JVM runtime.

Download and install the
[correct version](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html) of Java,
a more [up-to-date version](https://www.oracle.com/java/technologies/downloads/) of Java (if you don't have one),
then follow the [quick instructions on Scala's home page](https://www.scala-lang.org/download/)
to get a working development environment.  The modern installation of Scala utilizes Coursier, a Scala application
used for dependency resolution, and will install a number of "apps" including the sbt build tool.  These "apps" are
always installed with the most-recent non-developmental versioning, so follow the
[command guidelines](https://get-coursier.io/docs/cli-install) to install the version that is wanted or
required.  Additionally, specific releases of Scala can also be installed separately.

If you have Docker and docker-compose installed on your system, you can get a complete development environment up by
running `docker-compose up` in the source code directory. Otherwise, keep reading.

### sbt
As mentioned, when acquiring the Scala language using Coursier, a version of sbt will also be added.
If not using Coursier, download [sbt for your platform](http://www.scala-sbt.org/download.html) and install or
extract it.  Open up a command line tool - cmd.exe, bash, CYGWIN, Git Bash - that has the Java Development Kit
accessible from prompt to use `sbt` commands.

Note: sbt is quite slow at starting up due to JVM JIT warmup. An open sbt console - just run `sbt` without any
arguments - is recommended in order to avoid this startup time.

### PostgreSQL Database
A database is required for persistence of game state and player characters.  The login server and game server (which are
considered the same things, more or else) are set up to accept queries to a PostgreSQL server.  It doesn't matter if you
don't understand what PostgreSQL actually is compared to MySQL. I don't get it either - just install it: 
for [Windows](https://www.postgresql.org/download/windows/);
for Linux [Debian](https://www.postgresql.org/download/linux/debian/),
for Linux [Ubuntu](https://www.postgresql.org/download/linux/ubuntu/);
or, for macOS, [normally](https://www.postgresql.org/download/),
or using`brew install postgresql && brew net.psforever.services start postgresql`.

Additionally, loading the database information will require access to a graphical tool such as
[pgAdmin](https://www.pgadmin.org/download/) (highly recommended) or a PostgreSQL terminal (`psql`) for advanced users.
The Windows PostgreSQL installation will come with a version of pgAdmin.

To use pgAdmin, run the appropriate binary to start the pgAdmin server.  Depending on the version, a tab in your web
browser will open, or maybe a dedicated application window will open.  Either way, create necessary passwords during
the first login, then enter the connection details that were used during the PostgreSQL installation.  When connected,
expand the tree and right click on "Databases", menu -> Create... -> Database.  Enter name as "psforever", then Save.
Right click on the psforever database, menu -> Query Tool...  Copy and paste the commands below, then hit the 
"Play/Run" button. The user should be created and made owner of the database. (Prior to that, it should be "postgresql".)
(Check menu -> Properties to confirm.  May need to refresh first to see these changes.)
```sql
CREATE USER psforever;
ALTER USER psforever WITH PASSWORD 'psforever';
ALTER DATABASE psforever OWNER TO psforever;
```
**IMPORTANT NOTE**: applying privileges _after_ importing the schema will not apply them to existing objects.
If this happens, drop all objects and try again or apply permissions to everything manually using the Query Tool / `psql`.

### Using an IDE
Scala code can be fairly complex, and a good IDE helps you understand the code and what methods are available for certain
types, especially as you are learning the language. IntelliJ IDEA has some of the most mature support for Scala of any
IDE today. It has advanced type introspection (examine the properties of an object at runtime) and excellent code
completion (examine the code as you are writing it). 
Download the [community edition of IDEA](https://www.jetbrains.com/idea/download/) directly from IntelliJ's website
then get the [required Scala plugin for IDEA](https://www.jetbrains.com/help/idea/managing-plugins.html).

You will need to import the project into the IDE.  Older versions of IDEA (2016.3.4, etc.) have an 
[import procedure](https://www.lagomframework.com/documentation/1.6.x/scala/IntellijSbt.html)
where it is necessary to instruct the IDE what kind of project is being imported.  Modern IDEA (2022.1.3) still
utilizes this procedure but can also open the repo as a project and contextually determine what
is being expressed by the code (much better than older versions can, anyway).  Certain aspects will need to be
clarified manually if this method is utilized, e.g., the JDK and JRE, since those choices were skipped.  When the
project is fully imported, create a new run configuration using the dropdown near the top of the interface.
Create an sbt task configuration with the specific task instructions `server/run`.  Confirm.  Press the green arrow
"Run") to launch the server.

### Using a Text Editor
If you are not a fan of big clunky IDEs, and IDEA is definitely one of them, you can opt to use your favorite text
editor - VSCode, Sublime, ViM, Notepad++, Atom, etc - and use sbt to build the project. The only dependencies
necessary are sbt and access to the JDK.  Everything else should be deployed from working with those.  Run
commands in a command line tool with appropriate dependency visibility to start the server.

## Running the Server
The initial compile may take some time.  Sbt is powerful but slow to wake up.

To run a headless, non-interactive server, run
```
sbt server/run
```
PlanetSide can now connect to your server.
To run your custom server with an interactive `scala>` REPL, run
```
sbt server/console
```
![image](https://cloud.githubusercontent.com/assets/16912082/18024110/7b48dba8-6bc8-11e6-81d8-4692bc9d48a8.png)

To start the server and begin listening for connections, enter the following expression into the REPL:
```
Server.run
```
![image](https://cloud.githubusercontent.com/assets/16912082/18024137/1167452a-6bc9-11e6-8765-a86fb465de61.png)

This process is identical to running the headless, non-interactive server: PlanetSide clients can connect, logging
output will be printed to the screen, etc. The advantage is that you now have an interactive REPL that will evaluate any
Scala expression you type into it.
![image](https://cloud.githubusercontent.com/assets/16912082/18024339/62197f66-6bcd-11e6-90f7-5569d33472a7.png)

The REPL supports various useful commands. For example, to see the type of an arbitrary expression `foo`, run `:type
foo`. To print all members of a type, run `:javap -p some-type`. You can run `:help` to see a full list of commands.
![image](https://cloud.githubusercontent.com/assets/16912082/18024371/e0b72f9e-6bcd-11e6-9de5-421ec3eff994.png)

Tests that are packaged with the server project code can also be run using the command `sbt test`.  The anticipated
`/test/` directory should be reachable under the `/src/` directory.  IntelliJ IDEA allows hinting of the directory from
the context menu of the Project tab: `menu -> Mark Directory as -> Test Sources Root` or `Test Resources Root`.

The game server will automatically apply the latest schema to the database, updating sequential entries found in
`resources/db/migrations`. Migrations can also be applied manually using the
[Flyway CLI](https://flywaydb.org/documentation/commandline/).
Existing databases before the introduction of migrations must be baselined using the `flyway baseline` command.

### Troubleshooting
1. If dependency resolution results in certificate issues or generates a `/null/` directory into which some library
files are placed, the Java versioning is incorrectly applied.  Your system's Java, via `JAVA_HOME` environment variable,
must be advanced enough to operate the toolset and only the project itself requires JDK 8.  Check that project settings
import and utilize Java 1.8_251.  Perform normal generated file cleanup, e.g., sbt's `clean`. 
Any extraneous folders may also be deleted without issue.
2. If the server repeatedly complains that "authentication method 10 not supported" during startup, your PostgreSQL
database does not support [scram-sha-256](https://www.postgresql.org/docs/current/auth-password.html) authentication.
Check in your database configuration file `postgresql.conf` that `password_encryption` is set correctly; or, upgrade
your PostgreSQL version to one that supports scram-sha-256.  Whenever changing password encryption methods, all
existing passwords FOR THE USERS AND ROLES should be rehashed for the new encryption.

### Creating a Release
If you want to test the project without an IDE or deploy it to a server for run, you can use sbt-pack to create a
release (included with the repository). First make sure you have the [sbt tool](http://www.scala-sbt.org/download.html)
on your command line (or create a new task in IntelliJ IDEA). Then get a copy of the source directory (either in ZIP or
cloned form). Then do the below
```
cd PSF-LoginServer
sbt packArchiveZip # creates a single zip with resources
```
This will use the sbt-pack plugin to create a JAR file and some helper scripts to run the server. The output for this
will be in the `PSF-LoginServer/target` directory. Now you can copy the ZIP file to a server you want to run it on. You
will need the Java 8 runtime (JRE only) on the target to run this. In the ZIP file, there is a `bin/` directory with
some helper scripts. Run the correct file for your platform (.BAT for Windows and shell script for Unix).

## Tools
### decodePackets
The decodePackets program can be used to decode GameLogger `.gcap` packet captures. Requires
[gcapy](https://github.com/psforever/gcapy) to run, unless the `-p` flag is used.

To build, run:
```
sbt decodePackets/pack
```
The output will be in `tools/decode-packets/target/pack`. The `bin` folder contains scripts to
launch the program. On Linux, you can use the Makefile to install the files to any path:
```
make install PREFIX=$HOME/.local
```
Now you can run the program like that:
```
psf-decode-packets -o ./output-directory foo.gcap bar.gcap
```
By default, decodePackets takes in `.gcap` files, but it can also take gcapy ascii files with the
`-p` option. Run `psf-decode-packets --help` to get usage info.

### Generating Documentation
Using sbt, you can generate documentation all projects using `sbt docs/unidoc`.
Current documentation is available at [https://psforever.github.io/PSF-LoginServer/net/psforever/index.html](https://psforever.github.io/PSF-LoginServer/net/psforever/index.html)

## Contributing
Please fork the project and provide a pull request to contribute code. ~~Coding guidelines and contribution checklists
coming soon.~~

## Get in touch
- Website: http://psforever.net
- Discord (chat with us): https://discord.gg/0nRe5TNbTYoUruA4
  - Join the #code channel and ask any questions you have there

## License
GNU GPLv3. See LICENSE.md for the full copy.
