// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Vehicle}
import net.psforever.objects.equipment.{Equipment, JammableUnit}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ComponentDamageField, ComponentDamageMessage, GenericObjectActionMessage}
import net.psforever.types.{PlanetSideGUID, SubsystemComponent}

//data

sealed abstract class VehicleSubsystemConditionModifier(
                                                         val value: Int,
                                                         val multiplier: Float,
                                                         val addend: Int
                                                       ) extends IntEnumEntry

object VehicleSubsystemConditionModifier extends IntEnum[VehicleSubsystemConditionModifier] {
  val values = findValues

  case object Off extends VehicleSubsystemConditionModifier(value = 1065353216, multiplier = 0f, addend = 0)

  case object Decay20 extends VehicleSubsystemConditionModifier(value = 1061997772, multiplier = 0.8f, addend = 0)
  case object Decay40 extends VehicleSubsystemConditionModifier(value = 1058642329, multiplier = 0.6f, addend = 0)
  case object Decay60 extends VehicleSubsystemConditionModifier(value = 1053609164, multiplier = 0.4f, addend = 0)

  case object Decay55 extends VehicleSubsystemConditionModifier(value = 1055286886, multiplier = 0.45f, addend = 0)
  case object Decay90 extends VehicleSubsystemConditionModifier(value = 1036831948, multiplier = 0.1f, addend = 0)

  case object Range25 extends VehicleSubsystemConditionModifier(value = 1061158912, multiplier = 1f, addend = -25)
  case object Range50 extends VehicleSubsystemConditionModifier(value = 1056964608, multiplier = 1f, addend = -50)

  case object Add100 extends VehicleSubsystemConditionModifier(value = 1073741824, multiplier = 200.0f, addend = 0)
  case object Add200 extends VehicleSubsystemConditionModifier(value = 1077936128, multiplier = 300.0f, addend = 0)
  case object Add300 extends VehicleSubsystemConditionModifier(value = 1082130432, multiplier = 400.0f, addend = 0)
  case object Add500 extends VehicleSubsystemConditionModifier(value = 1086324736, multiplier = 600.0f, addend = 0)
  case object Add700 extends VehicleSubsystemConditionModifier(value = 1090519040, multiplier = 800.0f, addend = 0)
}

//Conditions

trait VehicleSubsystemCondition {
  def getMultiplier(): Float = 1f

  def getMessage(id: SubsystemComponent, vehicle: Vehicle, guid: PlanetSideGUID): List[PlanetSideGamePacket]
}

final case class VehicleComponentCondition(
                                            alarmLevel: Long,
                                            factor: VehicleSubsystemConditionModifier,
                                            unk: Boolean
                                          ) extends VehicleSubsystemCondition {
  override def getMultiplier(): Float = factor.multiplier

  def getMessage(id: SubsystemComponent, vehicle: Vehicle, guid: PlanetSideGUID): List[PlanetSideGamePacket] = {
    if (vehicle.Jammed) {
      List(ComponentDamageMessage(guid, id, Some(ComponentDamageField(alarm_level = 0, factor.value, unk))))
    } else {
      List(ComponentDamageMessage(guid, id, Some(ComponentDamageField(alarmLevel, factor.value, unk))))
    }
  }
}

object VehicleComponentCondition {
  def apply(alarm: Long, factor: VehicleSubsystemConditionModifier): VehicleComponentCondition =
    VehicleComponentCondition(alarm, factor, unk = true)
}

sealed abstract class BattleframeArmMountCondition(code: Int) extends VehicleSubsystemCondition {
  def getMessage(id: SubsystemComponent, vehicle: Vehicle, guid: PlanetSideGUID): List[PlanetSideGamePacket] = {
    List(GenericObjectActionMessage(guid, code))
  }
}

private case object BattleframeArmActive extends BattleframeArmMountCondition(code = 38)

private case object BattleframeArmInactive extends BattleframeArmMountCondition(code = 39)

//Statuses

trait VehicleSubsystemStatus {
  def name: String
  def effects: List[VehicleSubsystemCondition]
  def priority: Int
  def jamState: Int
  def damageState: Option[Any]

  def damageable: Boolean = damageState.nonEmpty

  def jammable: Boolean = jamState > 0

  def getMessageTarget(vehicle: Vehicle): Option[IdentifiableEntity] = Some(vehicle)

  def getMessageTargetId(vehicle: Vehicle): PlanetSideGUID = vehicle.GUID

  def getMessage(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket]

  def jammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket]

  def clearJammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket]
}

trait VehicleSubsystemComponent
  extends VehicleSubsystemStatus {
  def componentId: SubsystemComponent

  def getMessage(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    effects(toState).getMessage(componentId, vehicle, getMessageTargetId(vehicle))
  }

  def jammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    if (jammable && toState < jamState) {
      effects(jamState).getMessage(componentId, vehicle, getMessageTargetId(vehicle))
    } else {
      Nil
    }
  }

  def clearJammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    if (jammable) {
      effects(math.max(0, toState)).getMessage(componentId, vehicle, getMessageTargetId(vehicle))
    } else {
      Nil
    }
  }
}

final case class VehicleComponentStatus(
                                         name: String,
                                         componentId: SubsystemComponent,
                                         effects: List[VehicleSubsystemCondition],
                                         damageState: Option[Any],
                                         jamState: Int,
                                         priority: Int
                                       ) extends VehicleSubsystemComponent

object VehicleComponentStatus {
  def apply(state: String, cid: SubsystemComponent, states: List[VehicleSubsystemCondition]): VehicleComponentStatus =
    VehicleComponentStatus(state, cid, states, damageState = None, jamState = 0, priority = 0)
}

trait VehicleWeaponStatus
  extends VehicleSubsystemStatus {
  def slotIndex: Int

  override def getMessageTarget(vehicle : Vehicle) : Option[PlanetSideGameObject] = {
    vehicle.Weapons.get(slotIndex) match {
      case Some(slot) => slot.Equipment
      case None       => throw new IllegalArgumentException(s"subsystem for battleframe arm mount missing mount - $slotIndex")
    }
  }

  override def getMessageTargetId(vehicle: Vehicle): PlanetSideGUID = {
    getMessageTarget(vehicle) match {
      case Some(e) => e.GUID
      case None    => PlanetSideGUID(0)
    }
  }

  override def getMessage(toState: Int, vehicle: Vehicle) : List[PlanetSideGamePacket] = {
    getMessageTarget(vehicle) match {
      case Some(_) => effects(toState).getMessage(id = SubsystemComponent.Unknown(36), vehicle, getMessageTargetId(vehicle))
      case None    => Nil
    }
  }
}

sealed abstract class BattleframeWeaponComponent extends VehicleSubsystemComponent with VehicleWeaponStatus {
  override def getMessage(toState: Int, vehicle: Vehicle) : List[PlanetSideGamePacket] = {
    getMessageTarget(vehicle) match {
      case Some(_) => effects(toState).getMessage(componentId, vehicle, getMessageTargetId(vehicle))
      case None    => Nil
    }
  }
}

final case class BattleframeWeaponComponentStatus(
                                                   override val name: String,
                                                   override val componentId: SubsystemComponent,
                                                   override val effects: List[VehicleSubsystemCondition],
                                                   override val damageState: Option[Any],
                                                   override val jamState: Int,
                                                   override val priority: Int,
                                                   slotIndex: Int
                                                 ) extends BattleframeWeaponComponent

final case class BattleframeWeaponOnlyComponentStatus(
                                                       override val name: String,
                                                       override val componentId: SubsystemComponent,
                                                       override val effects: List[VehicleSubsystemCondition],
                                                       override val damageState: Option[Any],
                                                       override val jamState: Int,
                                                       override val priority: Int,
                                                       slotIndex: Int
                                                     ) extends BattleframeWeaponComponent {
  override def getMessageTarget(vehicle : Vehicle) : Option[PlanetSideGameObject] = {
    super.getMessageTarget(vehicle) match {
      case out @ Some(e: Equipment)
        if !(GlobalDefinitions.isBattleFrameArmorSiphon(e.Definition) ||
             GlobalDefinitions.isBattleFrameNTUSiphon(e.Definition)) =>
        out
      case _ =>
        None
    }
  }
}

final case class BattleframeSiphonOnlyComponentStatus(
                                                       override val name: String,
                                                       override val componentId: SubsystemComponent,
                                                       override val effects: List[VehicleSubsystemCondition],
                                                       override val damageState: Option[Any],
                                                       override val jamState: Int,
                                                       override val priority: Int,
                                                       slotIndex: Int
                                                     ) extends BattleframeWeaponComponent {
  override def getMessageTarget(vehicle : Vehicle) : Option[PlanetSideGameObject] = {
    super.getMessageTarget(vehicle) match {
      case out @ Some(e: Equipment)
        if GlobalDefinitions.isBattleFrameArmorSiphon(e.Definition) ||
           GlobalDefinitions.isBattleFrameNTUSiphon(e.Definition) =>
        out
      case _ =>
        None
    }
  }
}

final case class BattleframeArmorSiphonComponentStatus(
                                                       override val name: String,
                                                       override val componentId: SubsystemComponent,
                                                       override val effects: List[VehicleSubsystemCondition],
                                                       override val damageState: Option[Any],
                                                       override val jamState: Int,
                                                       override val priority: Int,
                                                       slotIndex: Int
                                                     ) extends BattleframeWeaponComponent {
  override def getMessageTarget(vehicle : Vehicle) : Option[PlanetSideGameObject] = {
    super.getMessageTarget(vehicle) match {
      case out @ Some(e: Equipment)
        if GlobalDefinitions.isBattleFrameArmorSiphon(e.Definition) =>
        out
      case _ =>
        None
    }
  }
}

final case class BattleframeWeaponToggle(slotIndex: Int)
  extends VehicleWeaponStatus {
  def name: String = "Toggle"

  def effects: List[VehicleSubsystemCondition] = List(BattleframeArmActive, BattleframeArmInactive)

  def priority: Int = 0

  def jamState: Int = 0

  def damageState: Option[Any] = None

  def jammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket] = Nil

  def clearJammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket] = Nil
}

//Entry

trait VehicleSubsystemFields {
  def name: String

  def statuses: List[VehicleSubsystemStatus]

  def startsEnabled: Boolean

  def enabledStatus: List[String]

  def defaultState: Boolean = startsEnabled

  def damageable: Boolean = statuses.exists { _.damageable }

  def jammable: Boolean = statuses.exists { _.jammable }
}

sealed abstract class VehicleSubsystemEntry(
                                             val name: String,
                                             val statuses: List[VehicleSubsystemStatus],
                                             val startsEnabled: Boolean,
                                             val enabledStatus: List[String]
                                            ) extends VehicleSubsystemFields {
  def this(value: String, statuses: List[VehicleSubsystemStatus]) =
    this(value, statuses, startsEnabled = true, enabledStatus = Nil)

//  def jammerMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = Nil //TODO
//
//  def clearJammerMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = Nil //TODO
//
//  def getMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = Nil //TODO
//
//  def currentMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = Nil //TODO
}

sealed abstract class BattleframeArmToggleEntry(override val name: String, slotIndex: Int)
  extends VehicleSubsystemEntry(
    name,
    statuses = List(BattleframeWeaponToggle(slotIndex)),
    startsEnabled = true,
    enabledStatus = List("Toggle")
  )

sealed abstract class BattleframeArmWeaponEntry(override val name: String, slotIndex: Int) extends VehicleSubsystemEntry(
  name,
  statuses = List(
    //weapons only
    BattleframeWeaponOnlyComponentStatus(
      "COFRecovery",
      SubsystemComponent.WeaponSystemsCOFRecovery,
      List(
        VehicleComponentNormal,
        VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Add100),
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Add200),
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Add300),
        VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Add500),
        VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Add700)
      ),
      damageState = Some(1),
      jamState = 1,
      priority = 0,
      slotIndex
    ),
    BattleframeWeaponOnlyComponentStatus(
      "COF",
      SubsystemComponent.WeaponSystemsCOF,
      List(
        VehicleComponentNormal,
        VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Add100),
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Add200),
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Add300),
        VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Add500),
        VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Add700)
      ),
      damageState = Some(1),
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    BattleframeWeaponOnlyComponentStatus(
      "AmmoLoss",
      SubsystemComponent.WeaponSystemsAmmoLoss,
      List(
        VehicleComponentNormal,
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Off)
      ),
      damageState = None,
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    BattleframeWeaponOnlyComponentStatus(
      "RefireTime",
      SubsystemComponent.WeaponSystemsRefireTime,
      List(
        VehicleComponentNormal,
        VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Add100),
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Add200),
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Add300),
        VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Add500),
        VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Add700)
      ),
      damageState = Some(1),
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    BattleframeWeaponOnlyComponentStatus(
      "ReloadTime",
      SubsystemComponent.WeaponSystemsReloadTime,
      List(
        VehicleComponentNormal,
        VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Add100),
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Add200),
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Add300),
        VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Add500),
        VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Add700)
      ),
      damageState = Some(1),
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    //siphons only
    BattleframeSiphonOnlyComponentStatus(
      "TransferEfficiency",
      SubsystemComponent.SiphonTransferEfficiency,
      List(VehicleComponentNormal,PlaceholderNormalReplaceLater),
      damageState = None,
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    BattleframeSiphonOnlyComponentStatus(
      "TransferRate",
      SubsystemComponent.SiphonTransferRateA,
      List(VehicleComponentNormal,PlaceholderNormalReplaceLater),
      damageState = None,
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    BattleframeSiphonOnlyComponentStatus(
      "DrainOnly",
      SubsystemComponent.SiphonDrainOnly,
      List(VehicleComponentNormal,PlaceholderNormalReplaceLater),
      damageState = None,
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    BattleframeSiphonOnlyComponentStatus(
      "StorageCapacity",
      SubsystemComponent.SiphonStorageCapacity,
      List(VehicleComponentNormal,PlaceholderNormalReplaceLater),
      damageState = None,
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    BattleframeSiphonOnlyComponentStatus(
      "TransferRate",
      SubsystemComponent.SiphonTransferRateB,
      List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
      damageState = None,
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    //unknown
    BattleframeWeaponComponentStatus(
      "ProjectileRange",
      SubsystemComponent.UnknownProjectileRange,
      List(
        VehicleComponentNormal,
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Decay55),
        VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Decay90)
      ),
      damageState = None,
      jamState = 1,
      priority = 0,
      slotIndex
    ),
    BattleframeWeaponComponentStatus(
      "SensorRange",
      SubsystemComponent.UnknownSensorRange,
      List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
      damageState = None,
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    BattleframeWeaponComponentStatus(
      "RechargeInterval",
      SubsystemComponent.UnknownRechargeInterval,
      List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
      damageState = None,
      jamState = 0,
      priority = 0,
      slotIndex
    ),
    //all
    BattleframeWeaponComponentStatus(
      "Online",
      SubsystemComponent.WeaponSystemsOffline,
      List(
        VehicleComponentNormal,
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Off)
      ),
      damageState = Some(1),
      jamState = 0,
      priority = 1,
      slotIndex
    ),
    BattleframeWeaponComponentStatus(
      "Destroyed",
      SubsystemComponent.WeaponSystemsDestroyed,
      List(
        VehicleComponentNormal,
        VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Off)
      ),
      damageState = Some(1),
      jamState = 0,
      priority = 2,
      slotIndex
    )
  ),
  startsEnabled = true,
  enabledStatus = List("Online", "Destroyed")
)

////

sealed abstract class BattleframeShieldGeneratorCondition(code: Int) extends VehicleSubsystemCondition {
  def getMessage(id: SubsystemComponent, vehicle: Vehicle, guid: PlanetSideGUID): List[PlanetSideGamePacket] = {
    if (vehicle.Shields > 0) {
      List(GenericObjectActionMessage(guid, code))
    } else {
      Nil
    }
  }
}

case object PlaceholderNormalReplaceLater extends VehicleSubsystemCondition {
  def getMessage(id: SubsystemComponent, vehicle: Vehicle, guid: PlanetSideGUID): List[PlanetSideGamePacket] = Nil
}

case object VehicleComponentNormal extends VehicleSubsystemCondition {
  def getMessage(id: SubsystemComponent, vehicle: Vehicle, guid: PlanetSideGUID): List[PlanetSideGamePacket] = {
    List(ComponentDamageMessage(guid, id, None))
  }
}

private case object BattleframeShieldGeneratorOnline extends BattleframeShieldGeneratorCondition(code = 44)

private case object BattleframeShieldGeneratorOffline extends BattleframeShieldGeneratorCondition(code = 45) {
  override def getMultiplier(): Float = 0f
}

private case object BattleframeShieldGeneratorFixed extends VehicleSubsystemCondition {
  def getMessage(id: SubsystemComponent, vehicle: Vehicle, guid: PlanetSideGUID): List[PlanetSideGamePacket] = {
    List(ComponentDamageMessage(guid, id, None))
  }
}

private case object BattleframeShieldGeneratorDamaged extends VehicleSubsystemCondition {
  override def getMultiplier(): Float = 0f

  def getMessage(id: SubsystemComponent, vehicle: Vehicle, guid: PlanetSideGUID): List[PlanetSideGamePacket] = {
    if (vehicle.SubsystemStatus("BattleframeShieldGenerator.Online").contains(true)) {
      List(ComponentDamageMessage(guid, id, Some(ComponentDamageField(4, VehicleSubsystemConditionModifier.Off.value))))
    } else {
      Nil
    }
  }
}

private case object BattleframeShieldGeneratorDestroyed extends VehicleSubsystemCondition {
  override def getMultiplier(): Float = 0f

  def getMessage(id: SubsystemComponent, vehicle: Vehicle, guid: PlanetSideGUID): List[PlanetSideGamePacket] = {
    BattleframeShieldGeneratorOffline.getMessage(id, vehicle, guid) ++
    List(ComponentDamageMessage(guid, id, Some(
      ComponentDamageField(4, VehicleSubsystemConditionModifier.Off.value, unk = false)
    )))
  }
}

object VehicleSubsystemEntry {
  case object Controls extends VehicleSubsystemEntry(
    name = "Controls",
    statuses = List(
      VehicleComponentStatus(
        "Impaired",
        SubsystemComponent.Unknown(36),
        List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
        damageState = None,
        jamState = 0,
        priority = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = List("Impaired")
  )

  case object Ejection extends VehicleSubsystemEntry(
    name = "Ejection",
    statuses = List(
      VehicleComponentStatus(
        "Online",
        SubsystemComponent.Unknown(36),
        List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
        damageState = Some(1),
        jamState = 0,
        priority = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = List("Online")
  )

  case object MosquitoRadar extends VehicleSubsystemEntry(
    name = "MosquitoRadar",
    statuses = List(
      VehicleComponentStatus(
        "Online",
        SubsystemComponent.Unknown(36),
        List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
        damageState = None,
        jamState = 0,
        priority = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = List("Online")
  )

  case object BattleframeMovementServos extends VehicleSubsystemEntry(
    name = "BattleframeMovementServos",
    statuses = List(
      VehicleComponentStatus(
        "Transit",
        SubsystemComponent.MovementServosTransit,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Off)
        ),
        damageState = Some(1),
        jamState = 1,
        priority = 0
      ),
      VehicleComponentStatus(
        "Backward",
        SubsystemComponent.MovementServosBackward,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Decay20),
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Decay40),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Decay60)
        ),
        damageState = Some(1),
        jamState = 1,
        priority = 0
      ),
      VehicleComponentStatus(
        "Forward",
        SubsystemComponent.MovementServosForward,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Decay20),
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Decay40),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Decay60)
        ),
        damageState = Some(1),
        jamState = 1,
        priority = 0
      ),
      VehicleComponentStatus(
        "PivotSpeed",
        SubsystemComponent.MovementServosPivotSpeed,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Decay20),
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Decay40),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Decay60)
        ),
        damageState = Some(1),
        jamState = 2,
        priority = 0
      ),
      VehicleComponentStatus(
        "StrafeSpeed",
        SubsystemComponent.MovementServosStrafeSpeed,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Decay20),
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Decay40),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Decay60)
        ),
        damageState = Some(1),
        jamState = 1,
        priority = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = Nil
  )

  case object BattleframeSensorArray extends VehicleSubsystemEntry(
    name = "BattleframeSensorArray",
    statuses = List(
      VehicleComponentStatus(
        "NoEnemies",
        SubsystemComponent.SensorArrayNoEnemies,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Off)
        ),
        damageState = Some(1),
        jamState = 1,
        priority = 1
      ),
      VehicleComponentStatus(
        "NoEnemyAircraft",
        SubsystemComponent.SensorArrayNoEnemyAircraft,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Off)
        ),
        damageState = Some(1),
        jamState = 0,
        priority = 0
      ),
      VehicleComponentStatus(
        "NoEnemyGroundVehicles",
        SubsystemComponent.SensorArrayNoEnemyGroundVehicles,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Off)
        ),
        damageState = Some(1),
        jamState = 0,
        priority = 0
      ),
      VehicleComponentStatus(
        "NoEnemyProjectiles",
        SubsystemComponent.SensorArrayNoEnemyProjectiles,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Off)
        ),
        damageState = Some(1),
        jamState = 0,
        priority = 0
      ),
      VehicleComponentStatus(
        "SensorRange",
        SubsystemComponent.SensorArrayRange,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Range50),
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Range25),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Off)
        ),
        damageState = Some(1),
        jamState = 0,
        priority = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = List("NoEnemies"),
  )

  case object BattleframeFlightPod extends VehicleSubsystemEntry(
    name = "BattleframeFlightPod",
    statuses = List(
      VehicleComponentStatus(
        "RechargeRate",
        SubsystemComponent.FlightSystemsRechargeRate,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Decay20),
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Decay40),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Decay60)
        )
      ),
      VehicleComponentStatus(
        "UseRate",
        SubsystemComponent.FlightSystemsUseRate,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Add100),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Add200)
        )
      ),
      VehicleComponentStatus(
        "HorizontalForce",
        SubsystemComponent.FlightSystemsHorizontalForce,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Decay20),
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Decay40),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Decay60)
        )
      ),
      VehicleComponentStatus(
        "VerticalForce",
        SubsystemComponent.FlightSystemsVerticalForce,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Decay20),
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Decay40),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Decay60)
        )
      ),
      VehicleComponentStatus(
        "Online",
        SubsystemComponent.FlightSystemsOffline,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Off, unk = false)
        ),
        damageState = Some(1),
        jamState = 1,
        priority = 1
      ),
      VehicleComponentStatus(
        "Destroyed",
        SubsystemComponent.FlightSystemsOffline,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(7, VehicleSubsystemConditionModifier.Off, unk = false)
        ),
        damageState = Some(1),
        jamState = 0,
        priority = 2
      )
    ),
    startsEnabled = true,
    enabledStatus = List("Online", "Destroyed")
  )

  case object BattleframeShieldGenerator extends VehicleSubsystemEntry(
    name = "BattleframeShieldGenerator",
    List(
      VehicleComponentStatus(
        "RechargeRate",
        SubsystemComponent.ShieldGeneratorRechargeRate,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(3, VehicleSubsystemConditionModifier.Decay20),
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Decay40),
          VehicleComponentCondition(5, VehicleSubsystemConditionModifier.Decay60)
        ),
        damageState = None,
        jamState = 2,
        priority = 0
      ),
      VehicleComponentStatus(
        "Online",
        SubsystemComponent.Unknown(36), //doesn't matter
        List(
          BattleframeShieldGeneratorOnline,
          BattleframeShieldGeneratorOffline
        ),
        damageState = None,
        jamState = 0,
        priority = 1
      ),
      VehicleComponentStatus(
        "Damaged",
        SubsystemComponent.ShieldGeneratorOffline,
        List(
          VehicleComponentNormal,
          BattleframeShieldGeneratorDamaged
        ),
        damageState = Some(1),
        jamState = 1,
        priority = 1
      ),
      VehicleComponentStatus(
        "Destroyed",
        SubsystemComponent.ShieldGeneratorDestroyed,
        List(
          BattleframeShieldGeneratorFixed,
          BattleframeShieldGeneratorDestroyed
        ),
        damageState = Some(1),
        jamState = 0,
        priority = 2
      )
    ),
    startsEnabled = true,
    enabledStatus = List("Online", "Damaged", "Destroyed")
  )

  case object BattleframeTrunk extends VehicleSubsystemEntry(
    name = "BattleframeTrunk",
    statuses = List(
      VehicleComponentStatus(
        "Damaged",
        SubsystemComponent.Trunk,
        List(
          VehicleComponentNormal,
          VehicleComponentCondition(4, VehicleSubsystemConditionModifier.Off) //one item destroyed
        ),
        damageState = Some(1),
        jamState = 0,
        priority = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = Nil
  )

  case object BattleframeLeftArm extends BattleframeArmToggleEntry(name = "BattleframeLeftArm", slotIndex = 2)

  case object BattleframeRightArm extends BattleframeArmToggleEntry(name = "BattleframeRightArm", slotIndex = 3)

  case object BattleframeFlightLeftArm extends BattleframeArmToggleEntry(name = "BattleframeLeftArmF", slotIndex = 1)

  case object BattleframeFlightRightArm extends BattleframeArmToggleEntry(name = "BattleframeRightArmF", slotIndex = 2)

  case object BattleframeLeftWeapon extends BattleframeArmWeaponEntry(name = "BattleframeLeftWeapon", slotIndex = 2)

  case object BattleframeRightWeapon extends BattleframeArmWeaponEntry(name = "BattleframeRightWeapon", slotIndex = 3)

  case object BattleframeGunnerWeapon extends BattleframeArmWeaponEntry(name = "BattleframeGunnerWeapon", slotIndex = 4)

  case object BattleframeFlightLeftWeapon extends BattleframeArmWeaponEntry(name = "BattleframeLeftWeaponF", slotIndex = 1)

  case object BattleframeFlightRightWeapon extends BattleframeArmWeaponEntry(name = "BattleframeRightWeaponF", slotIndex = 2)
}

class VehicleSubsystem(val sys: VehicleSubsystemEntry)
  extends JammableUnit {
  /** na */
  private val damageStates: Array[VehicleSubsystemStatusMonitor] = {
    sys.statuses.zipWithIndex.map { case (a, i) => new VehicleSubsystemStatusMonitor(a, i) }.toArray
  }
  /** na */
  private var activeJammedMsgs: List[Int] = Nil
  /** na */
  private var changedWhilejammed: Array[Int] = Array.emptyIntArray
  /** whether this subsystem is currently active or inactive */
  private var enabled: Boolean = sys.startsEnabled
  /** these statuses must be in good condition for the subsystem to be considered operational (enabled) */
  private val enabledStatusIndices = sys.enabledStatus.map {
    str => sys.statuses.indexWhere { _.name.equals(str) }
  }
  //first enabled status condition defaults to a damaged state if the subsystem defaults to disabled
  if (!enabled && enabledStatusIndices.nonEmpty) {
    damageStates(enabledStatusIndices.head).Condition = 1
  }

  /**
    * If this subsystem is activated,
    * any and all accredited statuses are considered healthy and/or
    * the internal field (if a primary flag) is set and
    * the subsystem is not jammed.
    * @return whether the subsystem is activated
    */
  def Enabled: Boolean = {
    !Jammed && (if (enabledStatusIndices.nonEmpty) {
      enabledStatusIndices.forall { damageStates(_).Condition == 0 }
    } else {
      enabled
    })
  }

  /**
    * Treat this subsystem as activated or deactivated.
    * If this subsystem has specific statuses whose conditions are linked to its activation state,
    * the first accredited status is selected and set to the same activation state.
    * @param state the new state of the subsystem
    * @return the new state of the subsystem
    */
  def Enabled_=(state: Boolean): Boolean = {
    if (enabled != state && enabledStatusIndices.nonEmpty) {
      //for any VehicleSubsystemEvent.status, index=0 is normal and index>0 is damaged/jammed/disabled
      val condIndex = enabledStatusIndices.head
      val stateAsInt = if (state) { 0 } else { 1 }
      if ((stateAsInt == 1 && damageStates(condIndex).Condition == 0) ||
          (stateAsInt == 0 && damageStates(condIndex).Condition > 0)) {
        damageStates(condIndex).Condition = stateAsInt
      }
    }
    enabled = state
    Enabled
  }

  def jam(): Unit = {
    val statuses = sys.statuses
    if (sys.jammable && activeJammedMsgs.isEmpty) {
      val indexed = statuses.indices.toList
      //find the highest priority amongst the damaged status conditions
      //if no damage, default to lowest priority - 0
      val highestPriorityDamagedStatus = indexed
        .filter { i => damageStates(i).Condition > 0 }
        .maxByOption { i => statuses(i).priority } match {
        case Some(i) => statuses(i).priority
        case None    => 0
      }
      //find all jammable statuses with priority equal to or greater than the highest priority
      //ignore all statuses where its current damage state is higher than its jam state (jamming does nothing)
      //turn the resulting statuses into packets
      val indices = indexed
        .filter { i =>
          val status = statuses(i)
          status.jammable &&
          status.priority >= highestPriorityDamagedStatus &&
          status.jamState > damageStates(i).Condition
        }
      activeJammedMsgs = indices
      indices.foreach { i => damageStates(i).jam() }
      Jammed = indices.nonEmpty
    }
  }

  def unjam(): Unit = {
    if (activeJammedMsgs.nonEmpty) {
      activeJammedMsgs.foreach { i => damageStates(i).jam() }
      activeJammedMsgs = Nil
      Jammed = false
    }
  }

  def messagesForStatus(statusName: String, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    sys.statuses.indexWhere { _.name.contains(statusName) } match {
      case -1 => Nil
      case n  => sys.statuses(n).getMessage(damageStates(n).Condition, vehicle)
    }
  }

  def stateOfStatus(statusName: String): Option[Boolean] = {
    damageStates.find { _.status.name.contains(statusName) } match {
      case Some(status) => Some(status.Condition == 0)
      case _            => None
    }
  }

  def multiplierOfStatus(statusName: String, defaultMultiplier: Float = 1f): Float = {
    sys.statuses.indexWhere { _.name.contains(statusName) } match {
      case -1 =>
        defaultMultiplier
      case n  =>
        val status = sys.statuses(n)
        if (Jammed && status.jammable) {
          status.effects(status.jamState).getMultiplier()
        } else {
          status.effects(damageStates(n).Condition).getMultiplier()
        }
    }
  }

  def jammerMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = {
    val statuses = sys.statuses
    val sysIndices = if (activeJammedMsgs.isEmpty) {
      val indexed = statuses.indices.toList
      //find the highest priority amongst the damaged status conditions
      //if no damage, default to lowest priority - 0
      val highestPriorityDamagedStatus = indexed
        .filter { i => damageStates(i).Condition > 0 }
        .maxByOption { i => statuses(i).priority } match {
        case Some(i) => statuses(i).priority
        case None    => 0
      }
      //find all jammable statuses with priority equal to or greater than the highest priority
      //ignore all statuses where its current damage state is higher than its jam state (jamming does nothing)
      //turn the resulting statuses into packets
      val indices = indexed
        .filter { i =>
          val status = statuses(i)
          status.jammable &&
          status.priority >= highestPriorityDamagedStatus &&
          status.jamState > damageStates(i).Condition
        }
      activeJammedMsgs = indices
      indices
    } else {
      activeJammedMsgs
    }
    val msgs = sysIndices.flatMap { i => statuses(i).jammerMessages(toState = -1, vehicle) }
    activeJammedMsgs = sysIndices
    Jammed = msgs.nonEmpty
    msgs
  }

  def clearJammerMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = {
    if (Jammed) {
      Jammed = false
      val statuses = sys.statuses
      val clearMsgs = activeJammedMsgs.flatMap { i => statuses(i).clearJammerMessages(damageStates(i).Condition, vehicle) }
      activeJammedMsgs = Nil
      changedWhilejammed = Array.emptyIntArray
      clearMsgs
    } else {
      Nil
    }
  }

  /**
    * Produce packets that are tailored to the current active situation of the subsystem.
    * When the subsystem is jammed, report packets that reflect the jammed conditions.
    * When not jammed, report any condition that is not neutral / normal.
    * @param vehicle the vehicle in which the subsystem module is operating
    * @return game packets that reflect the condition
    */
  def getMessage(vehicle: Vehicle): List[PlanetSideGamePacket] = {
    if (Jammed) {
      jammerMessages(vehicle)
    } else {
      damageStates
        .zipWithIndex
        .collect { case (state, index) if state.Condition > 0 => sys.statuses(index).getMessage(state.Condition, vehicle) }
        .flatten
        .toList
    }
  }

  /**
    * Regardless of meta-conditions surrounding the subsystem,
    * always try to produce packets that report the current situation of the subsystem.
    * May return a condition status "update" that does not actually change anything.
    * @param vehicle the vehicle in which the subsystem module is operating
    * @return game packets that reflect the condition
    */
  def currentMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = {
    toPacketList(damageStates.zipWithIndex, vehicle)
  }

  def changedMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = {
    toPacketList(damageStates.zipWithIndex.filter { case (status, _) => status.wasChanged }, vehicle, always = true)
  }

  def specificStatusMessage(name: String, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    damageStates.zipWithIndex.find { case (status, _) => status.status.name.contains(name) } match {
      case Some(pair) =>
        toPacketList(List(pair), vehicle)
      case _ =>
        Nil
    }
  }

  private def toPacketList(
                            list: Iterable[(VehicleSubsystemStatusMonitor, Int)],
                            vehicle: Vehicle,
                            always: Boolean = false
                          ): List[PlanetSideGamePacket] = {
    list.flatMap { case (state: VehicleSubsystemStatusMonitor, index) =>
      if (Jammed && state.status.jammable) {
        sys.statuses(index).getMessage(state.status.jamState, vehicle)
      } else if (state.Condition > 0 || always) {
        sys.statuses(index).getMessage(state.Condition, vehicle)
      } else {
        Nil
      }
    }
    .toList
  }
}

class VehicleSubsystemStatusMonitor(
                                     val status: VehicleSubsystemStatus,
                                     val index: Int
                                   ) {
  private var condition: Int = 0
  private var changed: Boolean = false

  def Condition: Int = condition

  def Condition_=(state: Int): Int = {
    changed = state != condition
    condition = state
    Condition
  }

  def jam(): Unit = {
    changed = status.jammable
  }

  def Changed: Boolean = changed

  /**
    * A kind of temporary meta-filter.
    * If state change occurred to this subsystem status's condition, the flag will be set.
    * When polled like this, the current state of the flag will be tested and returned,
    * but the flag will then be cleared too.
    * Subsequent tests should not observe this flag until another condition change.
    * @return whether or not a state changed since the last time this status was tested for a state change
    */
  def wasChanged: Boolean = {
    val originalValue = changed
    changed = false
    originalValue
  }
}
