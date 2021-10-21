// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{TriggerEffectMessage, TriggeredEffectLocation}
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.PlanetSideGUID

/**
  * A vehicle control agency exclusive to the armored personnel carrier (APC) ground transport vehicles.
  * These vehicles include the Juggernaut (`apc_tr`), the Vindicator (`apc_nc`), the and Leviathan (`apc_vs`).
  * A completely faction-neutral transport in the same sytle (`apc`) does exist but is unused.
  * A common characteristic of this type of vehicle is the ability to discharge a defensive wide-area electromagnetic pulse.
  * @param vehicle the APC
  */
class ApcControl(vehicle: Vehicle)
  extends VehicleControl(vehicle)
  with VehicleCapacitance {
  def CapacitanceObject: Vehicle = vehicle

  override def postStop() : Unit = {
    super.postStop()
    capacitancePostStop()
  }

  override def commonEnabledBehavior : Receive = super.commonEnabledBehavior
    .orElse(capacitorBehavior)
    .orElse {
      case SpecialEmp.Burst() =>
        performEmpBurst()

      case _ => ;
    }

  def performEmpBurst(): Unit = {
    val obj = CapacitanceObject
    if (obj.Capacitor == obj.Definition.MaxCapacitor) { //only if the capacitor is full
      val zone = obj.Zone
      val events = zone.VehicleEvents
      val pos = obj.Position
      val GUID0 = Service.defaultPlayerGUID
      val emp = obj.Definition.innateDamage.getOrElse { SpecialEmp.emp }
      val faction = obj.Faction
      //drain the capacitor
      obj.Capacitor = 0
      events ! VehicleServiceMessage(
        self.toString(),
        VehicleAction.PlanetsideAttribute(GUID0, obj.GUID, 113, 0)
      )
      //cause the emp
      events ! VehicleServiceMessage(
        zone.id,
        VehicleAction.SendResponse(
          GUID0,
          TriggerEffectMessage(
            GUID0,
            s"apc_explosion_emp_${faction.toString.toLowerCase}",
            None,
            Some(TriggeredEffectLocation(pos, obj.Orientation))
          )
        )
      )
      //resolve what targets are affected by the emp
      Zone.serverSideDamage(
        zone,
        obj,
        emp,
        SpecialEmp.createEmpInteraction(emp, pos),
        ExplosiveDeployableControl.detectionForExplosiveSource(obj),
        Zone.findAllTargets
      )
      //start charging again
      startCapacitorTimer()
    }
  }

  override def PrepareForDisabled(kickPassengers: Boolean) : Unit = {
    super.PrepareForDisabled(kickPassengers)
    capacitanceStop()
  }

  override protected def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    capacitancePostStop()
  }

  override def parseObjectAction(guid: PlanetSideGUID, action: Int, other: Option[Any]): Unit = {
    super.parseObjectAction(guid, action, other)
    if (action == 55) {
      performEmpBurst()
    }
  }
}
