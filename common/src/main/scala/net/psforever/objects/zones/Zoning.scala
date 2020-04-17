package net.psforever.objects.zones

import net.psforever.objects.SpawnPoint
import net.psforever.types.{PlanetSideEmpire, Vector3}

object Zoning {
  object Method extends Enumeration {
    type Type = Value

    val
    None,
    InstantAction,
    Recall
    = Value
  }

  object Status extends Enumeration {
    type Type = Value

    val
    None,
    Request,
    Countdown
    = Value
  }

  object Time {
    sealed case class TimeType(id : Int, descriptor : String)

    final val Immediate = TimeType(0, "Immediate")
    final val Friendly = TimeType(10, "Friendly")
    final val Sanctuary = TimeType(10, "Sanctuary")
    final val Neutral = TimeType(20, "Neutral")
    final val None = TimeType(20, "None")
    final val Enemy = TimeType(30, "Enemy")
  }

  object InstantAction {
    final case class Request(faction : PlanetSideEmpire.Value)

    final case class Located(zone : Zone, hotspot : Vector3, spawn_point : SpawnPoint)

    final case class NotLocated()
  }

  object Recall {
    final case class Request(faction : PlanetSideEmpire.Value, sanctuary_id : String)

    final case class Located(zone : Zone, spawn_point : SpawnPoint)

    final case class Denied(reason : String)
  }
}
