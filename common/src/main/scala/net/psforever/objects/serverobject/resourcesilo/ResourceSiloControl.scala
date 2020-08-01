// Copyright (c) 2019 PSForever
package net.psforever.objects.serverobject.resourcesilo

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.transfer.TransferBehavior
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.{Ntu, NtuContainer, NtuStorageBehavior}
import net.psforever.types.PlanetSideEmpire
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

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
  var panelAnimationFunc: Int => Unit = PanelAnimation

  def receive: Receive = {
    case "startup" =>
      // todo: This is just a temporary solution to drain NTU over time. When base object destruction is properly implemented NTU should be deducted when base objects repair themselves
      //      context.system.scheduler.schedule(5 second, 5 second, self, ResourceSilo.UpdateChargeLevel(-1))
      context.become(Processing)

    case _ => ;
  }

  def Processing: Receive =
    checkBehavior
      .orElse(storageBehavior)
      .orElse {
        case CommonMessages.Use(player, _) =>
          if (resourceSilo.Faction == PlanetSideEmpire.NEUTRAL || player.Faction == resourceSilo.Faction) {
            resourceSilo.Zone.Vehicles.find(v => v.PassengerInSeat(player).contains(0)) match {
              case Some(vehicle) =>
                context.system.scheduler.scheduleOnce(
                  delay = 1000 milliseconds,
                  vehicle.Actor,
                  TransferBehavior.Discharging(Ntu.Nanites)
                )
              case _ =>
            }
          }

        case ResourceSilo.LowNtuWarning(enabled: Boolean) =>
          LowNtuWarning(enabled)

        case ResourceSilo.UpdateChargeLevel(amount: Int) =>
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

  def UpdateChargeLevel(amount: Int): Unit = {
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
        s"Silo ${resourceSilo.GUID} NTU bar level has changed from $siloDisplayBeforeChange to ${resourceSilo.CapacitorDisplay}"
      )
      resourceSilo.Owner.Actor ! BuildingActor.MapUpdate()
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.PlanetsideAttribute(resourceSilo.GUID, 45, resourceSilo.CapacitorDisplay)
      )
      building.Actor ! BuildingActor.MapUpdate()
    }
    val ntuIsLow = resourceSilo.NtuCapacitor.toFloat / resourceSilo.Definition.MaxNtuCapacitor.toFloat < 0.2f
    if (resourceSilo.LowNtuWarningOn && !ntuIsLow) {
      LowNtuWarning(enabled = false)
    } else if (!resourceSilo.LowNtuWarningOn && ntuIsLow) {
      LowNtuWarning(enabled = true)
    }
    if (resourceSilo.NtuCapacitor == 0 && siloChargeBeforeChange > 0) {
      // Oops, someone let the base run out of power. Shut it all down.
      zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.PlanetsideAttribute(building.GUID, 48, 1))
      building.Actor ! BuildingActor.SetFaction(PlanetSideEmpire.NEUTRAL)
    } else if (siloChargeBeforeChange == 0 && resourceSilo.NtuCapacitor > 0) {
      // Power restored. Reactor Online. Sensors Online. Weapons Online. All systems nominal.
      //todo: Check generator is online before starting up
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.PlanetsideAttribute(building.GUID, 48, 0)
      )
      building.Zone.actor ! ZoneActor.ZoneMapUpdate()
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
    panelAnimationFunc(0)
  }

  /**
    * na
    *
    * @param sender na
    * @param min    a minimum amount of nanites requested;
    * @param max    the amount of nanites required to not make further requests;
    */
  def HandleNtuRequest(sender: ActorRef, min: Int, max: Int): Unit = {
    val originalAmount = resourceSilo.NtuCapacitor
    UpdateChargeLevel(-min)
    sender ! Ntu.Grant(resourceSilo, originalAmount - resourceSilo.NtuCapacitor)
  }

  /**
    * Accept nanites into the silo capacitor and set the animation state.
    */
  def HandleNtuGrant(sender: ActorRef, src: NtuContainer, amount: Int): Unit = {
    if (amount != 0) {
      val originalAmount = resourceSilo.NtuCapacitor
      UpdateChargeLevel(amount)
      panelAnimationFunc(resourceSilo.NtuCapacitor - originalAmount)
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
  def PanelAnimation(trigger: Int): Unit = {
    val zone = resourceSilo.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, resourceSilo.GUID, 49, if (trigger > 0) 1 else 0)
    ) // panel glow on & orb particles on
  }

  /**
    * Do nothing this turn.
    */
  def SkipPanelAnimation(trigger: Int): Unit = {}
}
