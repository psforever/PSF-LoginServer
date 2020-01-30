// Copyright (c) 2020 PSForever
package services.account

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects.serverobject.mount.Mountable

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import net.psforever.objects._
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.{RemoverActor, Service, ServiceManager}
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.VehicleServiceMessage

class AccountPersistenceService extends Actor {
  val log = org.log4s.getLogger
  var accountIndex : Long = 0
  val accounts : mutable.Map[String, ActorRef] = mutable.Map[String, ActorRef]()

  var squad : ActorRef = ActorRef.noSender
  var resolver : ActorRef = ActorRef.noSender

  override def preStart = {
    ServiceManager.serviceManager ! ServiceManager.Lookup("squad")
    ServiceManager.serviceManager ! ServiceManager.Lookup("taskResolver")
    log.trace("Starting; awaiting system hooks ...")
  }

  def receive : Receive = Setup

  val Started : Receive = {
    case msg @ AccountPersistenceService.Login(name) =>
      (accounts.get(name) match {
        case Some(ref) => ref
        case None => CreateNewPlayerToken(name)
      }).tell(msg, sender)

    case msg @ AccountPersistenceService.Update(name, _, _, _) =>
      accounts.get(name) match {
        case Some(ref) =>
          ref ! msg
        case None =>
          log.warn(s"tried to update a player entry ($name) that did not yet exist; rebuilding entry ...")
          CreateNewPlayerToken(name).tell(AccountPersistenceService.Login(name), sender)
      }

    case PlayerToken.LogoutInfo(target) =>
      accounts.remove(target)

    case _ => ;
  }

  val Setup : Receive = {
    case ServiceManager.LookupResult(id, endpoint) =>
      id match {
        case "squad" =>
          squad = endpoint
        case "taskResolver" =>
          resolver = endpoint
      }
      if(squad != ActorRef.noSender &&
        resolver != ActorRef.noSender) {
        log.trace("Hooks obtained.  Continuing with standard operation.")
        context.become(Started)
      }
    case _ => ;
  }

  def CreateNewPlayerToken(name : String) : ActorRef = {
    val ref = context.actorOf(Props(classOf[PlayerToken], name, squad, resolver), s"$name-$accountIndex")
    accountIndex += 1
    accounts += name -> ref
    ref
  }
}

object AccountPersistenceService {
  final case class Login(name : String)

  /**
    * na
    * Corpses become ineligible.
    * The player's name should be able to satisfy the condition:<br>
    * `zone.LivePlayers.exists(p => p.Name.equals(name))`<br>
    * If the player is in a facility SOI, record that facility's faction affinity.
    * If the player is in the wilderness, always list as `NEUTRAL`.
    * The zone's affinity is `NEUTRAL` unless it is empire locked.
    * @param name the unique name of the player
    * @param zone the current zone the player is in;
    * @param regionAffinity the last reported faction affinity of the surrounding region
    * @param zoneAffinity the last reported faction affinity of the zone
    */
  final case class Update(name : String, zone : Zone, regionAffinity : PlanetSideEmpire.Value, zoneAffinity : PlanetSideEmpire.Value)
}

class PlayerToken(name : String, squadService : ActorRef, taskResolver : ActorRef) extends Actor {
  val log = org.log4s.getLogger(s"Logout-$name")
  var inZone : Zone = Zone.Nowhere
  var regionAffinity : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  var zoneAffinity : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  var timer : Cancellable = DefaultCancellable.obj
  var timeup : Boolean = false
  UpdateTimer()

  override def postStop() : Unit = {
    super.postStop
    timer.cancel
    context.parent ! PlayerToken.LogoutInfo(name)
    performLogout()
  }

  def receive : Receive = {
    case PlayerToken.Logout(_) =>
      timeup = timer.cancel
      context.stop(self)

    case AccountPersistenceService.Login(_) =>
      sender ! PlayerToken.LoginInfo(name, inZone, regionAffinity, zoneAffinity)

    case AccountPersistenceService.Update(_, z, rf, zf) =>
      inZone = z
      regionAffinity = rf
      zoneAffinity = zf
      UpdateTimer()

    case _ => ;
  }

  def UpdateTimer() : Unit = {
    if(!timeup) {
      timer.cancel
      timer = context.system.scheduler.scheduleOnce(90000 milliseconds, self, PlayerToken.Logout(name))
    }
  }

  def performLogout() : Unit = {
    log.info(s"logout of $name")
    (inZone.Players.find(p => p.name == name), inZone.LivePlayers.find(p => p.Name == name)) match {
      case (Some(avatar), Some(player)) if player.VehicleSeated.nonEmpty =>
        //alive or dead in a vehicle
        //if the avatar is dead while in a vehicle, they haven't released yet
        //TODO perform any last minute saving now ...
        (inZone.GUID(player.VehicleSeated) match {
          case Some(vehicle : Mountable) =>
            (Some(vehicle), vehicle.Seat(vehicle.PassengerInSeat(player).getOrElse(-1)))
          case _ => (None, None) //bad data?
        }) match {
          case (Some(vehicle : Vehicle), Some(seat)) =>
            seat.Occupant = None //unseat
            if(vehicle.Health == 0 || vehicle.Seats.values.forall(seat => !seat.isOccupied)) {
              inZone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(vehicle), inZone))
              inZone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.AddTask(vehicle, inZone, if(vehicle.Flying) {
                //TODO gravity
                Some(0 seconds) //immediate deconstruction
              }
              else {
                vehicle.Definition.DeconstructionTime //normal deconstruction
              }))
            }
          case (Some(_), Some(seat)) =>
            seat.Occupant = None //unseat
          case _ => ;
        }
        StandardLogout(avatar, player)

      case (Some(avatar), Some(player)) =>
        //alive or dead, as Infantry; may be waiting for respawn
        //TODO perform any last minute saving now ...
        StandardLogout(avatar, player)

      case (Some(avatar), None) =>
        //player has released
        //our last body was turned into a corpse; just the avatar remains
        //TODO perform any last minute saving now ...
        squadService.tell(Service.Leave(Some(avatar.CharId.toString)), context.parent)
        Deployables.Disown(inZone, avatar, context.parent)
        inZone.GUID(avatar.VehicleOwned) match {
          case Some(obj : Vehicle) if obj.OwnerName.contains(avatar.name) =>
            obj.AssignOwnership(None)
          case _ => ;
        }
        inZone.Population.tell(Zone.Population.Release(avatar), context.parent)
        taskResolver.tell(GUIDTask.UnregisterLocker(avatar.Locker)(inZone.GUID), context.parent)

      case _ =>
        //user stalled during initial session, or was caught in between zone transfer
    }
  }

  def StandardLogout(avatar : Avatar, player : Player) : Unit = {
    val pguid = player.GUID
    player.Position = Vector3.Zero
    player.Health = 0
    squadService.tell(Service.Leave(Some(player.CharId.toString)), context.parent)
    Deployables.Disown(inZone, avatar, context.parent)
    inZone.Population.tell(Zone.Population.Release(avatar), context.parent)
    inZone.AvatarEvents.tell(AvatarServiceMessage(inZone.Id, AvatarAction.ObjectDelete(pguid, pguid)), context.parent)
    DisownVehicle(player)
    taskResolver.tell(GUIDTask.UnregisterAvatar(player)(inZone.GUID), context.parent)
  }

  /**
    * Vehicle cleanup that is specific to log out behavior.
    */
  def DisownVehicle(player : Player) : Unit = {
    Vehicles.Disown(player, inZone) match {
      case Some(vehicle) if vehicle.Seats.values.forall(seat => !seat.isOccupied) =>
        inZone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(vehicle), inZone))
        inZone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.AddTask(vehicle, inZone,
          if(vehicle.Flying) {
            //TODO gravity
            Some(0 seconds) //immediate deconstruction
          }
          else {
            vehicle.Definition.DeconstructionTime //normal deconstruction
          }
        ))
      case None => ;
    }
  }
}

object PlayerToken {
  final case class LoginInfo(name : String, zone : Zone, regionAffinity : PlanetSideEmpire.Value, zoneAffinity : PlanetSideEmpire.Value)
  private case class Logout(name : String)
  final case class LogoutInfo(name : String)
}
