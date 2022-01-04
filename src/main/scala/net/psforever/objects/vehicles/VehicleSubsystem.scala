// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import enumeratum.values.{StringEnum, StringEnumEntry}
import net.psforever.objects.Vehicle
import net.psforever.objects.equipment.JammableUnit
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.GenericObjectActionMessage
import net.psforever.types.PlanetSideGUID

/**
  * The parameters that comprise the subsystem,
  * its state changes,
  * and its interactions with the environment of the installing vehicle.
  * A subsystem can represent something substantial and quantifiable like a vehicle's weapon system
  * or something insubstantial like the ability to escape from the cockpit of a moving vehicle.
  * @param value the descriptive field
  * @param active the field attribute being active
  * @param inactive the field attribute being deactivate
  * @param defaultState the default activity state
  * @param damageable can be disabled by taking damage
  * @param jammable can be disabled by being jammed (vehicle can be jammered)
  * @param automaticPublish if state change occur due to jammering or damage,
  *                         automatically publish a packet to the event system;
  *                         `false` indicates this packet requires manual processing
  */
sealed abstract class VehicleSubsystemEntry(
                                             val value: String,
                                             val active: Int,
                                             val inactive: Int,
                                             val defaultState: Boolean,
                                             val damageable: Boolean,
                                             val jammable: Boolean,
                                             val automaticPublish: Boolean = true
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
                                           )
  extends VehicleSubsystemEntry(
    value,
    active,
    inactive,
    defaultState = true,
    damageable = false,
    jammable = false,
    automaticPublish = false
  ) {
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
    inactive=0,
    defaultState = true,
    damageable = true,
    jammable = true,
    automaticPublish = false//true
  )
  case object Ejection extends VehicleSubsystemEntry(
    value = "Ejection",
    active=0,
    inactive=50,
    defaultState = true,
    damageable = true,
    jammable = true,
    automaticPublish = false//true
  )
  case object MosquitoRadar extends VehicleSubsystemEntry(
    value = "MosquitoRadar",
    active=0,
    inactive=0,
    defaultState = false,
    damageable = false,
    jammable = true,
    automaticPublish = false//true
  )
  case object BattleframeTrunk extends VehicleSubsystemEntry(
    value = "BattleframeTrunk",
    active=0,
    inactive=0,
    defaultState = true,
    damageable = true,
    jammable = false,
    automaticPublish = false
  )
  case object BattleframeSensorArray extends VehicleSubsystemEntry(
    value = "BattleframeSensorArray",
    active=0,
    inactive=0,
    defaultState = true,
    damageable = true,
    jammable = true,
    automaticPublish = false//true
  )
  case object BattleframeShieldGenerator extends VehicleSubsystemEntry(
    value = "BattleframeShieldGenerator",
    active=44,
    inactive=45,
    defaultState = true,
    damageable = false,
    jammable = true,
    automaticPublish = false
  )
  case object BattleframeWeaponry extends VehicleSubsystemEntry(
    value = "BattleframeWeaponry",
    active=0,
    inactive=0,
    defaultState = true,
    damageable = true,
    jammable = true,
    automaticPublish = false//true
  )
  case object BattleframeFlightPod extends VehicleSubsystemEntry(
    value = "BattleframeFlightPod",
    active=0,
    inactive=0,
    defaultState = false,
    damageable = false,
    jammable = true,
    automaticPublish = false
  )
  case object BattleframeMovementServo extends VehicleSubsystemEntry(
    value = "BattleframeMovementServo",
    active=0,
    inactive=48, //49
    defaultState = true,
    damageable = true,
    jammable = true,
    automaticPublish = false//true
  )
  case object BattleframeLeftArm extends VehicleArmSubsystemEntry(
    value = "BattleframeLeftArmG",
    active=38,
    inactive=39,
    armSlot=2
  )
  case object BattleframeRightArm extends VehicleArmSubsystemEntry(
    value = "BattleframeRightArmG",
    active=38,
    inactive=39,
    armSlot=3
  )
  case object BattleframeFlightLeftArm extends VehicleArmSubsystemEntry(
    value = "BattleframeLeftArmF",
    active=38,
    inactive=39,
    armSlot=1
  )
  case object BattleframeFlightRightArm extends VehicleArmSubsystemEntry(
    value = "BattleframeRightArmF",
    active=38,
    inactive=39,
    armSlot=2
  )
}

class VehicleSubsystem(val sys: VehicleSubsystemEntry)
  extends JammableUnit {
  /** whether this subsystem is currently active or inactive */
  private var enabled: Boolean = sys.defaultState

  def Enabled: Boolean = !Jammed && enabled

  def Enabled_=(state: Boolean): Boolean = {
    if (Jammed) {
      enabled = state
    }
    Enabled
  }

  def getState(): Int = if (Enabled) sys.active else sys.inactive

  def getMessage(vehicle: Vehicle): PlanetSideGamePacket = {
    GenericObjectActionMessage(sys.getMessageGuid(vehicle), getState())
  }
}
