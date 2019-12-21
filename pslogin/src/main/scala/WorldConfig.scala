// Copyright (c) 2019 PSForever
import net.psforever.config._
import scala.concurrent.duration._

object WorldConfig extends ConfigParser {
  protected var config_map : Map[String, Any] = Map()

  protected val config_template = Seq(
    ConfigSection("loginserver",
      ConfigEntryInt("ListeningPort", 51000, Constraints.min(1), Constraints.max(65535))
    ),
    ConfigSection("worldserver",
      ConfigEntryInt("ListeningPort", 51001, Constraints.min(1), Constraints.max(65535))
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
