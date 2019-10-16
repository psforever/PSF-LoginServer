// Copyright (c) 2019 PSForever
import net.psforever.config._

object WorldConfig extends ConfigParser {
  protected var config_map : Map[String, Any] = Map()

  protected val config_template = Seq(
    ConfigSection("loginserver",
      ConfigEntryInt("ListeningPort", 51000, Constraints.min(1), Constraints.max(65535))
    ),
    ConfigSection("worldserver",
      ConfigEntryInt("ListeningPort", 51001, Constraints.min(1), Constraints.max(65535))
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
