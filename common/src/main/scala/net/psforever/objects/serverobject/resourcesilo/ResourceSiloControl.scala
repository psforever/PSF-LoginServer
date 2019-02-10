// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.resourcesilo

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.packet.game.PlanetSideGUID
import services.ServiceManager.Lookup
import services._
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
  * An `Actor` that handles messages being dispatched to a specific `Resource Silo`.
  * @param resourceSilo the `Resource Silo` object being governed
  */
class ResourceSiloControl(resourceSilo : ResourceSilo) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = resourceSilo
  var avatarService : ActorRef = Actor.noSender
  private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case "startup" =>
      ServiceManager.serviceManager ! Lookup("avatar")

    case ServiceManager.LookupResult("avatar", endpoint) =>
      avatarService = endpoint
      log.info("ResourceSiloControl: Silo " + resourceSilo.GUID + " Got avatar service " + endpoint)

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
      avatarService ! AvatarServiceMessage(resourceSilo.Owner.asInstanceOf[Building].Zone.Id, AvatarAction.PlanetsideAttribute(PlanetSideGUID(resourceSilo.Owner.asInstanceOf[Building].ModelId), 47, if(resourceSilo.LowNtuWarningOn) 1 else 0))

    case ResourceSilo.UpdateChargeLevel(amount: Int) =>
      val siloChargeBeforeChange = resourceSilo.ChargeLevel

      // Increase if positive passed in or decrease charge level if negative number is passed in
      resourceSilo.ChargeLevel += amount
      if(resourceSilo.ChargeLevel > 0) {
        log.trace(s"UpdateChargeLevel: Silo ${resourceSilo.GUID} set to ${resourceSilo.ChargeLevel}")
      }


      val ntuBarLevel = scala.math.round((resourceSilo.ChargeLevel.toFloat / resourceSilo.MaximumCharge.toFloat) * 10)
      // Only send updated capacitor display value to all clients if it has actually changed
      if(resourceSilo.CapacitorDisplay != ntuBarLevel) {
        log.trace(s"Silo ${resourceSilo.GUID} NTU bar level has changed from ${resourceSilo.CapacitorDisplay} to $ntuBarLevel")
        resourceSilo.CapacitorDisplay = ntuBarLevel
        resourceSilo.Owner.Actor ! Building.SendMapUpdate(all_clients = true)
        avatarService ! AvatarServiceMessage(resourceSilo.Owner.asInstanceOf[Building].Zone.Id, AvatarAction.PlanetsideAttribute(resourceSilo.GUID, 45, resourceSilo.CapacitorDisplay))
      }

      val ntuIsLow = resourceSilo.ChargeLevel.toFloat / resourceSilo.MaximumCharge.toFloat < 0.2f
      if(resourceSilo.LowNtuWarningOn && !ntuIsLow){
          self ! ResourceSilo.LowNtuWarning(enabled = false)
        } else if (!resourceSilo.LowNtuWarningOn && ntuIsLow) {
        self ! ResourceSilo.LowNtuWarning(enabled = true)
      }


      if(resourceSilo.ChargeLevel == 0 && siloChargeBeforeChange > 0) {
        // Oops, someone let the base run out of power. Shut it all down.
        //todo: Make base neutral if silo hits zero NTU

        //todo: temporarily disabled until warpgates can bring ANTs from sanctuary, otherwise we'd be stuck in a situation with an unpowered base and no way to get an ANT to refill it.
//        avatarService ! AvatarServiceMessage(resourceSilo.Owner.asInstanceOf[Building].Zone.Id, AvatarAction.PlanetsideAttribute(PlanetSideGUID(resourceSilo.Owner.asInstanceOf[Building].ModelId), 48, 1))
      } else if (siloChargeBeforeChange == 0 && resourceSilo.ChargeLevel > 0) {
        // Power restored. Reactor Online. Sensors Online. Weapons Online. All systems nominal.
        //todo: Check generator is online before starting up
        avatarService ! AvatarServiceMessage(resourceSilo.Owner.asInstanceOf[Building].Zone.Id, AvatarAction.PlanetsideAttribute(PlanetSideGUID(resourceSilo.Owner.asInstanceOf[Building].ModelId), 48, 0))
      }
    case _ => ;
  }


}
