// Copyright (c) 2020 PSForever
package net.psforever.objects.zones

import enumeratum.values.{StringEnum, StringEnumEntry}
import net.psforever.objects.SpawnPoint
import net.psforever.types.{PlanetSideEmpire, Vector3}

object Zoning {
  sealed abstract class Method(val value: String) extends StringEnumEntry
  sealed abstract class Status(val value: String) extends StringEnumEntry

  object Method extends StringEnum[Method] {
    val values: IndexedSeq[Method] = findValues

    case object None extends Method(value = "None")
    case object InstantAction extends Method(value = "InstantAction")
    case object OutfitRecall extends Method(value = "OutfitRecall")
    case object Recall extends Method(value = "Recall")
    case object Quit extends Method(value = "Quit")
    case object Login extends Method(value = "Login")
    case object Reset extends Method(value = "Reset")
  }

  object Status extends StringEnum[Status] {
    val values: IndexedSeq[Status] = findValues

    case object None extends Status(value = "None")
    case object Request extends Status(value = "Request")
    case object Countdown extends Status(value = "Countdown")
  }

  object Time {
    sealed case class TimeType(id: Int, descriptor: String)

    final val Immediate = TimeType(0, "Immediate")
    final val Friendly  = TimeType(10, "Friendly")
    final val Sanctuary = TimeType(10, "Sanctuary")
    final val Neutral   = TimeType(20, "Neutral")
    final val None      = TimeType(20, "None")
    final val Enemy     = TimeType(30, "Enemy")
  }

  final case class Quit()

  object InstantAction {
    final case class Request(faction: PlanetSideEmpire.Value)

    final case class Located(zone: Zone, hotspot: Vector3, spawn_point: SpawnPoint)

    final case class NotLocated()
  }

  object Recall {
    final case class Request(faction: PlanetSideEmpire.Value, sanctuary_id: String)

    final case class Located(zone: Zone, spawn_point: SpawnPoint)

    final case class Denied(reason: String)
  }
}
