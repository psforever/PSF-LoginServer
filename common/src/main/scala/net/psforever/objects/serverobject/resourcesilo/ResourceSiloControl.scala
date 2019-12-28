// Copyright (c) 2019 PSForever
package net.psforever.objects.serverobject.resourcesilo

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.types.PlanetSideEmpire
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `Resource Silo`.
  * @param resourceSilo the `Resource Silo` object being governed
  */
class ResourceSiloControl(resourceSilo : ResourceSilo) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = resourceSilo
  private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case "startup" =>
      // todo: This is just a temporary solution to drain NTU over time. When base object destruction is properly implemented NTU should be deducted when base objects repair themselves
      context.system.scheduler.schedule(5 second, 5 second, self, ResourceSilo.UpdateChargeLevel(-1))
      context.become(Processing)

    case _ => ;
  }

  def Processing : Receive = checkBehavior.orElse {
    case ResourceSilo.Use(player, msg) =>
      sender ! ResourceSilo.ResourceSiloMessage(player, msg, resourceSilo.Use(player, msg))
    case ResourceSilo.LowNtuWarning(enabled: Boolean) =>
      resourceSilo.LowNtuWarningOn = enabled
      log.trace(s"LowNtuWarning: Silo ${resourceSilo.GUID} low ntu warning set to $enabled")
      val building = resourceSilo.Owner
      val zone = building.Zone
      building.Zone.AvatarEvents ! AvatarServiceMessage(
        zone.Id,
        AvatarAction.PlanetsideAttribute(building.GUID, 47, if(resourceSilo.LowNtuWarningOn) 1 else 0)
      )

    case ResourceSilo.UpdateChargeLevel(amount: Int) =>
      val siloChargeBeforeChange = resourceSilo.ChargeLevel
      val building = resourceSilo.Owner.asInstanceOf[Building]
      val zone = building.Zone

      // Increase if positive passed in or decrease charge level if negative number is passed in
      resourceSilo.ChargeLevel += amount
      if(resourceSilo.ChargeLevel > 0) {
        log.trace(s"UpdateChargeLevel: Silo ${resourceSilo.GUID} set to ${resourceSilo.ChargeLevel}")
      }

      val ntuBarLevel = scala.math.ceil((resourceSilo.ChargeLevel.toFloat / resourceSilo.MaximumCharge.toFloat) * 10).toInt
      // Only send updated capacitor display value to all clients if it has actually changed
      if(resourceSilo.CapacitorDisplay != ntuBarLevel) {
        log.trace(s"Silo ${resourceSilo.GUID} NTU bar level has changed from ${resourceSilo.CapacitorDisplay} to $ntuBarLevel")
        resourceSilo.CapacitorDisplay = ntuBarLevel
        resourceSilo.Owner.Actor ! Building.SendMapUpdate(all_clients = true)
        zone.AvatarEvents ! AvatarServiceMessage(
          zone.Id,
          AvatarAction.PlanetsideAttribute(resourceSilo.GUID, 45, resourceSilo.CapacitorDisplay)
        )
        building.Actor ! Building.SendMapUpdate(all_clients = true)
      }

      val ntuIsLow = resourceSilo.ChargeLevel.toFloat / resourceSilo.MaximumCharge.toFloat < 0.2f
      if(resourceSilo.LowNtuWarningOn && !ntuIsLow){
          self ! ResourceSilo.LowNtuWarning(enabled = false)
        } else if (!resourceSilo.LowNtuWarningOn && ntuIsLow) {
        self ! ResourceSilo.LowNtuWarning(enabled = true)
      }

      if(resourceSilo.ChargeLevel == 0 && siloChargeBeforeChange > 0) {
        // Oops, someone let the base run out of power. Shut it all down.
        zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttribute(building.GUID, 48, 1))
        building.Faction = PlanetSideEmpire.NEUTRAL
        zone.LocalEvents ! LocalServiceMessage(zone.Id, LocalAction.SetEmpire(building.GUID, PlanetSideEmpire.NEUTRAL))
        building.TriggerZoneMapUpdate()
      } else if (siloChargeBeforeChange == 0 && resourceSilo.ChargeLevel > 0) {
        // Power restored. Reactor Online. Sensors Online. Weapons Online. All systems nominal.
        //todo: Check generator is online before starting up
        zone.AvatarEvents ! AvatarServiceMessage(
          zone.Id,
          AvatarAction.PlanetsideAttribute(building.GUID, 48, 0)
        )
        building.TriggerZoneMapUpdate()
      }
    case _ => ;
  }
}
