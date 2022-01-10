// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import enumeratum.values.{StringEnum, StringEnumEntry}
import net.psforever.objects.Vehicle
import net.psforever.objects.equipment.JammableUnit
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ComponentDamageField, ComponentDamageMessage, GenericObjectActionMessage}
import net.psforever.types.PlanetSideGUID

trait VehicleSubsystemVulnerability {
  def damageable: Boolean
  def jammable: Boolean
}

trait VehicleSubsystemMessage {
  def getMessage(id:Long, vehicle: Vehicle): List[PlanetSideGamePacket]
}

trait VehicleSubsystemStatusEffect extends VehicleSubsystemVulnerability {
  def state: String
  def priority: Int
  def jamState: Int
  def jammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket]
  def clearJammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket]
}

trait VehicleSubsystemVariableEffectState {
  def getMessage(id:Long, vehicle: Vehicle): List[PlanetSideGamePacket]
}

sealed abstract class VehicleSubsystemEntry2(
                                              val value: String,
                                              val statuses: List[VehicleSubsystemStatusEffect],
                                              val startsEnabled: Boolean,
                                              val enabledStatus: List[String]
                                            ) extends StringEnumEntry /*with VehicleSubsystemVulnerability*/ {
  def this(value: String, statuses: List[VehicleSubsystemStatusEffect]) =
    this(value, statuses, startsEnabled = true, enabledStatus = Nil)

  def damageable: Boolean = statuses.exists { _.damageable }

  def jammable: Boolean = statuses.exists { _.jammable }

  def getMessageGuid(vehicle: Vehicle): PlanetSideGUID = {
    vehicle.GUID
  }
}

final case class VehicleComponentEffects(
                                          state: String,
                                          componentId: Long,
                                          states: List[VehicleSubsystemVariableEffectState],
                                          damageable: Boolean,
                                          jamState: Int,
                                          priority: Int = 0
                                        ) extends VehicleSubsystemStatusEffect {
  def jammable: Boolean = jamState > 0

  def jammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    if (toState < jamState) {
      states(jamState).getMessage(componentId, vehicle)
    } else {
      Nil
    }
  }

  def clearJammerMessages(toState: Int, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    states(toState).getMessage(componentId, vehicle)
  }
}

object VehicleComponentEffects {
  def apply(state: String, cid: Long, states: List[VehicleSubsystemVariableEffectState]): VehicleComponentEffects =
    VehicleComponentEffects(state, cid, states, damageable = true, jamState = 0)
}

final case class VehicleComponentProgressiveCondition(
                                                          unk2: Long,
                                                          unk3: Long,
                                                          unk4: Boolean
                                                        ) extends VehicleSubsystemVariableEffectState {
  def getMessage(id:Long, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    List(ComponentDamageMessage(vehicle.GUID, id, Some(ComponentDamageField(unk2, unk3, unk4))))
  }
}

sealed abstract class BattleframeShieldGeneratorCondition(code: Int) extends VehicleSubsystemVariableEffectState {
  def getMessage(id: Long, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    List(GenericObjectActionMessage(vehicle.GUID, code))
  }
}

sealed abstract class BattleframeArmMountCondition(code: Int) extends VehicleSubsystemVariableEffectState {
  def getMessage(id: Long, vehicle: Vehicle): List[PlanetSideGamePacket] = {
    val guid = vehicle.Weapons(id.toInt).Equipment match {
      case Some(o) => o.GUID
      case _       => PlanetSideGUID(0)
    }
    List(GenericObjectActionMessage(guid, code))
  }
}

object VehicleSubsystemEntry2 extends StringEnum[VehicleSubsystemEntry2] {
  val values = findValues

  private case object PlaceholderNormalReplaceLater extends VehicleSubsystemVariableEffectState {
    def getMessage(id: Long, vehicle: Vehicle): List[PlanetSideGamePacket] = Nil
  }

  private case object VehicleComponentNormal extends VehicleSubsystemVariableEffectState {
    def getMessage(id: Long, vehicle: Vehicle): List[PlanetSideGamePacket] = {
      List(ComponentDamageMessage(vehicle.GUID, id, None))
    }
  }

  private case object BattleframeShieldGeneratorOnline extends BattleframeShieldGeneratorCondition(code = 44)

  private case object BattleframeShieldGeneratorOffline extends BattleframeShieldGeneratorCondition(code = 45)

  private case object BattleframeArmActive extends BattleframeArmMountCondition(code = 38)

  private case object BattleframeArmInactive extends BattleframeArmMountCondition(code = 39)

  private val bfrArmConditions = List(BattleframeArmActive, BattleframeArmInactive)

  protected abstract class BattleframeArmMount(override val value: String, slot: Int) extends VehicleSubsystemEntry2(
    value,
    statuses = List(
      VehicleComponentEffects(
        "Toggle",
        slot,
        bfrArmConditions,
        damageable = false,
        jamState = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = List("Toggle")
  )

  case object Controls extends VehicleSubsystemEntry2(
    value = "Controls",
    statuses = List(
      VehicleComponentEffects(
        "Impaired",
        0,
        List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
        damageable = false,
        jamState = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = List()
  )

  case object Ejection extends VehicleSubsystemEntry2(
    value = "Ejection",
    statuses = List(
      VehicleComponentEffects(
        "Offline",
        0,
        List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
        damageable = true,
        jamState = 1
      )
    ),
    startsEnabled = true,
    enabledStatus = List()
  )

  case object MosquitoRadar extends VehicleSubsystemEntry2(
    value = "MosquitoRadar",
    statuses = List(
      VehicleComponentEffects(
        "Offline",
        0,
        List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
        damageable = false,
        jamState = 1
      )
    ),
    startsEnabled = true,
    List()
  )

  case object BattleFrameMovementServos extends VehicleSubsystemEntry2(
    value = "BattleFrameMovementServos",
    statuses = List(
      VehicleComponentEffects(
        "Transit",
        9,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(5, 1065353216, unk4 = true) //NoTransit
        ),
        damageable = true,
        jamState = 1
      ),
      VehicleComponentEffects(
        "Backward",
        10,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(3, 1061997772, unk4 = true), //-20%
          VehicleComponentProgressiveCondition(4, 1058642329, unk4 = true) //-40%
        ),
        damageable = true,
        jamState = 1
      ),
      VehicleComponentEffects(
        "Forward",
        11,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(3, 1061997772, unk4 = true), //-20%
          VehicleComponentProgressiveCondition(4, 1058642329, unk4 = true) //-40%, EXPERIMENTAL
        ),
        damageable = true,
        jamState = 1
      ),
      VehicleComponentEffects(
        "PivotSpeed",
        12,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(5, 1053609164, unk4 = true) //-60%
        ),
        damageable = true,
        jamState = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = Nil
  )

  case object BattleframeSensorArray extends VehicleSubsystemEntry2(
    value = "BattleframeSensorArray",
    statuses = List(
      VehicleComponentEffects(
        "NoEnemies",
        17,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(5, 1065353216, unk4 = true)
        ),
        damageable = true,
        jamState = 1
      ),
      VehicleComponentEffects(
        "NoEnemyAircraft",
        18,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(4, 1065353216, unk4 = true)
        ),
        damageable = true,
        jamState = 1
      ),
      VehicleComponentEffects(
        "NoEnemyGroundVehicles",
        19,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(4, 1065353216, unk4 = true)
        ),
        damageable = true,
        jamState = 1
      ),
      VehicleComponentEffects(
        "NoEnemyProjectiles",
        20,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(4, 1065353216, unk4 = true)
        ),
        damageable = true,
        jamState = 1
      ),
      VehicleComponentEffects(
        "SensorRange",
        21,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(3, 1061158912, unk4 = true), //-25
          VehicleComponentProgressiveCondition(4, 1056964608, unk4 = true) //-50
        ),
        damageable = true,
        jamState = 2
      )
    ),
    startsEnabled = true,
    enabledStatus = Nil
  )

  case object BattleFrameFlightPod extends VehicleSubsystemEntry2(
    value = "BattleFrameFlightPod",
    statuses = List(
      VehicleComponentEffects(
        "FlightRecharge",
        3,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(3, 1061997772, unk4 = true), //-20%
          VehicleComponentProgressiveCondition(4, 1058642329, unk4 = true) //-40%
        )
      ),
      VehicleComponentEffects(
        "UseRate",
        4,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(5, 1077936128, unk4 = true), //200%
        )
      ),
      VehicleComponentEffects(
        "Horizontal",
        6,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(3, 1061997772, unk4 = true), //-20%, EXPERIMENTAL
          VehicleComponentProgressiveCondition(4, 1058642329, unk4 = true), //-40%
        )
      ),
      VehicleComponentEffects(
        "Vertical",
        8,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(3, 1061997772, unk4 = true), //-20%
          VehicleComponentProgressiveCondition(4, 1058642329, unk4 = true), //-40%, EXPERIMENTAL
        )
      ),
      VehicleComponentEffects(
        "Offline",
        7,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(4, 1065353216, unk4 = false)
        ),
        damageable = false,
        jamState = 1,
        priority = 1
      ),
      VehicleComponentEffects(
        "Destroyed",
        5,
        List(
          VehicleComponentNormal,
          VehicleComponentProgressiveCondition(7, 1065353216, unk4 = false),
        ),
        damageable = true,
        jamState = 0,
        priority = 2
      )
    ),
    startsEnabled = true,
    enabledStatus = List("Offline", "Destroyed")
  )

  case object BattleframeShieldGenerator extends VehicleSubsystemEntry2(
    value = "BattleFrameShieldGenerator",
    List(
      VehicleComponentEffects(
        "Offline",
        0,
        List(
          BattleframeShieldGeneratorOnline,
          BattleframeShieldGeneratorOffline
        ),
        damageable = false,
        jamState = 1
      )
    ),
    startsEnabled = true,
    enabledStatus = List("Offline")
  )

  case object BattleframeTrunk extends VehicleSubsystemEntry2(
    value = "BattleframeTrunk",
    statuses = List(
      VehicleComponentEffects(
        "Damaged",
        0,
        List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
        damageable = true,
        jamState = 0
      )
    ),
    startsEnabled = true,
    enabledStatus = Nil
  )

  case object BattleframeWeaponry extends VehicleSubsystemEntry2(
    value = "BattleframeWeaponry",
    statuses = List(
      VehicleComponentEffects(
        "Impaired",
        0,
        List(PlaceholderNormalReplaceLater,PlaceholderNormalReplaceLater),
        damageable = true,
        jamState = 1
      )
    ),
    startsEnabled = true,
    enabledStatus = Nil
  )

  case object BattleframeLeftArm extends BattleframeArmMount(value = "BattleframeLeftArm", slot = 2)

  case object BattleframeRightArm extends BattleframeArmMount(value = "BattleframeRightArm", slot = 3)

  case object BattleframeFlightLeftArm extends BattleframeArmMount(value = "BattleframeLeftArmF", slot = 1)

  case object BattleframeFlightRightArm extends BattleframeArmMount(value = "BattleframeRightArmF", slot = 2)
}

class VehicleSubsystem2(sys: VehicleSubsystemEntry2)
  extends JammableUnit {
  /** na */
  private val damageStates: Array[Int] = Array.fill[Int](sys.statuses.length)(elem = 0)
  /** na */
  private var activeJammedMsgs: List[Int] = Nil
  /** na */
  private var changedWhilejammed: Array[Int] = Array.emptyIntArray
  /** whether this subsystem is currently active or inactive */
  private var enabled: Boolean = sys.startsEnabled
  /** these statuses represent must be in good condition for the subsystem to be considered operational (enabled) */
  private val enabledStatusIndices = sys.enabledStatus.map {
    str => sys.statuses.indexOf { stat: VehicleSubsystemStatusEffect => stat.state.equals(str) }
  }
  //first enabled status condition defaults to a damaged state if the subsystem defaults to disabled
  if (!enabled && enabledStatusIndices.nonEmpty) {
    damageStates(enabledStatusIndices.head) = 1
  }

  def Enabled: Boolean = !Jammed && (if (enabledStatusIndices.nonEmpty) {
    enabledStatusIndices.forall { damageStates(_) == 0 }
  } else {
    enabled
  })

  def Enabled_=(state: Boolean): Boolean = {
    if (enabledStatusIndices.nonEmpty) {
      val stateAsInt = if (state) { 1 } else { 0 }
      if ((stateAsInt == 1 && damageStates(enabledStatusIndices.head) == 0) ||
          (stateAsInt == 0 && damageStates(enabledStatusIndices.head) > 0)) {
        damageStates(enabledStatusIndices.head) = stateAsInt
      }
    }
    enabled = state
    Enabled
  }

  def jammerMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = {
    val statuses = sys.statuses
    val indexed = statuses.indices.toList
    //find the highest priority amongst the damaged status conditions
    val highestPriorityDamagedStatus = indexed
      .filter { i => damageStates(i) > 0 }
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
        status.jamState > damageStates(i)
      }
    val msgs = indices.flatMap { i => statuses(i).jammerMessages(toState = -1, vehicle) }
    activeJammedMsgs = indices
    Jammed = msgs.nonEmpty
    msgs
  }

  def clearJammerMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = {
    //TODO can we downgrade directly, or do we have to cancel and then reinitialize to return proper conditions?
    if (Jammed) {
      Jammed = false
      val statuses = sys.statuses
      val clearMsgs = activeJammedMsgs.flatMap { i => statuses(i).clearJammerMessages(toState = -1, vehicle) }
      activeJammedMsgs = Nil
      changedWhilejammed = Array.emptyIntArray
      clearMsgs
    } else {
      Nil
    }
  }

  def currentMessages(vehicle: Vehicle): List[PlanetSideGamePacket] = {
    if (Jammed) {
      jammerMessages(vehicle)
    } else {
      Nil
    }
  }
}
