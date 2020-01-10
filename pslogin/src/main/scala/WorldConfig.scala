// Copyright (c) 2019 PSForever
import scala.util.matching.Regex
import net.psforever.config._
import scala.concurrent.duration._
import net.psforever.packet.game._

object WorldConfig extends ConfigParser {
  // hostname, but allow for empty string
  protected val hostname_pattern = Constraints.pattern(raw"^((([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9]))|()$$".r, "hostname")

  protected val config_template = Seq(
    ConfigSection("database",
      ConfigEntryString("Hostname", "localhost", hostname_pattern, Constraints.minLength(1)),
      ConfigEntryInt("Port", 5432, Constraints.min(1), Constraints.max(65535)),
      ConfigEntryEnum[ConfigDatabaseSSL.type]("SSL", ConfigDatabaseSSL.Prefer),
      ConfigEntryString("Database", "psforever", Constraints.minLength(1)),
      ConfigEntryString("Username", "psforever", Constraints.minLength(1)),
      ConfigEntryString("Password", "psforever", Constraints.minLength(1))
    ),
    ConfigSection("loginserver",
      ConfigEntryInt("ListeningPort", 51000, Constraints.min(1), Constraints.max(65535)),
      ConfigEntryBool("CreateMissingAccounts", true)
    ),
    ConfigSection("worldserver",
      ConfigEntryInt("ListeningPort", 51001, Constraints.min(1), Constraints.max(65535)),
      ConfigEntryString("Hostname", "", hostname_pattern),
      ConfigEntryString("ServerName", "PSForever", Constraints.minLength(1), Constraints.maxLength(31)),
      ConfigEntryEnum[ServerType.type]("ServerType", ServerType.Released)
    ),
    ConfigSection("network",
      ConfigEntryTime("Session.InboundGraceTime", 1 minute, Constraints.min(10 seconds)),
      ConfigEntryTime("Session.OutboundGraceTime", 1 minute, Constraints.min(10 seconds))
    ),
    ConfigSection("developer",
      ConfigEntryBool ("NetSim.Active", false),
      ConfigEntryFloat("NetSim.Loss", 0.02f, Constraints.min(0.0f), Constraints.max(1.0f)),
      ConfigEntryTime ("NetSim.Delay", 150 milliseconds, Constraints.min(0 seconds), Constraints.max(2 seconds)),
      ConfigEntryFloat("NetSim.ReorderChance", 0.005f, Constraints.min(0.0f), Constraints.max(1.0f)),
      ConfigEntryTime ("NetSim.ReorderTime", 150 milliseconds, Constraints.min(0 seconds), Constraints.max(2 seconds))
    )
  )

  object ConfigDatabaseSSL extends Enumeration {
    type Type = Value
    val Disable, Prefer, Require, Verify = Value
  }

  override def postParseChecks : ValidationResult = {
    var errors : Invalid = Invalid("")

    if (Get[Int]("worldserver.ListeningPort") == Get[Int]("loginserver.ListeningPort"))
      errors = errors ++ Invalid("worldserver.ListeningPort must be different from loginserver.ListeningPort")

    if (errors.errors.length > 1)
      // drop the first error using tail (it was a placeholder)
      Invalid(errors.errors.tail)
    else
      Valid
  }
}
