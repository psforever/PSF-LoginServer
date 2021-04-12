// Copyright (c) 2018 PSForever
package net.psforever.objects.ce

import akka.actor.{ActorContext, Cancellable}
import net.psforever.objects.{Default, TelepadDeployable, Vehicle}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.vehicles.Utility.InternalTelepad
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{GenericObjectActionMessage, ObjectCreateMessage, ObjectDeleteMessage}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
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
  final case class RequestLink(obj: TelepadDeployable)

  final case class SeverLink(obj: PlanetSideServerObject with TelepadLike)

  final case class Activate(obj: PlanetSideServerObject with TelepadLike)

  final case class Deactivate(obj: PlanetSideServerObject with TelepadLike)

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
  def AppraiseTeleportationSystem(router: Vehicle, zone: Zone): Option[(InternalTelepad, TelepadDeployable)] = {
    import net.psforever.objects.vehicles.UtilityType
    import net.psforever.types.DriveState
    router.Utility(UtilityType.internal_router_telepad_deployable) match {
      //if the vehicle has an internal telepad, it is allowed to be a Router (that's a weird way of saying it)
      case Some(util: InternalTelepad) =>
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

  /**
    * Create the mechanism that serves as one endpoint of the linked router teleportation system.<br>
    * <br>
    * Technically, the mechanism - an `InternalTelepad` object - is always made to exist
    * due to how the Router vehicle object is encoded into an `ObjectCreateMessage` packet.
    * Regardless, that internal mechanism is created anew each time the system links a new remote telepad.
    * @param routerGUID the vehicle that houses one end of the teleportation system (the `internalTelepad`)
    * @param obj the endpoint of the teleportation system housed by the router
    */
  def StartRouterInternalTelepad(zone: Zone, routerGUID: PlanetSideGUID, obj: InternalTelepad): Unit = {
    val utilityGUID = obj.GUID
    val udef  = obj.Definition
    val events = zone.LocalEvents
    val zoneId = zone.id
    /*
    the following instantiation and configuration creates the internal Router component
    normally dispatched while the Router is transitioned into its Deploying state
    it is safe, however, to perform these actions at any time during and after the Deploying state
     */
    events ! LocalServiceMessage(
      zoneId,
      LocalAction.SendResponse(
        ObjectCreateMessage(
          udef.ObjectId,
          utilityGUID,
          ObjectCreateMessageParent(routerGUID, 2), //TODO stop assuming slot number
          udef.Packet.ConstructorData(obj).get
        )
      )
    )
    events ! LocalServiceMessage(
      zoneId,
      LocalAction.SendResponse(GenericObjectActionMessage(utilityGUID, 27))
    )
    events ! LocalServiceMessage(
      zoneId,
      LocalAction.SendResponse(GenericObjectActionMessage(utilityGUID, 30))
    )
    LinkTelepad(zone, utilityGUID)
  }

  def LinkTelepad(zone: Zone, telepadGUID: PlanetSideGUID): Unit = {
    val events = zone.LocalEvents
    val zoneId = zone.id
    events ! LocalServiceMessage(
      zoneId,
      LocalAction.SendResponse(GenericObjectActionMessage(telepadGUID, 27))
    )
    events ! LocalServiceMessage(
      zoneId,
      LocalAction.SendResponse(GenericObjectActionMessage(telepadGUID, 28))
    )
  }
}

/**
  * Telepad-like components don't actually use control agents right now, but,
  * since the `trait` is used for a `Vehicle` `Utility` entity as well as a `Deployable` entity,
  * and all utilities are supposed to have control agents with which to interface,
  * a placeholder like this is easy to reason around.
  * @param obj an entity that extends `TelepadLike`
  */
class TelepadControl(obj: InternalTelepad) extends akka.actor.Actor {
  var setup: Cancellable = Default.Cancellable

  def receive: akka.actor.Actor.Receive = {
    case TelepadLike.Activate(o: InternalTelepad) if obj eq o =>
      obj.Active = true

    case TelepadLike.Deactivate(o: InternalTelepad) if obj eq o =>
      obj.Active = false
      val zone = obj.Zone
      zone.GUID(obj.Telepad) match {
        case Some(oldTpad: TelepadDeployable) if !obj.Active && !setup.isCancelled =>
          oldTpad.Actor ! TelepadLike.SeverLink(obj)
        case None => ;
      }
      obj.Telepad = None
      zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.SendResponse(ObjectDeleteMessage(obj.GUID, 0)))

    case TelepadLike.RequestLink(tpad: TelepadDeployable) =>
      val zone = obj.Zone
      if (obj.Active) {
        zone.GUID(obj.Telepad) match {
          case Some(oldTpad: TelepadDeployable) if !obj.Active && !setup.isCancelled =>
            oldTpad.Actor ! TelepadLike.SeverLink(obj)
          case None => ;
        }
        obj.Telepad = tpad.GUID
        //zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.StartRouterInternalTelepad(obj.Owner.GUID, obj.GUID, obj))
        TelepadLike.StartRouterInternalTelepad(zone, obj.Owner.GUID, obj)
        tpad.Actor ! TelepadLike.Activate(obj)
      } else {
        val channel = obj.Owner.asInstanceOf[Vehicle].OwnerName.getOrElse("")
        zone.LocalEvents ! LocalServiceMessage(channel, LocalAction.RouterTelepadMessage("@Teleport_NotDeployed"))
        tpad.Actor ! TelepadLike.SeverLink(obj)
      }

    case TelepadLike.SeverLink(tpad: TelepadDeployable) =>
      if (obj.Telepad.contains(tpad.GUID)) {
        obj.Telepad = None
        val zone = obj.Zone
        zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.SendResponse(ObjectDeleteMessage(obj.GUID, 0)))
      }

    case _ => ;
  }
}
