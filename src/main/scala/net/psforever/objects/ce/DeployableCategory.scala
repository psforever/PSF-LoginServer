// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

abstract class DeployableCategory(val name: String)

object DeployableCategory {
  case object None extends DeployableCategory(name = "None")

  case object Boomers extends DeployableCategory(name = "Boomers")

  case object Mines extends DeployableCategory(name = "Mines")

  case object SmallTurrets extends DeployableCategory(name = "SmallTurrets")

  case object Sensors extends DeployableCategory(name = "Sensors")

  case object TankTraps extends DeployableCategory(name = "TankTraps")

  case object FieldTurrets extends DeployableCategory(name = "FieldTurrets")

  case object ShieldGenerators extends DeployableCategory(name = "ShieldGenerators")

  case object Telepads extends DeployableCategory(name = "Telepads")

  val values: Seq[DeployableCategory] = Seq(None, Boomers, Mines, SmallTurrets, Sensors, TankTraps, FieldTurrets, ShieldGenerators, Telepads)
}
