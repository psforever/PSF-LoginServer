// Copyright (c) 2019 PSForever
package net.psforever.objects.serverobject.resourcesilo

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.transfer.TransferBehavior
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.{GlobalDefinitions, Ntu, NtuContainer, NtuStorageBehavior}
import net.psforever.types.PlanetSideEmpire
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `Resource Silo`.
  *
  * @param resourceSilo the `Resource Silo` object being governed
  */
class ResourceSiloControl(resourceSilo: ResourceSilo)
    extends Actor
    with FactionAffinityBehavior.Check
    with NtuStorageBehavior {
  def FactionObject: FactionAffinity = resourceSilo

  private[this] val log               = org.log4s.getLogger
  var panelAnimationFunc: (ActorRef, Float) => Unit = PanelAnimation

  def receive: Receive = {
    case Service.Startup() =>
      resourceSilo.Owner match {
        case building: Building =>
          UpdateChargeLevel(amount = 0)
          building.Actor ! (if (resourceSilo.NtuCapacitor <= 0f ) {
            BuildingActor.NtuDepleted()
          } else {
            BuildingActor.SuppliedWithNtu()
          })
        case _ => ;
      }
      context.become(Processing)

    case _ => ;
  }

  def Processing: Receive =
    checkBehavior
      .orElse(storageBehavior)
      .orElse {
        case CommonMessages.Use(player, _) =>
          val siloFaction = resourceSilo.Faction
          val playerFaction = player.Faction
          resourceSilo.Zone.Vehicles.find(v => v.PassengerInSeat(player).contains(0)) match {
            case Some(vehicle) =>
              (if (GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition)) {
                //bfr's discharge into friendly silos and charge from enemy and neutral silos
                if (siloFaction == playerFaction) {
                  Some(TransferBehavior.Discharging(Ntu.Nanites))
                } else if (resourceSilo.MaxNtuCapacitor * 0.4f < resourceSilo.NtuCapacitor) {
                  //the bfr never drains below 40%
                  Some(TransferBehavior.Charging(Ntu.Nanites))
                } else {
                  None
                }
              } else if(siloFaction == PlanetSideEmpire.NEUTRAL || siloFaction == playerFaction) {
                //ants discharge into neutral and friendly silos
                Some(TransferBehavior.Discharging(Ntu.Nanites))
              } else {
                None
              }) match {
                case Some(msg) =>
                  context.system.scheduler.scheduleOnce(
                    delay = 1000 milliseconds,
                    vehicle.Actor,
                    msg
                  )
                case None => ;
              }
            case _ => ;
          }

        case ResourceSilo.LowNtuWarning(enabled: Boolean) =>
          LowNtuWarning(enabled)

        case ResourceSilo.UpdateChargeLevel(amount: Float) =>
          UpdateChargeLevel(amount)

        case _ => ;
      }

  def LowNtuWarning(enabled: Boolean): Unit = {
    resourceSilo.LowNtuWarningOn = enabled
    log.trace(s"LowNtuWarning: Silo ${resourceSilo.GUID} low ntu warning set to $enabled")
    val building = resourceSilo.Owner
    val zone     = building.Zone
    building.Zone.AvatarEvents ! AvatarServiceMessage(
      zone.id,
      AvatarAction.PlanetsideAttribute(building.GUID, 47, if (resourceSilo.LowNtuWarningOn) 1 else 0)
    )
  }

  def UpdateChargeLevel(amount: Float): Unit = {
    val siloChargeBeforeChange  = resourceSilo.NtuCapacitor
    val siloDisplayBeforeChange = resourceSilo.CapacitorDisplay
    val building                = resourceSilo.Owner.asInstanceOf[Building]
    val zone                    = building.Zone

    // Increase if positive passed in or decrease charge level if negative number is passed in
    resourceSilo.NtuCapacitor += amount
    if (resourceSilo.NtuCapacitor > 0) {
      log.trace(s"UpdateChargeLevel: Silo ${resourceSilo.GUID} set to ${resourceSilo.NtuCapacitor}")
    }

    // Only send updated capacitor display value to all clients if it has actually changed
    if (resourceSilo.CapacitorDisplay != siloDisplayBeforeChange) {
      log.trace(
        s"UpdateChargeLevel: silo ${resourceSilo.GUID} NTU bar level has changed from $siloDisplayBeforeChange to ${resourceSilo.CapacitorDisplay}"
      )
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.PlanetsideAttribute(resourceSilo.GUID, 45, resourceSilo.CapacitorDisplay)
      )
      building.Actor ! BuildingActor.MapUpdate()
    }
    val ntuIsLow = resourceSilo.NtuCapacitor / resourceSilo.Definition.MaxNtuCapacitor < 0.2f
    if (resourceSilo.LowNtuWarningOn && !ntuIsLow) {
      LowNtuWarning(enabled = false)
    } else if (!resourceSilo.LowNtuWarningOn && ntuIsLow) {
      LowNtuWarning(enabled = true)
    }
    if (resourceSilo.NtuCapacitor == 0 && siloChargeBeforeChange > 0) {
      building.Actor ! BuildingActor.NtuDepleted()
      building.Actor ! BuildingActor.AmenityStateChange(resourceSilo)
    } else if (siloChargeBeforeChange == 0 && resourceSilo.NtuCapacitor > 0) {
      building.Actor ! BuildingActor.SuppliedWithNtu()
      building.Actor ! BuildingActor.AmenityStateChange(resourceSilo)
    }
  }

  /**
    * The silo will agree to offers until its nanite capacitor is completely full.
    */
  def HandleNtuOffer(sender: ActorRef, src: NtuContainer): Unit = {
    sender ! (if (resourceSilo.NtuCapacitor < resourceSilo.MaxNtuCapacitor) {
      Ntu.Request(0, resourceSilo.MaxNtuCapacitor - resourceSilo.NtuCapacitor)
    } else {
      StopNtuBehavior(sender)
      Ntu.Request(0, 0)
    })
  }

  /**
    * Reset the animation trigger and attempt the stop animation.
    */
  def StopNtuBehavior(sender: ActorRef): Unit = {
    panelAnimationFunc = PanelAnimation
    panelAnimationFunc(sender, 0)
  }

  /**
    * na
    *
    * @param sender na
    * @param min    a minimum amount of nanites requested;
    * @param max    the amount of nanites required to not make further requests;
    */
  def HandleNtuRequest(sender: ActorRef, min: Float, max: Float): Unit = {
    val originalAmount = resourceSilo.NtuCapacitor
    UpdateChargeLevel(-min)
    sender ! Ntu.Grant(resourceSilo, originalAmount - resourceSilo.NtuCapacitor)
  }

  /**
    * Accept nanites into the silo capacitor and set the animation state.
    */
  def HandleNtuGrant(sender: ActorRef, src: NtuContainer, amount: Float): Unit = {
    if (amount != 0) {
      panelAnimationFunc(sender, amount)
      panelAnimationFunc = SkipPanelAnimation
    }
  }

  /**
    * When charging from another source of nanites, the silo's panels will glow
    * and a particle affect will traverse towards the panels from about ten meters in front of the silo.
    * These effects are both controlled by thee same packet.
    *
    * @param trigger if positive, activate the animation;
    *                if negative or zero, disable the animation
    */
  def PanelAnimation(source: ActorRef, trigger: Float): Unit = {
    val currentlyHas = resourceSilo.NtuCapacitor
    // do not let the trigger charge go to waste, but also do not let the silo be filled
    // attempting to return it to the source may sabotage an ongoing transfer process
    val amount = (if (trigger > 0) {
      // panel glow & orb particles on
      val zone = resourceSilo.Zone
      zone.VehicleEvents ! VehicleServiceMessage(
        zone.id,
        VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, resourceSilo.GUID, 49, 1)
      )
      math.min(resourceSilo.MaxNtuCapacitor - currentlyHas, trigger)
    } else if (trigger < 0) {
      // no change to animation state
      if (currentlyHas > -trigger) { trigger } else { -currentlyHas }
    } else {
      // panel glow & orb particles off
      val zone = resourceSilo.Zone
      zone.VehicleEvents ! VehicleServiceMessage(
        zone.id,
        VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, resourceSilo.GUID, 49, 0)
      )
      0
    }) * 0.9f
    UpdateChargeLevel(amount)
  }

  /**
    * Update the charge level and decide if the silo is full.
    * Announce that full-ness to the NTU source.
    * Although called "Skip", an animation that broadcasts the transfer process should be ongoing at the moment.
    */
  def SkipPanelAnimation(source: ActorRef, trigger: Float): Unit = {
    UpdateChargeLevel(trigger)
    // immediate termination of ntu requests
    if (resourceSilo.NtuCapacitor == resourceSilo.MaxNtuCapacitor) {
      source ! Ntu.Request(0, 0)
    }
  }
}
