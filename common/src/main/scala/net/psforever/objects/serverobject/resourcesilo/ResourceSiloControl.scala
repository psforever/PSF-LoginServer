// Copyright (c) 2019 PSForever
package net.psforever.objects.serverobject.resourcesilo

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.vehicles.{Ntu, NtuBehavior}
import net.psforever.types.PlanetSideEmpire
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `Resource Silo`.
  * @param resourceSilo the `Resource Silo` object being governed
  */
class ResourceSiloControl(resourceSilo: ResourceSilo) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject: FactionAffinity = resourceSilo
  private[this] val log              = org.log4s.getLogger

  def receive: Receive = {
    case "startup" =>
      // todo: This is just a temporary solution to drain NTU over time. When base object destruction is properly implemented NTU should be deducted when base objects repair themselves
//      context.system.scheduler.schedule(5 second, 5 second, self, ResourceSilo.UpdateChargeLevel(-1))
      context.become(Processing)

    case _ => ;
  }

  def Processing: Receive =
    checkBehavior.orElse {
    case ResourceSilo.Use(player, _) =>
      if(resourceSilo.Faction == PlanetSideEmpire.NEUTRAL || player.Faction == resourceSilo.Faction) {
        resourceSilo.Zone.Vehicles.find(v => v.PassengerInSeat(player).contains(0)) match {
          case Some(vehicle) =>
            context.system.scheduler.scheduleOnce(delay = 1000 milliseconds, vehicle.Actor, NtuBehavior.Discharging())
          case _ =>
        }
      }

      case ResourceSilo.LowNtuWarning(enabled: Boolean) =>
        resourceSilo.LowNtuWarningOn = enabled
        log.trace(s"LowNtuWarning: Silo ${resourceSilo.GUID} low ntu warning set to $enabled")
        val building = resourceSilo.Owner
        val zone     = building.Zone
        building.Zone.AvatarEvents ! AvatarServiceMessage(
          zone.Id,
          AvatarAction.PlanetsideAttribute(building.GUID, 47, if (resourceSilo.LowNtuWarningOn) 1 else 0)
        )

    case ResourceSilo.UpdateChargeLevel(amount: Int) =>
      UpdateChargeLevel(amount)

    case Ntu.Offer() =>
      sender ! (if(resourceSilo.ChargeLevel < resourceSilo.MaximumCharge) {
        Ntu.Request(0, resourceSilo.MaximumCharge - resourceSilo.ChargeLevel)
      }
      else {
        UpdateChargeLevel(amount = 0)
        Ntu.Request(0, 0)
      })

    case Ntu.Request(min, _) =>
      val originalAmount = resourceSilo.ChargeLevel
      UpdateChargeLevel(-min)
      sender ! Ntu.Grant(originalAmount - resourceSilo.ChargeLevel)

    case Ntu.Grant(amount) =>
      UpdateChargeLevel(amount)

    case _ => ;
  }

  def UpdateChargeLevel(amount: Int) : Unit = {
    val siloChargeBeforeChange = resourceSilo.ChargeLevel
    val siloDisplayBeforeChange = resourceSilo.CapacitorDisplay
    val building = resourceSilo.Owner.asInstanceOf[Building]
    val zone = building.Zone

    // Increase if positive passed in or decrease charge level if negative number is passed in
    resourceSilo.ChargeLevel += amount
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.Id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, resourceSilo.GUID, 49, if(amount != 0) 1 else 0)
    ) // panel glow on & orb particles on
    if (resourceSilo.ChargeLevel > 0) {
      log.trace(s"UpdateChargeLevel: Silo ${resourceSilo.GUID} set to ${resourceSilo.ChargeLevel}")
    }

    // Only send updated capacitor display value to all clients if it has actually changed
    if (resourceSilo.CapacitorDisplay != siloDisplayBeforeChange) {
      log.trace(s"Silo ${resourceSilo.GUID} NTU bar level has changed from $siloDisplayBeforeChange to ${resourceSilo.CapacitorDisplay}")
      resourceSilo.Owner.Actor ! Building.SendMapUpdate(all_clients = true)
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.Id,
        AvatarAction.PlanetsideAttribute(resourceSilo.GUID, 45, resourceSilo.CapacitorDisplay)
      )
      building.Actor ! Building.SendMapUpdate(all_clients = true)
    }
    val ntuIsLow = resourceSilo.ChargeLevel.toFloat / resourceSilo.MaximumCharge.toFloat < 0.2f
    if (resourceSilo.LowNtuWarningOn && !ntuIsLow) {
      self ! ResourceSilo.LowNtuWarning(enabled = false)
    }
    else if (!resourceSilo.LowNtuWarningOn && ntuIsLow) {
      self ! ResourceSilo.LowNtuWarning(enabled = true)
    }
    if (resourceSilo.ChargeLevel == 0 && siloChargeBeforeChange > 0) {
      // Oops, someone let the base run out of power. Shut it all down.
      zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttribute(building.GUID, 48, 1))
      building.Faction = PlanetSideEmpire.NEUTRAL
      zone.LocalEvents ! LocalServiceMessage(zone.Id, LocalAction.SetEmpire(building.GUID, PlanetSideEmpire.NEUTRAL))
      building.TriggerZoneMapUpdate()
    }
    else if (siloChargeBeforeChange == 0 && resourceSilo.ChargeLevel > 0) {
      // Power restored. Reactor Online. Sensors Online. Weapons Online. All systems nominal.
      //todo: Check generator is online before starting up
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.Id,
        AvatarAction.PlanetsideAttribute(building.GUID, 48, 0)
      )
      building.TriggerZoneMapUpdate()
    }
  }
}
