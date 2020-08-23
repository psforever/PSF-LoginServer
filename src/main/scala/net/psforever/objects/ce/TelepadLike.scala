// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

import akka.actor.ActorContext
import net.psforever.objects.{Default, PlanetSideGameObject, TelepadDeployable, Vehicle}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.vehicles.Utility
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID

trait TelepadLike {
  private var router: Option[PlanetSideGUID] = None
  private var activated: Boolean             = false

  def Router: Option[PlanetSideGUID] = router

  def Router_=(rguid: PlanetSideGUID): Option[PlanetSideGUID] = Router_=(Some(rguid))

  def Router_=(rguid: Option[PlanetSideGUID]): Option[PlanetSideGUID] = {
    router match {
      case None =>
        router = rguid
      case Some(_) =>
        if (rguid.isEmpty || rguid.contains(PlanetSideGUID(0))) {
          router = None
        }
    }
    Router
  }

  def Active: Boolean = activated

  def Active_=(state: Boolean): Boolean = {
    activated = state
    Active
  }
}

object TelepadLike {
  final case class Activate(obj: PlanetSideGameObject with TelepadLike)

  final case class Deactivate(obj: PlanetSideGameObject with TelepadLike)

  /**
    * Assemble some logic for a provided object.
    * @param obj an `Amenity` object;
    *            anticipating a `Terminal` object using this same definition
    * @param context hook to the local `Actor` system
    */
  def Setup(obj: Amenity, context: ActorContext): Unit = {
    obj.asInstanceOf[TelepadLike].Router = obj.Owner.GUID
    import akka.actor.Props
    if (obj.Actor == Default.Actor) {
      obj.Actor = context.actorOf(Props(classOf[TelepadControl], obj), PlanetSideServerObject.UniqueActorName(obj))
    }
  }

  /**
    * An analysis of the active system of teleportation utilized by Router vehicles.
    * Information about the two endpoints - an internal telepad and a remote telepad - are collected, if they are applicable.
    * The vehicle "Router" itself must be in the drive state of `Deployed`.
    * @param router the vehicle that serves as the container of an internal telepad unit
    * @param zone where the router is located
    * @return the pair of units that compose the teleportation system
    */
  def AppraiseTeleportationSystem(router: Vehicle, zone: Zone): Option[(Utility.InternalTelepad, TelepadDeployable)] = {
    import net.psforever.objects.vehicles.UtilityType
    import net.psforever.types.DriveState
    router.Utility(UtilityType.internal_router_telepad_deployable) match {
      //if the vehicle has an internal telepad, it is allowed to be a Router (that's a weird way of saying it)
      case Some(util: Utility.InternalTelepad) =>
        //check for a readied remote telepad
        zone.GUID(util.Telepad) match {
          case Some(telepad: TelepadDeployable) =>
            //determine whether to activate both the Router's internal telepad and the deployed remote telepad
            if (router.DeploymentState == DriveState.Deployed && util.Active && telepad.Active) {
              Some((util, telepad))
            } else {
              None
            }
          case _ =>
            None
        }
      case _ =>
        None
    }
  }
}

/**
  * Telepad-like components don't actually use control agents right now, but,
  * since the `trait` is used for a `Vehicle` `Utility` entity as well as a `Deployable` entity,
  * and all utilities are supposed to have control agents with which to interface,
  * a placeholder like this is easy to reason around.
  * @param obj an entity that extends `TelepadLike`
  */
class TelepadControl(obj: TelepadLike) extends akka.actor.Actor {
  def receive: akka.actor.Actor.Receive = {
    case _ => ;
  }
}
