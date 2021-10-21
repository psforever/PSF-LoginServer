// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import enumeratum.values.{StringEnum, StringEnumEntry}
import net.psforever.objects.Vehicle
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.GenericObjectActionMessage
import net.psforever.types.PlanetSideGUID

sealed abstract class VehicleSubsystemEntry(
                                             val value: String,
                                             val active: Int,
                                             val inactive: Int
                                           ) extends StringEnumEntry {
  def getMessageGuid(vehicle: Vehicle): PlanetSideGUID = {
    vehicle.GUID
  }
}

sealed abstract class VehicleArmSubsystemEntry(
                                             override val value: String,
                                             override val active: Int,
                                             override val inactive: Int,
                                             armSlot: Int
                                           ) extends VehicleSubsystemEntry(value, active, inactive) {
  override def getMessageGuid(vehicle: Vehicle): PlanetSideGUID = {
    vehicle.Weapons.get(armSlot) match {
      case Some(slot) if slot.Equipment.nonEmpty =>
        slot.Equipment.get.GUID
      case _ =>
        PlanetSideGUID(0)
    }
  }
}

object VehicleSubsystemEntry extends StringEnum[VehicleSubsystemEntry] {
  val values = findValues

  case object Controls extends VehicleSubsystemEntry(
    value = "Controls",
    active=0,
    inactive=0
  )
  case object Ejection extends VehicleSubsystemEntry(
    value = "Ejection",
    active=0,
    inactive=50
  )
  case object MosquitoRadar extends VehicleSubsystemEntry(
    value = "MosquitoRadar",
    active=0,
    inactive=0
  )
  case object BattleframeTrunk extends VehicleSubsystemEntry(
    value = "BattleframeTrunk",
    active=0,
    inactive=0
  )
  case object BattleframeSensorArray extends VehicleSubsystemEntry(
    value = "BattleframeSensorArray",
    active=0,
    inactive=0
  )
  case object BattleframeShieldGenerator extends VehicleSubsystemEntry(
    value = "BattleframeShieldGenerator",
    active=44,
    inactive=45
  )
  case object BattleframeWeaponry extends VehicleSubsystemEntry(
    value = "BattleframeWeaponry",
    active=0,
    inactive=0
  )
  case object BattleframeFlightPod extends VehicleSubsystemEntry(
    value = "BattleframeFlightPod",
    active=0,
    inactive=0
  )
  case object BattleframeMovementServo extends VehicleSubsystemEntry(
    value = "BattleframeMovementServo",
    active=0,
    inactive=48 //49
  )
  case object BattleframeLeftArm extends VehicleArmSubsystemEntry(
    value = "BattleframeLeftArm",
    active=38,
    inactive=39,
    armSlot=2
  )
  case object BattleframeRightArm extends VehicleArmSubsystemEntry(
    value = "BattleframeRightArm",
    active=38,
    inactive=39,
    armSlot=3
  )
}

class VehicleSubsystem(val sys: VehicleSubsystemEntry) {
  var enabled: Boolean = true

  def getState(): Int = if (enabled) sys.active else sys.inactive

  def getMessage(vehicle: Vehicle): PlanetSideGamePacket = {
    GenericObjectActionMessage(sys.getMessageGuid(vehicle), getState())
  }
}
