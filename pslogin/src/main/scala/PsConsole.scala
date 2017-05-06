// Copyright (c) 2017 PSForever
import org.jline.reader._;
import org.jline.terminal._;
import akka.actor.ActorSystem;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.ConsoleAppender;

class PsConsole(system : ActorSystem) {
  val terminal = TerminalBuilder.terminal()
  val reader = LineReaderBuilder.builder().
    terminal(terminal).
    build()

  // TODO: customize based on current context
  def getPrompt : String = {
    return "psf> "
  }

  def processCommand(sz : String) : Boolean = {
    if(sz == "")
      return false

    println("Cmd " + sz)

    val argv = sz.split(" ")

    val cmd = argv{0}
    val argc = argv.length - 1

    cmd match {
      case "exit" | "quit" | "shutdown" =>
        return true
      case "log" =>
        import collection.JavaConverters._;

        if(argc != 1)
          return false

        // Hacks to affect console logging only
        val root : Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
        val stdoutAppender = root.getAppender("STDOUT").asInstanceOf[ConsoleAppender[ILoggingEvent]]
        val filters = stdoutAppender.getCopyOfAttachedFiltersList

        filters.asScala.find(_.isInstanceOf[ThresholdFilter]).foreach(m => m.asInstanceOf[ThresholdFilter].setLevel(argv{1}))
        return false
      case _ =>
        return false
    }
  }

  def process : Unit = {
    var exit = false

    while(!exit) {
      try {
        val line = reader.readLine(getPrompt)
        exit = processCommand(line)
      } catch {
        case e : UserInterruptException =>
        case e : EndOfFileException =>
      }
    }

    close
    system.terminate()
  }

  def close : Unit = {
    terminal.close
  }
}
